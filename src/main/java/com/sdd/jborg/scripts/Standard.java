package com.sdd.jborg.scripts;

import com.sdd.jborg.CoffeeScript;
import com.sdd.jborg.Logger;
import com.sdd.jborg.Networks;
import com.sdd.jborg.Server;
import com.sdd.jborg.Ssh;
import com.sdd.jborg.util.Callback0;
import com.sdd.jborg.util.FileSystem;
import org.reflections.Reflections;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.Base64;
import java.util.Queue;
import java.util.Set;
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
				catch (InstantiationException|IllegalAccessException e)
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

	public static ExecuteParams execute(final String cmd)
	{
		return chainForCb(new ExecuteParams(), p -> {
			// TODO: implement retries
			// TODO: implement expect assertions
			ssh.cmd(p.getSudoCmd() + cmd, (code, out, err) -> {
				if (!empty(p.getTest()))
					p.getTest().call(code, out, err);
			});
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

					if (empty(p.getOwner()) || empty(p.getGroup()))
						chown(path)
							.setOwner(p.getOwner())
							.setGroup(p.getGroup())
							.setSudoCmd(p.getSudoCmd())
							.callImmediate();

					if (empty(p.getMode()))
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
							chmod("$(echo ~" + name + "/.ssh/authorized_keys")
								.setMode("0600")
								.setSudoCmd(p.getSudoCmd())
								.callImmediate();
							execute("echo " + bashEscape(key) + " | sudo tee -a $(echo ~" + name + ")/.ssh/authorized_keys >/dev/null")
								.setSudoCmd(p.getSudoCmd())
								.callImmediate();
							chown("$(echo ~" + name + "/.ssh/")
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

	public static Callback0 remoteFileExists(final String path)
	{
		return () -> {

		};
	}
}
