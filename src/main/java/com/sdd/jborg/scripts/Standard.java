package com.sdd.jborg.scripts;

import com.sdd.jborg.CoffeeScript;
import com.sdd.jborg.Logger;
import com.sdd.jborg.networks.Networks;
import com.sdd.jborg.Server;
import com.sdd.jborg.Ssh;
import com.sdd.jborg.util.Callback0;
import com.sdd.jborg.util.Crypto;
import com.sdd.jborg.util.FileSystem;
import groovy.text.StreamingTemplateEngine;
import org.reflections.Reflections;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sdd.jborg.scripts.params.StandardParams.*;
import static com.sdd.jborg.util.Crypto.Algorithm.*;

/**
 * Standard fields and methods every script should have in scope.
 */
public class Standard
{
	/**
	 * Base Script class all scripts must extend.
	 */
	public static abstract class Script
	{
		private static final Set<Class<? extends Script>> scripts = new Reflections().getSubTypesOf(Script.class);

		public static Script findMatch()
		{
			for (Class<? extends Script> script : scripts)
			{
				final Script instance;
				try
				{
					instance = script.newInstance();
					if (instance.match())
					{
						return instance;
					}
				}
				catch (InstantiationException | IllegalAccessException e)
				{
					e.printStackTrace();
				}
			}
			return null;
		}

		abstract public boolean match();

		abstract public void assimilate();
	}


	// Global Attributes

	public static final Networks networks = new Networks(CoffeeScript.readCsonFileToJsonObject("networks.coffee"));
	//public static final Networks networks = new Networks();
	public static final Server server = new Server(); // a.k.a. "locals"
	public static Ssh ssh;

	// Async Flow Control

	private static Queue<Callback0> queue = new ArrayDeque<>();

	public static void then(final Params params)
	{
		queue.add(params.callback);
	}

	public static void go()
	{
		while (queue.size() > 0)
		{
			queue.poll().call();
		}
	}

	// Helpers

	private static final Pattern BASH_PATTERN = Pattern.compile("([^0-9a-z-])", Pattern.CASE_INSENSITIVE);

	public static String bashEscape(final String cmd)
	{
		final Matcher matcher = BASH_PATTERN.matcher(cmd);
		return matcher.replaceAll("\\$1");
	}

	public static void die(final Exception reason)
	{
		Logger.err("Aborting. Reason: " + reason.getMessage());
		reason.printStackTrace();
		System.exit(1);
	}

	public static void notifySkip(final Exception reason)
	{
		Logger.err("Skipping. Reason: " + reason.getMessage());
		reason.printStackTrace();
	}

	public static boolean empty(final String value)
	{
		return value == null || value.equals("");
	}

	public static boolean empty(final Object value)
	{
		return value == null;
	}

	public static boolean empty(final String[] value)
	{
		return value == null || value.length < 1;
	}

	public static String encrypt(final String s)
	{
		return Crypto.encrypt(s);
	}

	public static String decrypt(final String s)
	{
		return Crypto.decrypt(s);
	}

	/**
	 * Will notify user and abort process.
	 */
	public static final class DeveloperInputValidationException extends Exception
	{
		public DeveloperInputValidationException(String message)
		{
			super(message);
		}
	}

	/**
	 * Will step out, notify user, and continue.
	 */
	public static final class RemoteServerValidationException extends Exception
	{
		public RemoteServerValidationException(String message)
		{
			super(message);
		}
	}

	public interface ScriptCallback1<T>
	{
		void call(final T t)
			throws DeveloperInputValidationException,
			RemoteServerValidationException;
	}

	public static <T extends Params> T chainForCb(final T p, final ScriptCallback1<T> cb)
	{
		p.callback = () -> {
			try
			{
				cb.call(p);
			}
			catch (final RemoteServerValidationException e)
			{
				com.sdd.jborg.scripts.Standard.notifySkip(e);
			}
			catch (final DeveloperInputValidationException e)
			{
				die(e);
			}
		};
		return p;
	}

	private static final class Container<T>
	{
		private T t;

		public void set(final T t)
		{
			this.t = t;
		}

		public T get()
		{
			return t;
		}
	}

