package com.sdd.jborg.scripts.params

import com.sdd.jborg.Ssh

class Standard
{
	private static trait Sudoable
	{
		private String sudo = "";

		public String getSudo() {
			return sudo;
		}

		public void setSudo(final String cmd)
		{
			this.sudo = cmd;
		}

		public void setSudoUser(final String sudoer)
		{
			this.sudo = "sudo -i -u "+ sudoer +" ";
		}

		public void setSudo(final boolean sudo)
		{
			this.sudo = sudo ? "sudo -i " : "";
		}
	}

	public static final class ExecuteParams
		implements Sudoable
	{
		private Ssh.CmdCallback testCb;

		public void setTest(final Ssh.CmdCallback testCb)
		{
			this.testCb = testCb;
		}

		public Ssh.CmdCallback getTest()
		{
			return testCb;
		}
	}

	private static trait Ownable
	{
		private String owner = "";
		private String group = "";

		public String getOwner()
		{
			return owner;
		}

		public void setOwner(final String owner)
		{
			this.owner = owner;
		}

		public String getGroup()
		{
			return group;
		}

		public void setGroup(final String group)
		{
			this.group = group;
		}
	}

	private static trait Recursable
	{
		private boolean recursive = "";

		public boolean getRecursive()
		{
			return recursive;
		}

		public void setRecursive(final boolean recursive)
		{
			this.recursive = recursive;
		}
	}

	private static trait Modeable
	{
		private String mode = "";

		public String getMode()
		{
			return mode;
		}

		public void setMode(String mode)
		{
			this.mode = mode;
		}
	}

	public static final class ChownParams
		implements Sudoable, Ownable, Recursable
	{
	}

	public static final class ChmodParams
		implements Sudoable, Modeable, Recursable
	{
	}

	public static final class DirectoryParams
		implements Sudoable, Ownable, Modeable, Recursable
	{
	}

	public static final class UserParams
		implements Sudoable
	{
		private String comment;
		private String password;
		private String[] sshKeys;
		private String groupName;
		private String[] groups;
		private String shell;

		public String getComment()
		{
			return comment;
		}

		public void setComment(final String comment)
		{
			this.comment = comment;
		}

		public String getPassword()
		{
			return password;
		}

		public void setPassword(final String password)
		{
			this.password = password;
		}

		public String[] getSshKeys()
		{
			return sshKeys;
		}

		public void setSshKeys(final String[] sshKeys)
		{
			this.sshKeys = sshKeys;
		}

		public String getGroupName()
		{
			return groupName;
		}

		public void setGroupName(final String groupName)
		{
			this.groupName = groupName;
		}

		public String[] getGroups()
		{
			return groups;
		}

		public void setGroups(final String[] groups)
		{
			this.groups = groups;
		}

		public String getShell()
		{
			return shell;
		}

		public void setShell(final String shell)
		{
			this.shell = shell;
		}
	}
}
