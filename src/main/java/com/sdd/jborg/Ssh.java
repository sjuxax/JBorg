package com.sdd.jborg;

import com.sdd.jborg.util.Callback1;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.UserAuthException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class Ssh
{
	private SSHClient ssh;

	public Ssh connect(final String host, final int port, final String user, final String key)
	{
		try
		{
			ssh = new SSHClient();
			// just accept any remote host fingerprint;
			// they will almost always be new to us here
			ssh.addHostKeyVerifier(new PromiscuousVerifier());
			//ssh.loadKnownHosts();
			ssh.connect(host, port);
			try
			{
				ssh.authPublickey(user, System.getProperty("user.home") + File.separator + ".ssh" + File.separator + key);
			}
			catch (UserAuthException | TransportException e)
			{
				e.printStackTrace();
				close();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			close();
		}
		return this;
	}

	public interface CmdCallback
	{
		void call(final int code, final String out, final String err);
	}

	public void cmd(final String command, final CmdCallback cb)
	{
		Session session = null;
		try
		{
			session = ssh.startSession();
			final Session.Command cmd = session.exec(command);
			Logger.stdin(command);
			cmd.join(); // wait indefinitely for remote process to exit
			final String stdOut = IOUtils.readFully(cmd.getInputStream()).toString(StandardCharsets.UTF_8.name());
			if (stdOut.length() > 0)
				Logger.stdout(stdOut);
			final String stdErr = IOUtils.readFully(cmd.getErrorStream()).toString(StandardCharsets.UTF_8.name());
			if (stdErr.length() > 0)
				Logger.stderr(stdErr);
			final int code = cmd.getExitStatus();
			Logger.info("remote process exit code: " + code);
			cb.call(code, stdOut, stdErr);
		}
		catch (ConnectionException e)
		{
			e.printStackTrace();
		}
		catch (TransportException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (session != null)
				{
					session.close();
				}
			}
			catch (TransportException e)
			{
				e.printStackTrace();
			}
			catch (ConnectionException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void put(final Path src, final String dst, final Callback1<IOException> errCallback)
	{
		SFTPClient sftp = null;
		try
		{
			sftp = ssh.newSFTPClient();
			sftp.put(src.toString(), dst);
		}
		catch (IOException e)
		{
			errCallback.call(e);
		}
		finally
		{
			try
			{
				if (sftp != null)
				{
					sftp.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void close()
	{
		try
		{
			ssh.disconnect();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
