package com.sdd.jborg.scripts;

import com.sdd.jborg.util.JsonObject;

import static com.sdd.jborg.scripts.params.StandardParams.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sdd.jborg.scripts.Standard.*;

public final class Sudo
	implements Includable
{
	@Override
	public void included()
	{
		server.putDefaults("sudo", new JsonObject()
				.put("groups", new JsonObject.JsonArray("sysadmin"))
				.put("users", new JsonObject.JsonArray())
				.put("passwordless", false)
				.put("include_sudoers_d", false)
				.put("agent_forwarding", false)
				.put("defaults", new JsonObject.JsonArray()
						.put("!lecture,tty_tickets,!fqdn")
						.put("env_reset")
						.put("secure_path=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin")
				)
				.put("command_aliases", new JsonObject.JsonArray())
				.put("prefix", "/etc")
		);

//	@default sudo:
//	    groups: ['sysadmin']
//		users: []
//		passwordless: false
//		include_sudoers_d: false
//		agent_forwarding: false
//		defaults: ['!lecture,tty_tickets,!fqdn']
//		command_aliases: []
//		prefix: '/etc'


//		@import __dirname, 'attributes', 'default'
//
//		@then @install 'sudo'
//
//		@then @template [__dirname, 'templates', 'default', 'sudoers'],
//		to: "#{@server.sudo.prefix}/sudoers"
//		owner: 'root'
//		group: 'root'
//		sudo: true
//		mode: '0440'

	}
}
