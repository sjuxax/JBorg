package com.sdd.jborg;

import com.sdd.jborg.cloud.Aws;

public class Main
{
	public static final Networks networks = new Networks(CoffeeScript.readCsonFileToJsonObject("networks.coffee"));
	public static final Server server = new Server(); // a.k.a. "locals"
	public static Ssh ssh;

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

				ssh = new Ssh();
				ssh.connect(
					networks.getSshHost(),
					networks.getSshPort(),
					networks.getSshUser(),
					networks.getSshKey()
				);
				//ssh.cmd("ping -c 1 google.com");

				AsyncFlowControl.go(); // loop 2
				break;
		}
	}

	private static void create()
	{
		Aws.create();
	}
}
