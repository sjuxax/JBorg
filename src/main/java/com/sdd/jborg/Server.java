package com.sdd.jborg;

import com.sdd.jborg.util.JsonObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Server
{
	private final JsonObject meta = new JsonObject();

	private static final Pattern FQDN_PATTERN = Pattern.compile(
		"^(test-)?([a-z]{2,3}-[a-z]{2,3})-([a-z]{1,5})-([a-z-]+)(\\d{2,4})(?:-([a-z]+))?(?:\\.(\\w+\\.[a-z]{2,3}))$",
		Pattern.CASE_INSENSITIVE);

	public Server setFqdn(final String fqdn)
	{
		meta.putString("fqdn", fqdn);
		final Matcher m = FQDN_PATTERN.matcher(fqdn);
		if (m.matches())
		{
			setDatacenter(m.group(2));
			setEnv(m.group(3));
			setType(m.group(4));
			setInstance(m.group(5));
			setSubproject(m.group(6));
			setTld(m.group(7));
		}

		return this;
	}

	public String getFqdn()
	{
		return meta.getString("fqdn");
	}

	public Server setDatacenter(final String datacenter)
	{
		meta.putString("datacenter", datacenter);
		return this;
	}

	public String getDatacenter()
	{
		return meta.getString("datacenter");
	}

	public Server setEnv(final String env)
	{
		meta.putString("env", env);
		return this;
	}

	public String getEnv()
	{
		return meta.getString("env");
	}

	public Server setType(final String type)
	{
		meta.putString("type", type);
		return this;
	}

	public String getType()
	{
		return meta.getString("type");
	}

	public Server setInstance(final String instance)
	{
		meta.putString("instance", instance);
		return this;
	}

	public String getInstance()
	{
		return meta.getString("instance");
	}

	public Server setSubproject(final String subproject)
	{
		meta.putString("subproject", subproject);
		return this;
	}

	public String getSubproject()
	{
		return meta.getString("subproject");
	}

	public Server setTld(final String tld)
	{
		meta.putString("tld", tld);
		return this;
	}

	public String getTld()
	{
		return meta.getString("tld");
	}

	public JsonObject getMeta()
	{
		return meta;
	}
}