	public static ExecuteParams execute(final String cmd)
	{
		return chainForCb(new ExecuteParams(), p -> {
			// TODO: could do this with simple while loop probably; would be less complex
			final AtomicInteger triesRemaining = new AtomicInteger(p.getRetryTimes());
			final Container<Callback0> _try = new Container<>();
			_try.set(() -> ssh.cmd(p.getSudoCmd() + cmd, (code, out, err) -> {
				String error = null;
				// TODO: implement multiple types of expectations?
				// for now just implementing code check because i think
				// that was the most common case.
				if (p.getExpectCode() != null)
				{
					if (code != 0)
					{
						if (code != p.getExpectCode())
						{
							error = "Expected exit code " + p.getExpectCode() + ", but got " + code + ".";
						}
						if (error == null)
						{
							Logger.info("NOTICE: Non-zero exit code was expected. Will continue.");
						}
						else if (p.isIgnoringErrors())
						{
							Logger.info("NOTICE: Non-zero exit code can be ignored. Will continue.");
						}
					}
				}

				if (error != null)
				{
					if (triesRemaining.decrementAndGet() > 0)
					{
						Logger.err(error + " Will try again...");
						_try.get().call(); // try again
					}
					else
					{
						die(new RemoteServerValidationException(error + " Tried " + p.getRetryTimes() + " times. Giving up."));
					}
				}

				// TODO: wait until tries are over... DON'T invoke test on every try

				if (!empty(p.getTest()))
					p.getTest().call(code, out, err);
			}));

			_try.get().call();
		});
	}

	public static ServiceParams service(final String serviceName)
	{
		return chainForCb(new ServiceParams(), p -> {
			execute("service " + serviceName + " " + p.getAction())
				.setSudo(true)
				.callImmediate();
		});
	}


	public static ChownParams chown(final String path)
	{
		return chainForCb(new ChownParams(), p -> {
			if (empty(p.getOwner()) || empty(p.getGroup()))
				throw new DeveloperInputValidationException("chown owner and group are required.");

			execute("chown " +
				(p.getRecursive() ? "-R " : "") +
				p.getOwner() +
				"." + p.getGroup() +
				" " + path)
				.setSudoCmd(p.getSudoCmd())
				.callImmediate();
		});
	}

	public static ChmodParams chmod(final String path)
	{
		return chainForCb(new ChmodParams(), p -> {
			if (empty(p.getMode()))
				throw new DeveloperInputValidationException("mode is required.");

			execute("chmod " +
				p.getMode() +
				" " + path)
				.setSudoCmd(p.getSudoCmd())
				.callImmediate();
		});
	}

	public static DirectoryParams directory(final String path)
	{
		return chainForCb(new DirectoryParams(), p -> {
			if (empty(p.getMode()))
				p.setMode("0755");

			execute("test -d " + path)
				.setTest((code, out, err) -> {
					if (code == 0)
					{
						Logger.info("Skipping existing directory.");
					}
					else
					{
						execute("mkdir " +
							(p.getRecursive() ? " -p" : "") +
							" " + path)
							.setSudoCmd(p.getSudoCmd())
							.callImmediate();
					}
					//Check if user exists
					if (!empty(p.getOwner()) || !empty(p.getGroup()))
						chown(path)
							.setOwner(p.getOwner())
							.setGroup(p.getGroup())
							.setSudoCmd(p.getSudoCmd())
							.callImmediate();

					if (!empty(p.getMode()))
						chmod(path)
							.setMode(p.getMode())
							.setSudoCmd(p.getSudoCmd())
							.callImmediate();
				})
				.callImmediate();
		});
	}

