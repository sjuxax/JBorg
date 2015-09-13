package com.sdd.jborg;

import static com.sdd.jborg.scripts.Standard.*;

public class Main
{
	public static void main(String[] args)
	{
		server.setFqdn(args[1]);

		final Script script = Script.findMatch();
		if (script == null)
		{
			Logger.err("Unable to locate matching script.");
			return;
		}
		script.assimilate(); // loop 1

		switch (args[0].toLowerCase())
		{
			case "assemble":
				server.getDatacenter().getProvider().createVirtualMachine();

				// flow through to assimilate

			case "assimilate":
				Logger.setHost(server.ssh.host);
				ssh = new Ssh().connect(
					server.ssh.host,
					server.ssh.port,
					server.ssh.user,
					server.ssh.key
				);

				go(); // loop 2
				ssh.close();
				Logger.info("Assimilation complete.");
				break;
		}
	}

	public static void die(final Exception reason)
	{
		Logger.err("Aborting. Reason: " + reason.getMessage());
		reason.printStackTrace();
		System.exit(1);
	}
}
