package com.sdd.jborg.scripts.params;

import com.sdd.jborg.Ssh;
import com.sdd.jborg.util.Callback0;
import com.sdd.jborg.scripts.Standard.RemoteServerValidationException;
import com.sdd.jborg.util.Func1;

import java.util.Map;

public class StandardParams
{
	public static class Params
	{
		public Callback0 callback;

		public void callImmediate()
		{
			callback.call();
		}
	}

	private static class Sudoable
	{
		private String sudo = "";

		public String getSudoCmd()
		{
			return sudo;
		}

		public void setSudoCmd(final String cmd)
		{
			this.sudo = cmd;
		}

		public void setSudoAsUser(final String sudoer)
		{
			this.sudo = "sudo -i -u " + sudoer + " ";
		}

		public void setSudo(final boolean sudo)
		{
			this.sudo = sudo ? "sudo -i " : "";
		}
	}

	public interface ScriptRemoteTestCallback1
	{
		void call(final int code, final String out, final String err)
			throws RemoteServerValidationException;
	}

	public static final class ExecuteParams extends Params
	{
		private Ssh.CmdCallback testCb;
		private Sudoable sudoable = new Sudoable();
		private int retryTimes;
		private Integer expectCode;
		private boolean ignoreErrors = false;

		public ExecuteParams setTest(final ScriptRemoteTestCallback1 testCb)
		{
			this.testCb = (code, out, err) -> {
				try
				{
					testCb.call(code, out, err);
				}
				catch (final RemoteServerValidationException e)
				{
					com.sdd.jborg.scripts.Standard.notifySkip(e);
				}
			};
			return this;
		}

		public Ssh.CmdCallback getTest()
		{
			return testCb;
		}

		public String getSudoCmd()
		{
			return sudoable.getSudoCmd();
		}

		public ExecuteParams setSudoCmd(final String cmd)
		{
			sudoable.setSudoCmd(cmd);
			return this;
		}

		public ExecuteParams setSudoAsUser(final String sudoer)
		{
			sudoable.setSudoAsUser(sudoer);
			return this;
		}

		public ExecuteParams setSudo(final boolean sudo)
		{
			sudoable.setSudo(sudo);
			return this;
		}

		public int getRetryTimes()
		{
			return this.retryTimes;
		}

		public ExecuteParams setRetry(final int times)
		{
			this.retryTimes = times;
			return this;
		}

		public Integer getExpectCode()
		{
			return expectCode;
		}

		public ExecuteParams expect(final Integer code)
		{
			this.expectCode = code;
			return this;
		}

		public boolean isIgnoringErrors()
		{
			return ignoreErrors;
		}

