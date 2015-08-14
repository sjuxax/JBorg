package com.sdd.jborg;

import com.sdd.jborg.cloud.Aws;
import static com.sdd.jborg.scripts.Standard.*;

public class Main
{
	public static void main(String[] args)
	{
		server.setFqdn(args[1]);

		switch (args[0].toLowerCase())
		{
			case "assemble":
				create();
				// flow through to assimilate
			case "assimilate":
				final Script script = Script.findMatch();
				if (script == null)
				{
					Logger.err("Unable to locate matching script.");
					return;
				}
				script.assimilate(); // loop 1

				Logger.setHost(server.getObject("ssh").getString("host"));
				ssh = new Ssh().connect(
					server.getObject("ssh").getString("host"),
					server.getObject("ssh").getInteger("port"),
					server.getObject("ssh").getString("user"),
					server.getObject("ssh").getString("key")
				);

				go(); // loop 2
				ssh.close();
				Logger.info("Assimilation complete.");
				break;
		}
	}

	private static void create()
	{
		Aws.create();
	}
}
