package com.sdd.jborg;

import com.sdd.jborg.util.JsonObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Server
	extends JsonObject
{
	private static final Pattern FQDN_PATTERN = Pattern.compile(
		"^(test-)?([a-z]{2,3}-[a-z]{2,3})-([a-z]{1,5})-([a-z-]+)(\\d{2,4})(?:-([a-z]+))?(?:\\.(\\w+\\.[a-z]{2,3}))$",
		Pattern.CASE_INSENSITIVE);

	public Server setFqdn(final String fqdn)
	{
		put("fqdn", fqdn);
		final Matcher m = FQDN_PATTERN.matcher(fqdn);
		if (m.matches())
		{
			put("datacenter", m.group(2));
			put("env", m.group(3));
			put("type", m.group(4));
			put("instance", m.group(5));
			put("subproject", m.group(6));
			put("tld", m.group(7));
		}

		return this;
	}

	public String getHostname() {
		return getString("datacenter") + "-" +
			getString("env") + "-" +
			getString("type") +
			getString("instance") +
			(getString("subproject") != null ? "-" + getString("subproject") : "");
	}


	public Server define(final String key, final JsonObject value)
	{
		put(key, value);
		return this;
	}

	public Server setDefault(final String key, final JsonObject value)
	{
		put(key, JsonObject.merge(value, getObject(key)));
		return this;
	}
}