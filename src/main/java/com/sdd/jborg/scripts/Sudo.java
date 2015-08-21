package com.sdd.jborg.scripts;

import com.sdd.jborg.util.JsonArray;
import com.sdd.jborg.util.JsonObject;

import static com.sdd.jborg.scripts.Standard.*;

public final class Sudo
	implements Includable
{
	@Override
	public void included()
	{
		server.setDefault("sudo", new JsonObject()
			.put("defaults", new JsonArray()
				.put("!lecture,tty_tickets,!fqdn"))
			.put("users", new JsonArray())
			.put("groups", new JsonArray("sysadmin"))
			.put("passwordless", false)
			.put("include_sudoers_d", false)
			.put("agent_forwarding", false)
			.put("command_aliases", new JsonArray())
			.put("prefix", "/etc"));

		then(install("sudo"));

		then(template("sudoers")
			.setTo(server.getObject("sudo").getString("prefix") + "/sudoers")
			.setOwner("root")
			.setGroup("root")
			.setSudo(true)
			.setMode("0440"));
	}
}