		public ExecuteParams setIgnoreErrors(final boolean ignoreErrors)
		{
			this.ignoreErrors = ignoreErrors;
			return this;
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

	private static class Comparable
	{
		private boolean compareLocalFile = false;

		private String compareChecksum;

		public boolean getCompareLocalFile()
		{
			return compareLocalFile;
		}

		public void setCompareLocalFile(final boolean compareLocalFile)
		{
			this.compareLocalFile = compareLocalFile;
		}

		public String getCompareChecksum()
		{
			return compareChecksum;
		}

		public void setCompareChecksum(final String compareChecksum)
		{
			this.compareChecksum = compareChecksum;
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

		public void setMode(final String mode)
		{
			this.mode = mode;
		}
	}

	public static final class ChownParams extends Params
	{
		private Sudoable sudoable = new Sudoable();

		public String getSudoCmd()
		{
			return sudoable.getSudoCmd();
		}

		public ChownParams setSudoCmd(final String cmd)
		{
			sudoable.setSudoCmd(cmd);
			return this;
		}

		public ChownParams setSudoAsUser(final String sudoer)
		{
			sudoable.setSudoAsUser(sudoer);
			return this;
		}

		public ChownParams setSudo(final boolean sudo)
		{
			sudoable.setSudo(sudo);
			return this;
		}

		private Ownable ownable = new Ownable();

		public String getOwner()
		{
			return ownable.getOwner();
		}

		public ChownParams setOwner(final String owner)
		{
			ownable.setOwner(owner);
			return this;
		}

		public String getGroup()
		{
			return ownable.getGroup();
		}

		public ChownParams setGroup(final String group)
		{
			ownable.setGroup(group);
			return this;
		}

		private Recursable recursable = new Recursable();

		public boolean getRecursive()
		{
			return recursable.getRecursive();
		}

		public ChownParams setRecursive(final boolean recursive)
		{
			recursable.setRecursive(recursive);
			return this;
		}
	}

	public static final class ChmodParams extends Params
	{
		private Sudoable sudoable = new Sudoable();

		public String getSudoCmd()
		{
			return sudoable.getSudoCmd();
		}

		public ChmodParams setSudoCmd(final String cmd)
		{
			sudoable.setSudoCmd(cmd);
			return this;
		}

		public ChmodParams setSudoAsUser(final String sudoer)
		{
			sudoable.setSudoAsUser(sudoer);
			return this;
		}

		public ChmodParams setSudo(final boolean sudo)
		{
			sudoable.setSudo(sudo);
			return this;
		}

		private Recursable recursable = new Recursable();

		public boolean getRecursive()
		{
			return recursable.getRecursive();
		}

		public ChmodParams setRecursive(final boolean recursive)
		{
			recursable.setRecursive(recursive);
			return this;
		}

		private Modeable modeable = new Modeable();

		public String getMode()
		{
			return modeable.getMode();
		}

		public ChmodParams setMode(final String mode)
		{
			modeable.setMode(mode);
			return this;
		}
	}

	public static final class DirectoryParams extends Params
	{
		private Sudoable sudoable = new Sudoable();

		public String getSudoCmd()
		{
			return sudoable.getSudoCmd();
		}

		public DirectoryParams setSudoCmd(final String cmd)
		{
			sudoable.setSudoCmd(cmd);
			return this;
		}

		public DirectoryParams setSudoAsUser(final String sudoer)
		{
			sudoable.setSudoAsUser(sudoer);
			return this;
		}

		public DirectoryParams setSudo(final boolean sudo)
		{
			sudoable.setSudo(sudo);
			return this;
		}

		private Ownable ownable = new Ownable();

		public String getOwner()
		{
			return ownable.getOwner();
		}

		public DirectoryParams setOwner(final String owner)
		{
			ownable.setOwner(owner);
			return this;
		}

		public String getGroup()
		{
			return ownable.getGroup();
		}

		public DirectoryParams setGroup(final String group)
		{
			ownable.setGroup(group);
			return this;
		}

		private Recursable recursable = new Recursable();

		public boolean getRecursive()
		{
			return recursable.getRecursive();
		}

		public DirectoryParams setRecursive(final boolean recursive)
		{
			recursable.setRecursive(recursive);
			return this;
		}

		private Modeable modeable = new Modeable();

		public String getMode()
		{
			return modeable.getMode();
		}

		public DirectoryParams setMode(final String mode)
		{
			modeable.setMode(mode);
			return this;
		}
	}

	public static final class UserParams extends Params
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

		public UserParams setComment(final String comment)
		{
			this.comment = comment;
			return this;
		}

		public String getPassword()
		{
			return password;
		}

		public UserParams setPassword(final String password)
		{
			this.password = password;
			return this;
		}

		public String[] getSshKeys()
		{
			return sshKeys;
		}

		public UserParams setSshKeys(final String[] sshKeys)
		{
			this.sshKeys = sshKeys;
			return this;
		}

		public String getGroupName()
		{
			return groupName;
		}

		public UserParams setGroupName(final String groupName)
		{
			this.groupName = groupName;
			return this;
		}

		public String[] getGroups()
		{
			return groups;
		}

		public UserParams setGroups(final String[] groups)
		{
			this.groups = groups;
			return this;
		}

		public String getShell()
		{
			return shell;
		}

		public UserParams setShell(final String shell)
		{
			this.shell = shell;
			return this;
		}

		private Sudoable sudoable = new Sudoable();

		public String getSudoCmd()
		{
			return sudoable.getSudoCmd();
		}

		public UserParams setSudoCmd(final String cmd)
		{
			sudoable.setSudoCmd(cmd);
			return this;
		}

		public UserParams setSudoAsUser(final String sudoer)
		{
			sudoable.setSudoAsUser(sudoer);
			return this;
		}

		public UserParams setSudo(final boolean sudo)
		{
			sudoable.setSudo(sudo);
			return this;
		}
	}

	public static final class RemoteFileExistsParams extends Params
	{
		private String path = "";
		private String compareLocalFile;
		private String compareChecksum;
		private Sudoable sudoable = new Sudoable();
		private Callback0 trueCallback;
		private Callback0 falseCallback;

		public String getPath()
		{
			return path;
		}

		public RemoteFileExistsParams setPath(final String path)
		{
			this.path = path;
			return this;
		}

		public String getSudoCmd()
		{
			return sudoable.getSudoCmd();
		}

		public RemoteFileExistsParams setSudoCmd(final String cmd)
		{
			sudoable.setSudoCmd(cmd);
			return this;
		}

		public RemoteFileExistsParams setSudoAsUser(final String sudoer)
		{
			sudoable.setSudoAsUser(sudoer);
			return this;
		}

		public RemoteFileExistsParams invokeTrueCallback()
		{
			if (trueCallback != null)
				trueCallback.call();
			return this;
		}

		public RemoteFileExistsParams setTrueCallback(final Callback0 trueCb)
		{
			this.trueCallback = trueCb;
			return this;
		}

		public RemoteFileExistsParams invokeFalseCallback()
		{
			if (falseCallback != null)
				falseCallback.call();
			return this;
		}

		public RemoteFileExistsParams setFalseCallback(final Callback0 falseCb)
		{
			this.falseCallback = falseCb;
			return this;
		}

		public RemoteFileExistsParams setSudo(final boolean sudo)
		{
			sudoable.setSudo(sudo);
			return this;
		}

		public String getCompareLocalFile()
		{
			return this.compareLocalFile;
		}

		public RemoteFileExistsParams setCompareLocalFile(final String CompareLocalFile)
		{
			this.compareLocalFile = CompareLocalFile;
			return this;
		}

		public String getCompareChecksum()
		{
			return this.compareChecksum;
		}

		public RemoteFileExistsParams setCompareChecksum(final String CompareChecksum)
		{
			this.compareChecksum = CompareChecksum;
			return this;
		}

		private Modeable modeable = new Modeable();

		public String getMode()
		{
			return modeable.getMode();
		}

		public RemoteFileExistsParams setMode(final String mode)
		{
			modeable.setMode(mode);
			return this;
		}
	}

	public final static class ServiceParams extends Params
	{
		private String action = "start";

		public String getAction()
		{
			return action;
		}

		public void setAction(final String action)
		{
			this.action = action;
		}
	}

	public final static class UninstallParams extends Params
	{
		private boolean purge;

		public boolean isPurge()
		{
			return purge;
		}

		public void setPurge(final boolean purge)
		{
			this.purge = purge;
		}
	}

	public final static class DownloadParams extends Params
	{
		private String checksum;

		public String getChecksum()
		{
			return checksum;
		}

		public void setChecksum(final String checksum)
		{
			this.checksum = checksum;
		}

		private Ownable ownable = new Ownable();

		public String getOwner()
		{
			return ownable.getOwner();
		}

		public DownloadParams setOwner(final String owner)
		{
			ownable.setOwner(owner);
			return this;
		}

		public String getGroup()
		{
			return ownable.getGroup();
		}

		public DownloadParams setGroup(final String group)
		{
			ownable.setGroup(group);
			return this;
		}

		private Modeable modeable = new Modeable();

		public String getMode()
		{
			return modeable.getMode();
		}

		private Sudoable sudoable = new Sudoable();

		public String getSudoCmd()
		{
			return sudoable.getSudoCmd();
		}

		public DownloadParams setSudoCmd(final String cmd)
		{
			sudoable.setSudoCmd(cmd);
			return this;
		}

		public DownloadParams setSudoAsUser(final String sudoer)
		{
			sudoable.setSudoAsUser(sudoer);
			return this;
		}

		public DownloadParams setSudo(final boolean sudo)
		{
			sudoable.setSudo(sudo);
			return this;
		}
	}

	public final static class DeployParams extends Params
	{
		private String deployTo;

		public DeployParams setDeployTo(final String deployTo)
		{
			this.deployTo = deployTo;
			return this;
		}

		public final static class GitParams
		{
			private String repo;
			private String branch;
			private String deployKey;

			public String getRepo()
			{
				return repo;
			}

			public GitParams setRepo(final String repo)
			{
				this.repo = repo;
				return this;
			}

			public String getBranch()
			{
				return branch;
			}

			public GitParams setBranch(final String branch)
			{
				this.branch = branch;
				return this;
			}

			public String getDeployKey()
			{
				return deployKey;
			}

			public GitParams setDeployKey(final String deployKey)
			{
				this.deployKey = deployKey;
				return this;
			}
		}

		private GitParams gitParams;

		public GitParams getGit()
		{
			return gitParams;
		}

		public DeployParams setGit(final Func1<GitParams, GitParams> gitParamsCallback)
		{
			this.gitParams = gitParamsCallback.call(new GitParams());
			return this;
		}

		private int keepReleases = 3; // default

		public int getKeepReleases()
		{
			return keepReleases;
		}

		public DeployParams setKeepReleases(final int amount)
		{
			this.keepReleases = amount;
			return this;
		}

		private Sudoable sudoable = new Sudoable();

		public String getSudoCmd()
		{
			return sudoable.getSudoCmd();
		}

		public DeployParams setSudoCmd(final String cmd)
		{
			sudoable.setSudoCmd(cmd);
			return this;
		}

		public DeployParams setSudoAsUser(final String sudoer)
		{
			sudoable.setSudoAsUser(sudoer);
			return this;
		}

		public DeployParams setSudo(final boolean sudo)
		{
			sudoable.setSudo(sudo);
			return this;
		}

		private Ownable ownable = new Ownable();

		public String getOwner()
		{
			return ownable.getOwner();
		}

		public DeployParams setOwner(final String owner)
		{
			ownable.setOwner(owner);
			return this;
		}

		public String getGroup()
		{
			return ownable.getGroup();
		}

		public DeployParams setGroup(final String group)
		{
			ownable.setGroup(group);
			return this;
		}
	}

	public static final class TemplateParams extends Params
	{
		private String to;

		public String getTo()
		{
			return to;
		}

		public TemplateParams setTo(final String to)
		{
			this.to = to;
			return this;
		}

		private String content;

		public String getContent()
		{
			return content;
		}

		public TemplateParams setContent(final String content)
		{
			this.content = content;
			return this;
		}

		private Map variables;

		public Map getVariables()
		{
			return variables;
		}

		public TemplateParams setVariables(Map variables)
		{
			this.variables = variables;
			return this;
		}

		private Sudoable sudoable = new Sudoable();

		public String getSudoCmd()
		{
			return sudoable.getSudoCmd();
		}

		public TemplateParams setSudoCmd(final String cmd)
		{
			sudoable.setSudoCmd(cmd);
			return this;
		}

		public TemplateParams setSudoAsUser(final String sudoer)
		{
			sudoable.setSudoAsUser(sudoer);
			return this;
		}

		public TemplateParams setSudo(final boolean sudo)
		{
			sudoable.setSudo(sudo);
			return this;
		}

		private Ownable ownable = new Ownable();

		public String getOwner()
		{
			return ownable.getOwner();
		}

		public TemplateParams setOwner(final String owner)
		{
			ownable.setOwner(owner);
			return this;
		}

		public String getGroup()
		{
			return ownable.getGroup();
		}

		public TemplateParams setGroup(final String group)
		{
			ownable.setGroup(group);
			return this;
		}

		private Modeable modeable = new Modeable();

		public String getMode()
		{
			return modeable.getMode();
		}

		public TemplateParams setMode(final String mode)
		{
			modeable.setMode(mode);
			return this;
		}
	}

	public static final class UploadParams extends Params
	{
		private String to;

		public String getTo()
		{
			return to;
		}

		public UploadParams setTo(final String to)
		{
			this.to = to;
			return this;
		}

		private String finalTo;

		public String getFinalTo()
		{
			return finalTo;
		}

		public UploadParams setFinalTo(final String finalTo)
		{
			this.finalTo = finalTo;
			return this;
		}

		private boolean encrypted;

		public boolean isEncrypted()
		{
			return encrypted;
		}

		public UploadParams isEncrypted(final boolean encrypted)
		{
			this.encrypted = encrypted;
			return this;
		}

		private String content;

		public String getContent()
		{
			return content;
		}

		public UploadParams setContent(final String content)
		{
			this.content = content;
			return this;
		}

		private Map variables;

		public Map getVariables()
		{
			return variables;
		}

		public UploadParams setVariables(final Map variables)
		{
			this.variables = variables;
			return this;
		}

		private Sudoable sudoable = new Sudoable();

		public String getSudoCmd()
		{
			return sudoable.getSudoCmd();
		}

		public UploadParams setSudoCmd(final String cmd)
		{
			sudoable.setSudoCmd(cmd);
			return this;
		}

		public UploadParams setSudoAsUser(final String sudoer)
		{
			sudoable.setSudoAsUser(sudoer);
			return this;
		}

		public UploadParams setSudo(final boolean sudo)
		{
			sudoable.setSudo(sudo);
			return this;
		}

		private Ownable ownable = new Ownable();

		public String getOwner()
		{
			return ownable.getOwner();
		}

		public UploadParams setOwner(final String owner)
		{
			ownable.setOwner(owner);
			return this;
		}

		public String getGroup()
		{
			return ownable.getGroup();
		}

		public UploadParams setGroup(final String group)
		{
			ownable.setGroup(group);
			return this;
		}

		private Modeable modeable = new Modeable();

		public String getMode()
		{
			return modeable.getMode();
		}

		public UploadParams setMode(final String mode)
		{
			modeable.setMode(mode);
			return this;
		}
	}
}