	public static UserParams user(final String name)
	{
		return chainForCb(new UserParams(), p -> {
			execute("id " + name)
				.setTest((code, out, err) -> {
					if (code == 0)
						throw new RemoteServerValidationException("user " + name + " exists.");

					execute("useradd " + name + " \\\n" +
						"  --create-home \\\n" +
						"  --user-group \\\n" +
						(!empty(p.getComment()) ? "  --comment " + bashEscape(p.getComment()) + " \\\n" : "") +
						(!empty(p.getPassword()) ? "  --password " + bashEscape(p.getPassword()) + " \\\n" : "") +
						("  --shell " + (empty(p.getShell()) ? "/bin/bash" : "")))
						.setSudoCmd(p.getSudoCmd())
						.callImmediate();

					if (!empty(p.getGroupName()))
						execute("usermod -g " + p.getGroupName() + " " + name)
							.setSudoCmd(p.getSudoCmd())
							.callImmediate();

					if (!empty(p.getGroups()))
						for (final String group : p.getGroups())
							execute("usermod -a -G " + group + " " + name)
								.setSudoCmd(p.getSudoCmd())
								.callImmediate();

					if (!empty(p.getSshKeys()))
						for (final String key : p.getSshKeys())
						{
							directory("$(echo ~" + name + ")/.ssh/")
								.setRecursive(true)
								.setMode("0700")
								.setSudoCmd(p.getSudoCmd())
								.callImmediate();
							execute("touch $(echo ~" + name + ")/.ssh/authorized_keys")
								.setSudoCmd(p.getSudoCmd())
								.callImmediate();
							chmod("$(echo ~" + name + ")/.ssh/authorized_keys")
								.setMode("0600")
								.setSudoCmd(p.getSudoCmd())
								.callImmediate();
							execute("echo " + bashEscape(key) + " | sudo tee -a $(echo ~" + name + ")/.ssh/authorized_keys >/dev/null")
								.setSudoCmd(p.getSudoCmd())
								.callImmediate();
							chown("$(echo ~" + name + ")/.ssh/")
								.setRecursive(true)
								.setOwner(name)
								.setGroup(name)
								.setSudoCmd(p.getSudoCmd())
								.callImmediate();
						}
				})
				.callImmediate();
		});
	}

	public static void include(final Class<? extends Includable> cls)
	{
		try
		{
			cls.newInstance().include();
		}
		catch (IllegalAccessException | InstantiationException e)
		{
			e.printStackTrace();
		}
	}

	public static Params install(final String packages)
	{
		return chainForCb(new Params(), p -> {
			execute("dpkg -s " + packages + " 2>&1 | grep 'is not installed and'")
				.setTest((code, out, err) -> {
					if (code != 0)
					{
						Logger.info("Skipping package(s) already installed.");
						return;
					}

					execute("DEBIAN_FRONTEND=noninteractive apt-get install -y " + packages)
						.setSudo(true)
						.setRetry(3)
						.expect(0)
						.callImmediate();
				}).callImmediate();
		});
	}

	public static UninstallParams uninstall(final String packages)
	{
		return chainForCb(new UninstallParams(), p -> {
			execute("dpkg -s " + packages + " 2>&1 | grep 'install ok installed'")
				.setSudo(true)
				.setTest((code, out, err) -> {
					if (code != 0)
					{
						throw new RemoteServerValidationException(packages + " is not installed, ignoring");
					}
					else
					{
						execute("DEBIAN_FRONTEND=noninteractive apt-get " + ((p.isPurge() == true) ? "purge " : "uninstall ") + packages)
							.setSudo(true)
							.setRetry(3)
							.expect(0)
							.callImmediate();
					}
				}).callImmediate();
		});
	}

	public static Params update()
	{
		return chainForCb(new Params(), p -> {
			execute("apt-get update")
				.setSudo(true)
				.setRetry(3)
				.expect(0)
				.callImmediate();

			execute("DEBIAN_FRONTEND=noninteractive apt-get dist-upgrade -y")
				.setSudo(true)
				.setRetry(3)
				.expect(0)
				.callImmediate();
		});
	}

public static Params hostfileEntry(final String hostname, final String ip)
{
	return chainForCb(new Params(), p -> {

	});
}


	public static DeployParams deploy(final String appName)
	{
		return chainForCb(new DeployParams(), p -> {
			// force sudo as deploy owner
			p.setSudoAsUser(p.getOwner());

			final String privateKeyPath = "$(echo ~" + p.getOwner() + ")/.ssh/id_rsa";

			directory("$(echo ~" + p.getOwner() + ")/")
				.setOwner(p.getOwner())
				.setGroup(p.getGroup())
				.setSudo(true)
				.setRecursive(true)
				.setMode("0700")
				.callImmediate();

			directory("$(echo ~" + p.getOwner() + ")/.ssh/")
				.setOwner(p.getOwner())
				.setGroup(p.getGroup())
				.setSudo(true)
				.setRecursive(true)
				.setMode("0700")
				.callImmediate();

			// write ssh key to ~/.ssh/
			template(privateKeyPath)
				.setContent(p.getGit().getDeployKey())
				.setOwner(p.getOwner())
				.setGroup(p.getGroup())
				.setMode("0600")
				.setSudo(true)
				.callImmediate();
		});
	}

