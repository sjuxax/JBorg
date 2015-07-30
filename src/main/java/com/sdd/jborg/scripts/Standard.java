package com.sdd.jborg.scripts;

import com.sdd.jborg.CoffeeScript;
import com.sdd.jborg.Logger;
import com.sdd.jborg.networks.Networks;
import com.sdd.jborg.Server;
import com.sdd.jborg.Ssh;
import com.sdd.jborg.util.Callback0;
import com.sdd.jborg.util.FileSystem;
import org.reflections.Reflections;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.FileInputStream;
import java.util.ArrayDeque;
import java.util.Base64;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sdd.jborg.scripts.params.StandardParams.*;

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

	public static void log(final String msg)
	{
		Logger.info(msg);
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

	// Encryption
	private static final String CIPHER_TYPE = "AES/CBC/PKCS5Padding";
	private static final SecretKey secret = readSecret();
	private static final IvParameterSpec iv = new IvParameterSpec(new byte[16]);

	private static SecretKey readSecret()
	{
		try
		{
			return new SecretKeySpec(MessageDigest.getInstance("SHA-256")
				.digest(FileSystem.readFileToBytes("secret")), "AES");
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static String encrypt(final String s)
	{
		try
		{
			final Cipher cipher = Cipher.getInstance(CIPHER_TYPE);
			cipher.init(Cipher.ENCRYPT_MODE, secret, iv);
			return Base64.getEncoder().encodeToString(cipher.doFinal(s.getBytes(StandardCharsets.UTF_8)));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return "";
		}
	}

	public static String decrypt(final String s)
	{
		try
		{
			final Cipher cipher = Cipher.getInstance(CIPHER_TYPE);
			cipher.init(Cipher.DECRYPT_MODE, secret, iv);
			return new String(cipher.doFinal(Base64.getDecoder().decode(s)));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return "";
		}
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
							log("NOTICE: Non-zero exit code was expected. Will continue.");
						}
						else if (p.isIgnoringErrors())
						{
							log("NOTICE: Non-zero exit code can be ignored. Will continue.");
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
						die(new RuntimeException(error + " Tried " + p.getRetryTimes() + " times. Giving up."));
					}
				}

				// wait until tries are over...

				if (!empty(p.getTest()))
					p.getTest().call(code, out, err);
			}));

			_try.get().call();
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
						log("Skipping existing directory.");
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

	public static Callback0 template(final String path)
	{
		return () -> {

		};
	}

	public static Callback0 upload(final String path)
	{
		return () -> {

		};
	}

	public interface Includable
	{
		void include();
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
						log("Skipping package(s) already installed.");
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

	public static DeployParams deploy(final String appName)
	{
		return chainForCb(new DeployParams(), p -> {
			execute("dpkg -s " + packages + " 2>&1 | grep 'is not installed and'")
				.setTest((code, out, err) -> {
					if (code != 0)
					{
						log("Skipping package(s) already installed.");
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
		return chainForCb(new RemoteFileExistsParams(), (p) -> {
			if (empty(p.getCompareLocalFile()) && empty(p.getCompareChecksum()))
			{
				execute("stat " + path)
					.setSudoCmd(p.getSudoCmd())
					.setTest((code, out, err) -> {
						if (code == 0)
						{
							log("Remote file " + path + " exists.");
							p.invokeTrueCallback();
						}
						else
						{
							p.invokeFalseCallback();
						}
					});
			}
			else
			{
				final String localChecksum; // TODO: calculate checksum
				if (p.getCompareLocalFile().getClass() == String.class)
				{
					localChecksum = getHash(p.getCompareLocalFile());
					execute("sha256sum " + path)
						.setSudoCmd(p.getSudoCmd())
						.setTest((code, out, err) -> {
							final Matcher matcher = CHECKSUM_PATTERN.matcher(out);
							if (matcher.matches())
							{
								if (matcher.group(0).equals(localChecksum))
								{
									log("Remote file checksum of " + matcher.group(0) + " matches checksum of local file " + p.getCompareLocalFile() + ".");
									p.invokeTrueCallback();
								}
								else
								{
									log("Remote file checksum of " + matcher.group(0) + " did not match checksum of local file " + p.getCompareLocalFile() + ".");
									p.invokeFalseCallback();
								}
							}
						});
				}
				if (p.getCompareChecksum().getClass() == String.class)
				{
					execute("sha256sum " + path)
						.setSudoCmd(p.getSudoCmd())
						.setTest((code, out, err) -> {
							final Matcher matcher = CHECKSUM_PATTERN.matcher(out);
							if (matcher.matches())
							{
								if (matcher.group(0).equals(localChecksum))
								{
									log("Remote file checksum " + matcher.group(0) + " matches expected checksum " + localChecksum + ".");
									p.invokeTrueCallback();
								}
								else
								{
									log("Remote file checksum " + matcher.group(0) + " does not match expected checksum " + localChecksum + ".");
									p.invokeFalseCallback();
								}
							}
							else
							{
								log("Unexpected problems reading remote file checksum.  Assuming remote file checksum does not match expected checksum " + localChecksum + ".");
								p.invokeFalseCallback();
							}
						});
				}
			}
		});
	}

	private static String bytesToHexString(final byte[] bytes)
	{
		StringBuilder hexLine = new StringBuilder();
		for (int i = 0; i < bytes.length; i++)
		{
			hexLine.append(String.format("%02X", bytes[i]));
		}
		return hexLine.toString();
	}

	private static String getHash(final String filename)
	{
		final MessageDigest digest;
		try
		{
			digest = MessageDigest.getInstance("SHA-256");
			digest.update(FileSystem.readFileToBytes(filename));
			return bytesToHexString(digest.digest());
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
			return "";
		}
	}
}
