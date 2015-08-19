package com.sdd.jborg.scripts;

import static com.sdd.jborg.scripts.params.StandardParams.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sdd.jborg.scripts.Standard.*;

public class Hosts
{
	private static Map<String, List<String>> hosts = new HashMap<>();

	public static final class HostsFileEntryParams extends Params
	{
		private boolean clear = false;
		private boolean override = false;

		public HostsFileEntryParams clear()
		{
			this.clear = true;
			return this;
		}

		public HostsFileEntryParams overrride()
		{
			this.override = true;
			return this;
		}
	}

	public static HostsFileEntryParams hostsFileEntry(final String ip, final String[] hostnames)
	{
		return chainForCb(new HostsFileEntryParams(), p -> {
			// NOTICE: we replace all matching lines in the /etc/hosts file every time this method is invoked,
			// but we also remember, concatenate, and sort unique hostname assignments between calls,
			// so as to have a snowball effect until the final invocation which will have everything.

			if (p.clear)
			{
				hosts.clear();
			}

			final List<String> existingList;
			if (p.override || !hosts.containsKey(ip))
			{
				hosts.put(ip, existingList = new ArrayList<>());
			}
			else
			{
				existingList = hosts.get(ip);
			}

			existingList.addAll(Arrays.asList(hostnames));
			Collections.sort(existingList);
			// TODO: ensure hostname list is unique for this ip

			// remove any lines referring to the same ip; this prevents duplicates
			execute("sed -i '/^"+ ip +"/d' /etc/hosts")
				.setSudo(true)
				.callImmediate();

			// append ip and hostnames
			execute("echo "+ ip +" "+ String.join(" ", existingList) +" | sudo tee -a /etc/hosts >/dev/null")
				.expect(0)
				.callImmediate();
		});
	}

}
