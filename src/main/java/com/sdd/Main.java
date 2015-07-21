package com.sdd;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;

import javax.script.ScriptException;
import java.io.IOException;
import java.io.File;
import java.util.concurrent.TimeUnit;

public class Main
{
	public static void main(String[] args) throws IOException
	{
		System.out.println("Java can do " + ANSIColor.YELLOW + "colors" + ANSIColor.RESET + ", too! :)");


		final SSHClient ssh = new SSHClient();
		ssh.loadKnownHosts();
		ssh.connect("smullin.org", 22);
		try
		{
			ssh.authPublickey("msmullin", System.getProperty("user.home") + File.separator + ".ssh" + File.separator + "id_rsa");
			final Session session = ssh.startSession();
			try
			{
				final Command cmd = session.exec("ping -c 1 google.com");
				System.out.println(IOUtils.readFully(cmd.getInputStream()).toString());
				cmd.join(5, TimeUnit.SECONDS);
				System.out.println("\n** exit status: " + cmd.getExitStatus());
			}
			finally
			{
				session.close();
			}
		}
		finally
		{
			ssh.disconnect();
		}

		System.out.println("Java can connect over SSH, too!");


		CoffeeSipper cs = new CoffeeSipper();
		try
		{
			String s = cs.toJs("a = b: c: 1");
			System.out.println(s);
			System.out.println("Java can compile CSON, too!");
		}
		catch (ScriptException e)
		{
			System.out.println(e.toString());
		}
	}
}
