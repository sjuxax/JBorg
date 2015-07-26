package com.sdd.jborg.scripts;

import com.sdd.jborg.CoffeeScript;
import com.sdd.jborg.Logger;
import com.sdd.jborg.Networks;
import com.sdd.jborg.Server;
import com.sdd.jborg.Ssh;
import com.sdd.jborg.util.Callback0;
import com.sdd.jborg.util.Callback1;
import com.sdd.jborg.util.FileSystem;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sdd.jborg.scripts.params.Standard.*;

/**
 * Standard fields and methods every script should have in scope.
 */
public class Standard
{
	// Global Attributes

	public static final Networks networks = new Networks(CoffeeScript.readCsonFileToJsonObject("networks.coffee"));
	//public static final Networks networks = new Networks();
	public static final Server server = new Server(); // a.k.a. "locals"
	public static Ssh ssh;

	// Async Flow Control

	private static Queue<Callback0> queue = new ArrayDeque<>();

	public static void then(final Callback0 cb)
	{
		queue.add(cb);
	}

	public static void go()
	{
		while (queue.size() > 0)
		{
			queue.poll().call();
		}
	}

	private static final Pattern BASH_PATTERN = Pattern.compile("([^0-9a-z-])", Pattern.CASE_INSENSITIVE);
	public static String bashEscape(final String cmd)
	{
		final Matcher matcher = BASH_PATTERN.matcher(cmd);
		return matcher.replaceAll("\\$1");
	}


	// methods called once connected

	public static void die(final String reason)
	{
		Logger.stderr("Aborting. Reason: " + reason);
		System.exit(1);
	}

	public static void log(final String msg)
	{
		Logger.info(msg);
	}


	// encryption
	private static final String CIPHER_TYPE = "AES/CBC/PKCS5Padding";
	private static final SecretKey secret = readSecret();
	private static final IvParameterSpec iv = new IvParameterSpec(new byte[16]);
	private static SecretKey readSecret()
	{
		try {
			return new SecretKeySpec(MessageDigest.getInstance("SHA-256")
				.digest(FileSystem.readFileToBytes("secret")), "AES");
		} catch (NoSuchAlgorithmException e) {
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

	public static Callback0 nope(final String reason) {
		Logger.stderr(reason);
		return () -> {};
	}

	public static Callback0 execute(final String cmd, final Callback1<ExecuteParams> paramsCb)
	{
		final ExecuteParams p = new ExecuteParams();
		paramsCb.call(p);
		return () -> {
			// TODO: implement retries
			// TODO: implement expect assertions
			ssh.cmd(p.getSudo() + cmd, (code, out, err) -> {
				if (!empty(p.getTest()))
					p.getTest().call(code, out, err);
			});
		};
	}

	public static Callback0 chown(final String path, final Callback1<ChownParams> paramsCb)
	{
		final ChownParams p = new ChownParams();
		paramsCb.call(p);
		if (empty(p.getOwner()) || empty(p.getGroup()))
			die("chown owner and group are required.");
		return () -> {
			execute("chown " +
					(p.getRecursive() ? "-R " : "") +
					p.getOwner() +
					"." + p.getGroup() +
					" " + path,
				a -> {
					a.setSudo(p.getSudo());
				}).call();
		};
	}

	public static Callback0 chmod(final String path, final Callback1<ChmodParams> paramsCb)
	{
		final ChmodParams p = new ChmodParams();
		paramsCb.call(p);
		if (empty(p.getMode()))
			return nope("mode is required.");
		return () -> {
			execute("chmod " +
					p.getMode() +
					" " + path,
				a -> {
					a.setSudo(p.getSudo());
				}).call();
		};
	}

	public static Callback0 directory(final String path, final Callback1<DirectoryParams> paramsCb)
	{
		final DirectoryParams p = new DirectoryParams();
		paramsCb.call(p);
		if (empty(p.getMode()))
			p.setMode("0755");
		return () -> {
			execute("test -d "+ path, a -> {
				a.setTest((code, out, err) -> {
					if (code == 0)
						log("Skipping existing directory.");
					else
						execute("mkdir " + (p.getRecursive() ? " -p" : "") + " " + path, a2 -> {
							a2.setSudo(p.getSudo());
						}).call();
					if (empty(p.getOwner()) || empty(p.getGroup()))
						chown(path, a3 -> {
							a3.setOwner(p.getOwner());
							a3.setGroup(p.getGroup());
							a3.setSudo(p.getSudo());
						}).call();
					if (empty(p.getMode()))
						chmod(path, a4 -> {
							a4.setMode(p.getMode());
							a4.setSudo(p.getSudo());
						}).call();
				});
			}).call();
		};
	}

	public static Callback0 user(final String name, final Callback1<UserParams> paramsCb)
	{
		final UserParams p = new UserParams();
		paramsCb.call(p);
		return () -> {
			execute("id "+name, a -> {
				a.setTest((code, out, err) -> {
					if (code == 0)
					{
						log("user "+ name +" exists.");
						return;
					}
					execute(
						"useradd "+ name +" \\\n" +
						"  --create-home \\\n" +
						"  --user-group \\\n" +
						(!empty(p.getComment()) ? "  --comment " + bashEscape(p.getComment()) + " \\\n" : "") +
						(!empty(p.getPassword()) ? "  --password " + bashEscape(p.getPassword()) + " \\\n" : "") +
						("  --shell " + (empty(p.getShell()) ? "/bin/bash" : "")),
						a2 -> {
							a2.setSudo(p.getSudo());
						}).call();
					if (!empty(p.getGroupName()))
						execute("usermod -g "+ p.getGroupName() + " "+ name, a3 -> {
							a3.setSudo(p.getSudo());
						}).call();
					if (!empty(p.getGroups()))
						for (final String group : p.getGroups())
							execute("usermod -a -G "+ group +" "+ name, a4 -> {
								a4.setSudo(p.getSudo());
							}).call();
					if (!empty(p.getSshKeys()))
						for (final String key : p.getSshKeys())
						{
							directory("$(echo ~" + name + ")/.ssh/", a5 -> {
								a5.setRecursive(true);
								a5.setMode("0700");
								a5.setSudo(p.getSudo());
							}).call();
							execute("touch $(echo ~"+ name +")/.ssh/authorized_keys", a6 -> {
								a6.setSudo(p.getSudo());
							}).call();
							chmod("$(echo ~"+ name +"/.ssh/authorized_keys", a7 -> {
								a7.setMode("0600");
								a7.setSudo(p.getSudo());
							}).call();
							execute("echo "+ bashEscape(key) +" | sudo tee -a $(echo ~"+ name +")/.ssh/authorized_keys >/dev/null", a8 -> {
								a8.setSudo(p.getSudo());
							}).call();
							chown("$(echo ~" + name + "/.ssh/", a9 -> {
								a9.setRecursive(true);
								a9.setOwner(name);
								a9.setGroup(name);
								a9.setSudo(p.getSudo());
							}).call();
						}
				});
			}).call();
		};
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
