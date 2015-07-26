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
				//create();
				// flow through to assimilate
			case "assimilate":
				final Script script = Script.findMatch();
				script.assimilate(); // loop 1

				Logger.setHost(networks.getSshHost());
				ssh = new Ssh();
				ssh.connect(
					networks.getSshHost(),
					networks.getSshPort(),
					networks.getSshUser(),
					networks.getSshKey()
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
