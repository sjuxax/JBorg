package com.sdd.jborg;

import com.sdd.jborg.cloud.Aws;
import com.sdd.jborg.util.JsonObject;

public class Main
{
	public static final JsonObject networks = CoffeeScript.readCsonFileToJsonObject("networks.coffee");
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
				final JsonObject networksSsh = networks.getObject("ssh");
				ssh.connect(
					networksSsh.getString("host"),
					networksSsh.getInteger("port"),
					networksSsh.getString("user"),
					networksSsh.getString("key")
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
