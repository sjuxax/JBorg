package com.sdd.jborg

import com.sdd.jborg.util.Callback0

/**
 * Standard fields and methods every script should have in scope.
 */
public class Standard
{
	// Global Attributes

	public static final Networks networks = new Networks(CoffeeScript.readCsonFileToJsonObject("networks.coffee"));
	public static final Server server = new Server(); // a.k.a. "locals"
	public static Ssh ssh;

	// Async Flow Control

	private static Queue<Callback0> queue = new ArrayDeque<>();

	public static final int SERIAL = 1;
	public static final int PARALLEL = 2;

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

	/**
	 * methods that may only be called asynchronously
	 * e.g., from within second loop
	 */
	public static void die(final String reason)
	{
		Logger.stderr("Aborting. Reason: "+ reason);
		System.exit(1);
	}

	public static void log(final String msg)
	{
		Logger.info(msg);
	}

	public static String decrypt(final String s)
	{
		return "";
	}

	public static abstract class Params
	{
		private Callback0 callback;

		private void setCallback(final Callback0 callback)
		{
			this.callback = callback;
		}

		private Callback0 getCallback()
		{
			return callback;
		}
	}


	public static final class ExecuteParams
		extends Params
		implements Sudoable
	{
		public static interface ExecuteTestCallback
		{
			void call(final int code, final String out, final String err);
		}

		private ExecuteTestCallback testCb;

		public ExecuteParams setTest(final ExecuteTestCallback testCb)
		{
			this.testCb = testCb;
			return this;
		}

		public ExecuteParams setSudo(final String sudoer)
		{
			_setSudo(sudoer);
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
			ssh.cmd(cmd);
		});
		return p;
	}

	public static final class ChownParams
		extends Params
		implements Sudoable, Ownable
	{
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

		public ChownParams setSudo(final String sudoer)
		{
			_setSudo(sudoer);
			return this;
		}

		public ChownParams setSudo(final boolean sudo)
		{
			_setSudo(sudo);
			return this
		}
	}

	public static ChownParams chown(final Map o = [:], final String path)
	{
		final ChownParams p = new ChownParams();
		if (p.owner == null || p.group == null)
			die "chown owner and group are required."
		p.setCallback({
			execute "chown ${o['owner']}.${o['group']} ${path}"
		});
		return p;
	}

	public static Callback0 chmod(final Map o = [:], final String path)
	{
		return { execute "chmod ${o['mode']} ${path}" }
	}

	private trait Ownable
	{
		public String owner;
		public String group;
	}

	private trait Modeable
	{
		public String mode;
	}

	public static final class DirectoryParams
		extends Params
		implements Sudoable, Ownable, Modeable
	{
		public boolean recursive;

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
		o['mode'] ?: '0755'
		return {
			execute "setTest -d ${path}", test: { code ->
				if (code == 0)
					log 'Skipping existing directory.'
				else
					execute
			}
			execute "mkdir -p ${path}"
		}
	}

	private trait Sudoable
	{
		private String sudo;

		private void _setSudo(final String sudoer)
		{
			this.sudo = "sudo -i -u ${sudoer}"
		}

		private void _setSudo(final boolean sudo)
		{
			this.sudo = sudo ? 'sudo -i' : ''
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

		public UserParams setSudo(final String sudoer)
		{
			_setSudo(sudoer);
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
					(p.comment ? " --comment ${bashEscape p.comment} \\\n" : "")+
					(p.password ? " --password ${bashEscape p.password} \\\n" : "")+
					" --shell ${p.shell ?: "/bin/bash"} \\\n")
					.sudo = p.sudo;
				if (p.groupName != null)
					execute("usermod -g ${p.groupName} ${name}")
						.sudo = p.sudo;
				if (p.groups.length > 0)
					for (final String group : p.groups)
						execute("usermod -a -G ${group} ${name}")
							.sudo = p.sudo;
				if (p.sshKeys.length > 0)
					for (final String key : p.sshKeys)
					{
						directory("\$(echo ~${name})/.ssh/")
							.setRecursive(true)
							.setMode('0700')
							.sudo = p.sudo;
						execute("touch \$(echo ~${name})/.ssh/authorized_keys")
							.sudo = p.sudo;
						chmod("\$(echo ~${name})/.ssh/authorized_keys")
							.setMode('0600')
							.sudo = p.sudo;
						execute("echo ${bashEscape key} | sudo tee -a \$(echo ~${name})/.ssh/authorized_keys >/dev/null")
							.sudo = p.sudo;
						chown("\$(echo ~${name})/.ssh/")
							.setRecursive(true)
							.setOwner(name)
							.setGroup(name)
							.sudo = p.sudo;
					}
			});
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
