package com.sdd.jborg

import com.sdd.jborg.util.Callback0
import com.sdd.jborg.util.FileSystem;

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.regex.Matcher
import java.util.regex.Pattern

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

	public static void then(final Params params)
	{
		queue.add(params.getCallback());
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
		matcher.replaceAll('\\$1');
	}


	// methods called once connected

	public static void die(final String reason)
	{
		Logger.stderr("Aborting. Reason: "+ reason);
		System.exit(1);
	}

	public static void log(final String msg)
	{
		Logger.info(msg);
	}


	// encryption
	private static final String CIPHER_TYPE = "AES/CBC/PKCS5Padding";
	private static final SecretKey secret = readSecret();
	private static final IvParameterSpec iv = readIv();
	private static SecretKey readSecret()
	{
		try {
			return new SecretKeySpec(MessageDigest.getInstance("SHA-256")
				.digest(FileSystem.readFileToBytes("secret")), "AES");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static IvParameterSpec readIv()
	{
		try {
			return new IvParameterSpec(new byte[16]);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
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

	public static abstract class Params
	{
		private Callback0 callback;

		public void setCallback(final Callback0 callback)
		{
			this.callback = callback;
		}

		public Callback0 getCallback()
		{
			return callback;
		}
	}

	public static final class ExecuteParams
		extends Params
		implements Sudoable
	{
		private Ssh.CmdCallback testCb;

		public ExecuteParams setTest(final Ssh.CmdCallback testCb)
		{
			this.testCb = testCb;
			return this;
		}

		public ExecuteParams setSudo(final String cmd)
		{
			_setSudo(cmd);
			return this;
		}

		public ExecuteParams setSudoUser(final String sudoer)
		{
			_setSudoUser(sudoer);
			return this;
		}

		public ExecuteParams setSudo(final boolean sudo)
		{
			_setSudo(sudo);
			return this
		}
	}

	public static ExecuteParams execute(final String cmd)
	{
		final ExecuteParams p = new ExecuteParams();
		p.setCallback({
			// TODO: implement retries
			// TODO: implement expect assertions
			ssh.cmd(p.sudo + cmd, { code, out, err ->
				if (p.testCb != null)
					p.testCb.call(code, out, err);
			});
		});
		return p;
	}

	public static final class ChownParams
		extends Params
		implements Sudoable
	{
		public String owner;
		public String group;
		private boolean recursive;

		public ChownParams setRecursive(final boolean recursive)
		{
			this.recursive = recursive;
			return this;
		}

		public ChownParams setOwner(final String owner)
		{
			this.owner = owner;
			return this;
		}

		public ChownParams setGroup(final String group)
		{
			this.group = group;
			return this;
		}

		public ChownParams setSudo(final String cmd)
		{
			_setSudo(cmd);
			return this;
		}

		public ChownParams setSudoUser(final String sudoer)
		{
			_setSudoUser(sudoer);
			return this;
		}

		public ChownParams setSudo(final boolean sudo)
		{
			_setSudo(sudo);
			return this
		}
	}

	public static ChownParams chown(final String path)
	{
		final ChownParams p = new ChownParams();
		if (p.owner == null || p.group == null)
			die("chown owner and group are required.");
		p.setCallback({
			execute("chown "+
				"${p.recursive ? "-R " : ""}"+
				"${p.mode}"+
				" ${path}")
				.setSudo(p.sudo).callback.call();
		});
		return p;
	}

	public static final class ChmodParams
		extends Params
		implements Sudoable
	{
		public String mode;

		public ChownParams setMode(final String mode)
		{
			this.mode = mode;
			return this;
		}

		public ChownParams setSudo(final String cmd)
		{
			_setSudo(cmd);
			return this;
		}

		public ChownParams setSudoUser(final String sudoer)
		{
			_setSudoUser(sudoer);
			return this;
		}

		public ChownParams setSudo(final boolean sudo)
		{
			_setSudo(sudo);
			return this
		}
	}

	public static ChmodParams chmod(final String path)
	{
		final ChmodParams p = new ChmodParams();
		if (p.mode == null)
		{
			die("mode is required.");
			return null;
		}
		p.setCallback({
			execute("chmod "+
				"${p.mode}"+
				" ${path}")
				.setSudo(p.sudo).callback.call();
		});
		return p;
	}

	public static final class DirectoryParams
		extends Params
		implements Sudoable
	{
		public boolean recursive;
		public String owner;
		public String group;
		public String mode;

		public DirectoryParams setOwner(final String owner)
		{
			this.owner = owner
			return this
		}

		public DirectoryParams setGroup(final String group)
		{
			this.group = group
			return this
		}

		public DirectoryParams setMode(final String mode)
		{
			this.mode = mode
			return this
		}

		public DirectoryParams setSudo(final String sudoer)
		{
			_setSudo(sudoer);
			return this;
		}

		public DirectoryParams setSudo(final boolean sudo)
		{
			_setSudo(sudo);
			return this
		}

		public DirectoryParams setRecursive(final boolean recursive)
		{
			this.recursive = true;
			return this;
		}
	}

	public static DirectoryParams directory(final String path)
	{
		final DirectoryParams p = new DirectoryParams();
		p.mode ?: '0755'
		p.setCallback({
			execute("test -d ${path}").setTest({ code, out, err ->
				if (code == 0)
				{
					log("Skipping existing directory.");
					return;
				}
				else
				{
					execute("mkdir ${p.recursive ? " -p" : ""} ${path}")
						.setSudo(p.sudo).callback.call();
				}
				if (p.owner != null || p.group != null)
					chown(path)
						.setOwner(p.owner)
						.setGroup(p.group)
						.setSudo(p.sudo).callback.call();
				if (p.mode != null)
					chmod(path)
						.setMode(p.mode)
						.setSudo(p.sudo).callback.call();
			}).callback.call();
		});
		return p;
	}

	private trait Sudoable
	{
		private String sudo = "";

		public String getSudo()
		{
			return sudo;
		}

		public void _setSudo(final String cmd)
		{
			this.sudo = cmd;
		}

		public void _setSudoUser(final String sudoer)
		{
			this.sudo = "sudo -i -u ${sudoer} "
		}

		public void _setSudo(final boolean sudo)
		{
			this.sudo = sudo ? 'sudo -i ' : ''
		}
	}

	public static final class UserParams
		extends Params
		implements Sudoable
	{
		public String comment;
		public String password;
		public String[] sshKeys;
		public String groupName;
		public String[] groups;
		public String shell;

		public UserParams setComment(final String comment)
		{
			this.comment = comment;
			return this;
		}

		public UserParams setPassword(final String password)
		{
			this.password = password;
			return this;
		}

		public UserParams setSshKeys(final String[] sshKeys)
		{
			this.sshKeys = sshKeys;
			return this;
		}

		public UserParams setGroupName(final String groupName)
		{
			this.groupName = groupName;
			return this;
		}

		public UserParams setGroups(final String[] groups)
		{
			this.groups = groups;
			return this;
		}

		public UserParams setShell(final String shell)
		{
			this.shell = shell;
			return this;
		}

		public UserParams setSudo(final String cmd)
		{
			_setSudo(cmd);
			return this;
		}

		public UserParams setSudoUser(final String sudoer)
		{
			_setSudoUser(sudoer);
			return this;
		}

		public UserParams setSudo(final boolean sudo)
		{
			_setSudo(sudo);
			return this
		}
	}

	public static UserParams user(final String name)
	{
		final UserParams p = new UserParams();
		p.setCallback({
			execute("id ${name}").setTest({ code, out, err ->
				if (code == 0)
				{
					log("user ${name} exists.")
					return;
				}
				execute("useradd ${name} \\\n"+
					"  --create-home \\\n"+
					"  --user-group \\\n"+
					(p.comment ? " --comment "+ bashEscape(p.comment) +" \\\n" : "")+
					(p.password ? " --password "+ bashEscape(p.password) +" \\\n" : "")+
					" --shell ${p.shell ?: "/bin/bash"} \\\n")
					.setSudo(p.getSudo()).callback.call();
				if (p.groupName != null)
					execute("usermod -g ${p.groupName} ${name}")
						.setSudo(p.getSudo()).callback.call();
				if (p.groups != null && p.groups.length > 0)
					for (final String group : p.groups)
						execute("usermod -a -G ${group} ${name}")
							.setSudo(p.getSudo()).callback.call();
				if (p.sshKeys != null && p.sshKeys.length > 0)
					for (final String key : p.sshKeys)
					{
						directory("\$(echo ~${name})/.ssh/")
							.setRecursive(true)
							.setMode('0700')
							.setSudo(p.getSudo()).callback.call();
						execute("touch \$(echo ~${name})/.ssh/authorized_keys")
							.setSudo(p.getSudo()).callback.call();
						chmod("\$(echo ~${name})/.ssh/authorized_keys")
							.setMode('0600')
							.setSudo(p.getSudo()).callback.call();
						execute("echo "+ bashEscape(key) +" | sudo tee -a \$(echo ~${name})/.ssh/authorized_keys >/dev/null")
							.setSudo(p.getSudo()).callback.call();
						chown("\$(echo ~${name})/.ssh/")
							.setRecursive(true)
							.setOwner(name)
							.setGroup(name)
							.setSudo(p.getSudo()).callback.call();
					}
			}).callback.call();
		});
		return p;
	}

	public static Callback0 template(final String path)
	{
		return {

		};
	}

	public static Callback0 upload(final String path)
	{
		return {

		};
	}

	public static Callback0 remoteFileExists(final String path)
	{
		return {

		};
	}
}