	private static String tmpFile(final String seed)
	{
		return Crypto.computeHash(SHA_1, seed) +
			Long.toHexString(Double.doubleToRawLongBits(Math.random())).toUpperCase().substring(8);
	}

	/**
	 * Compiles text output from strings or local files on disk when given a map of variables to fill in.
	 *
	 * Template engine syntax and details:
	 * http://docs.groovy-lang.org/latest/html/documentation/template-engines.html#_streamingtemplateengine
	 */
	public static TemplateParams template(final String path)
	{
		return chainForCb(new TemplateParams(), p -> {
			final String template;
			if (p.getContent() != null)
			{
				// template from string
				p.setTo(path);
				template = p.getContent();
			}
			else
			{
				// template from disk
				template = FileSystem.readFileToString(path + ".template");
			}

			if (p.getTo() == null)
				throw new DeveloperInputValidationException("to is a required parameter");

			// compile template variables
			final String output;
			try
			{
				output = new StreamingTemplateEngine()
					.createTemplate(template)
					.make(p.getVariables())
					.toString();
			}
			catch (ClassNotFoundException | IOException e)
			{
				e.printStackTrace();
				return; // abort
			}

			// log for debugging purposes
			final String ver = tmpFile(output);
			Logger.info("rendering file " + p.getTo() + " version " + ver);
			Logger.out("---- BEGIN FILE ----\n" + output + "\n--- END FILE ---");

			// write string to temporary file on local disk
			final Path tmpFile = Paths.get(System.getProperty("java.io.tmpdir"), "local-" + ver);
			FileSystem.writeStringToFile(tmpFile, output);

			// upload file to remote disk
			upload(tmpFile)
				.setTo("/tmp/remote-" + ver)
				.setFinalTo(p.getTo())
				.setSudoCmd(p.getSudoCmd())
				.setOwner(p.getOwner())
				.setGroup(p.getGroup())
				.setMode(p.getMode())
				.callImmediate();

			// delete temporary file from local test
			FileSystem.unlink(tmpFile);
		});
	}

	/**
	 * upload a file from localhost to the remote host with sftp
	 */
	public static UploadParams upload(final Path path)
	{
		return chainForCb(new UploadParams(), p -> {
			if (p.getTo() == null)
				throw new DeveloperInputValidationException("to is a required parameter");

			final String ver = tmpFile(path.toString());

			// TODO: implement file decryption

			if (p.getFinalTo() == null)
			{
				p.setFinalTo(p.getTo());
				p.setTo("/tmp/remote-"+ver);
			}

			// TODO: check if remote file exists

			Logger.info("SFTP uploading "+ path.toFile().length() +" "+
				(p.isEncrypted() ? "decrypted " : "") +
				"bytes from \""+ path +"\" to \""+ p.getFinalTo() +"\" "+
				(!p.getFinalTo().equals(p.getTo()) ? " through temporary file \""+ p.getTo() +"\"" : "") +
				"...");

			ssh.put(path, p.getTo(), e -> {
				die(new RemoteServerValidationException("error during SFTP file transfer: " + e.getMessage()));
			});

			Logger.info("SFTP upload complete.");

			// set ownership and permissions
			chown(p.getTo())
				.setSudoCmd(p.getSudoCmd())
				.setOwner(p.getOwner())
				.setGroup(p.getGroup())
				.callImmediate();
			chmod(p.getTo())
				.setSudoCmd(p.getSudoCmd())
				.setMode(p.getMode())
				.callImmediate();

			// move into final location
			execute("mv " + p.getTo() +" "+ p.getFinalTo())
				.setSudoCmd(p.getSudoCmd())
				.expect(0)
				.callImmediate();
		});
	}

