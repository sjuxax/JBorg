package com.sdd.jborg.scripts.params;

import com.sdd.jborg.Ssh;

public class Standard
{
	private static class Sudoable
	{
		private String sudo = "";

		public String getSudo()
		{
			return sudo;
		}

		public void setSudo(final String cmd)
		{
			this.sudo = cmd;
		}

		public void setSudoUser(final String sudoer)
		{
			this.sudo = "sudo -i -u " + sudoer + " ";
		}

		public void setSudo(final boolean sudo)
		{
			this.sudo = sudo ? "sudo -i " : "";
		}
	}

	public static final class ExecuteParams
	{
		private Ssh.CmdCallback testCb;
		private Sudoable sudoable = new Sudoable();

		public void setTest(final Ssh.CmdCallback testCb)
		{
			this.testCb = testCb;
		}

		public Ssh.CmdCallback getTest()
		{
			return testCb;
		}

		public String getSudo()
		{
			return sudoable.getSudo();
		}

		public void setSudo(final String cmd)
		{
			sudoable.setSudo(cmd);
		}

		public void setSudoUser(final String sudoer)
		{
			sudoable.setSudoUser(sudoer);
		}

		public void setSudo(final boolean sudo)
		{
			sudoable.setSudo(sudo);
		}
	}

	private static class Ownable
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

	private static class Recursable
	{
		private boolean recursive = false;

		public boolean getRecursive()
		{
			return recursive;
		}

		public void setRecursive(final boolean recursive)
		{
			this.recursive = recursive;
		}
	}

	private static class Modeable
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
	{
		private Sudoable sudoable = new Sudoable();

		public String getSudo()
		{
			return sudoable.getSudo();
		}

		public void setSudo(final String cmd)
		{
			sudoable.setSudo(cmd);
		}

		public void setSudoUser(final String sudoer)
		{
			sudoable.setSudoUser(sudoer);
		}

		public void setSudo(final boolean sudo)
		{
			sudoable.setSudo(sudo);
		}

		private Ownable ownable = new Ownable();

		public String getOwner()
		{
			return ownable.getOwner();
		}

		public void setOwner(final String owner)
		{
			ownable.setOwner(owner);
		}

		public String getGroup()
		{
			return ownable.getGroup();
		}

		public void setGroup(final String group)
		{
			ownable.setGroup(group);
		}

		private Recursable recursable = new Recursable();

		public boolean getRecursive()
		{
			return recursable.getRecursive();
		}

		public void setRecursive(final boolean recursive)
		{
			recursable.setRecursive(recursive);
		}
	}

	public static final class ChmodParams
	{
		private Sudoable sudoable = new Sudoable();

		public String getSudo()
		{
			return sudoable.getSudo();
		}

		public void setSudo(final String cmd)
		{
			sudoable.setSudo(cmd);
		}

		public void setSudoUser(final String sudoer)
		{
			sudoable.setSudoUser(sudoer);
		}

		public void setSudo(final boolean sudo)
		{
			sudoable.setSudo(sudo);
		}

		private Recursable recursable = new Recursable();

		public boolean getRecursive()
		{
			return recursable.getRecursive();
		}

		public void setRecursive(final boolean recursive)
		{
			recursable.setRecursive(recursive);
		}

		private Modeable modeable = new Modeable();

		public String getMode()
		{
			return modeable.getMode();
		}

		public void setMode(String mode)
		{
			modeable.setMode(mode);
		}
	}

	public static final class DirectoryParams
	{
		private Sudoable sudoable = new Sudoable();

		public String getSudo()
		{
			return sudoable.getSudo();
		}

		public void setSudo(final String cmd)
		{
			sudoable.setSudo(cmd);
		}

		public void setSudoUser(final String sudoer)
		{
			sudoable.setSudoUser(sudoer);
		}

		public void setSudo(final boolean sudo)
		{
			sudoable.setSudo(sudo);
		}

		private Ownable ownable = new Ownable();

		public String getOwner()
		{
			return ownable.getOwner();
		}

		public void setOwner(final String owner)
		{
			ownable.setOwner(owner);
		}

		public String getGroup()
		{
			return ownable.getGroup();
		}

		public void setGroup(final String group)
		{
			ownable.setGroup(group);
		}

		private Recursable recursable = new Recursable();

		public boolean getRecursive()
		{
			return recursable.getRecursive();
		}

		public void setRecursive(final boolean recursive)
		{
			recursable.setRecursive(recursive);
		}

		private Modeable modeable = new Modeable();

		public String getMode()
		{
			return modeable.getMode();
		}

		public void setMode(String mode)
		{
			modeable.setMode(mode);
		}
	}

	public static final class UserParams
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

		private Sudoable sudoable = new Sudoable();

		public String getSudo()
		{
			return sudoable.getSudo();
		}

		public void setSudo(final String cmd)
		{
			sudoable.setSudo(cmd);
		}

		public void setSudoUser(final String sudoer)
		{
			sudoable.setSudoUser(sudoer);
		}

		public void setSudo(final boolean sudo)
		{
			sudoable.setSudo(sudo);
		}
	}
}