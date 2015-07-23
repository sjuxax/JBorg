package com.sdd.jborg;

import com.sdd.jborg.util.Logger;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.userauth.UserAuthException;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Ssh
{
	private Session session;

	public void connect(final String host, final int port, final String user, final String key)
	{
		try
		{
			final SSHClient ssh = new SSHClient();
			ssh.loadKnownHosts();
			ssh.connect(host, port);
			try
			{
				ssh.authPublickey(user, System.getProperty("user.home") + File.separator + ".ssh" + File.separator + key);
				session = ssh.startSession();
			}
			finally
			{
				ssh.disconnect();
			}
		}
		catch (UserAuthException e)
		{
			e.printStackTrace();
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
	}

	public void cmd(final String command)
	{
		try
		{
			final Session.Command cmd = session.exec(command);
			Logger.out(IOUtils.readFully(cmd.getInputStream()).toString());
			cmd.join(5, TimeUnit.SECONDS);
			Logger.out("\n** exit status: " + cmd.getExitStatus());
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
			close();
		}
	}

	public void close()
	{
		try
		{
			session.close();
		}
		catch( ConnectionException|TransportException e)
		{
		}
	}
}