	/**
	 * download a file from the internet to the remote host with wget
	 */
	public static DownloadParams download(final String uri, final String destination)
	{
		return chainForCb(new DownloadParams(), p -> {
			//TODO: Once remote file exists function is verified, check if the file already exists before downloading
			//for now, just download
			execute("wget -nv " + uri + " " + bashPrefix("-O", destination))
				.setSudoCmd(p.getSudoCmd())
				.callImmediate();

			if (!empty(p.getOwner()) && !empty(p.getGroup())) {
				chown(destination)
					.setOwner(p.getOwner())
					.setGroup(p.getGroup())
					.setSudoCmd(p.getSudoCmd())
					.callImmediate();
			}

			if (!empty(p.getMode()))
			{
				chmod(destination)
					.setMode(p.getMode())
					.setSudoCmd(p.getSudoCmd())
					.callImmediate();
			}
		});
	}

	public static Params sysctl(final String variable, final String value)
	{
		return chainForCb(new Params(), params -> {
			//remove any existing values for this key in sysctl.conf
			execute("sed -i '/^" + variable + "/d' /etc/sysctl.conf")
				.setSudo(true)
				.callImmediate();
			//Apply changes to sysctl.conf so that it persists beyond a reboot
			execute("echo '" + variable + " = " + value + "' | sudo tee -a /etc/sysctl.conf > /dev/null")
				.callImmediate();
			//Reload sysctl
			execute("sysctl -q -p /etc/sysctl.conf")
				.setSudo(true)
				.callImmediate();
		});
	}

	public static String bashPrefix(String flag, String value)
	{
		if (value != null)
			return " " + flag + " " + value + " ";
		else
			return "";
	}

	public static String bashPrefix(String flag)
	{
		return " " + flag + " ";
	}

	public interface Includable
	{
		void include();
	}

	private static final Pattern CHECKSUM_PATTERN = Pattern.compile("/[a-f0-9]{64}/");

	/**
	 * Check whether a file exists on the remote server's disk.
	 *
	 * @param path path to file on remote server
	 * @return Composable parameters:
	 * CompareLocalFile - path to file on local machine to hash and compare to hash of remote file
	 * CompareChecksum - sha256 hash string to compare hash of remote file to
	 */
	public static RemoteFileExistsParams remoteFileExists(final String path)
	{
		return chainForCb(new RemoteFileExistsParams(), p -> {
			if (empty(p.getCompareLocalFile()) && empty(p.getCompareChecksum()))
			{
				execute("stat " + path)
					.setSudoCmd(p.getSudoCmd())
					.setTest((code, out, err) -> {
						if (code == 0)
						{
							Logger.info("Remote file " + path + " exists.");
							p.invokeTrueCallback();
						}
						else
						{
							p.invokeFalseCallback();
						}
					})
					.callImmediate();
			}
			else
			{
				if (p.getCompareLocalFile() != null)
				{
					execute("sha256sum " + path)
						.setSudoCmd(p.getSudoCmd())
						.setTest((code, out, err) -> {
							final Matcher matcher = CHECKSUM_PATTERN.matcher(out);
							if (matcher.matches())
							{
								if (matcher.group(0).equals(
									Crypto.computeHash(SHA_256,
										FileSystem.readFileToBytes(p.getCompareLocalFile()))))
								{
									Logger.info("Remote file checksum of " + matcher.group(0) +
										" matches checksum of local file " + p.getCompareLocalFile() + ".");
									p.invokeTrueCallback();
								}
								else
								{
									Logger.info("Remote file checksum of " + matcher.group(0) +
										" did not match checksum of local file " + p.getCompareLocalFile() + ".");
									p.invokeFalseCallback();
								}
							}
						})
						.callImmediate();
				}
				if (p.getCompareChecksum() != null)
				{
					execute("sha256sum " + path)
						.setSudoCmd(p.getSudoCmd())
						.setTest((code, out, err) -> {
							final Matcher matcher = CHECKSUM_PATTERN.matcher(out);
							if (matcher.matches())
							{
								if (matcher.group(0).equals(p.getCompareChecksum()))
								{
									Logger.info("Remote file checksum " + matcher.group(0) + " matches expected checksum " + p.getCompareChecksum() + ".");
									p.invokeTrueCallback();
								}
								else
								{
									Logger.info("Remote file checksum " + matcher.group(0) + " does not match expected checksum " + p.getCompareChecksum() + ".");
									p.invokeFalseCallback();
								}
							}
							else
							{
								Logger.info("Unexpected problems reading remote file checksum.  Assuming remote file checksum does not match expected checksum " + p.getCompareChecksum() + ".");
								p.invokeFalseCallback();
							}
						})
						.callImmediate();
				}
			}
		});
	}
}
