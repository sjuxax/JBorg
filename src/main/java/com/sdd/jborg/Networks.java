package com.sdd.jborg;

import com.sdd.jborg.util.JsonObject;

public final class Networks
{
	private final JsonObject meta;

	public Networks()
	{
		this.meta = new JsonObject();
	}

	public Networks(final JsonObject meta)
	{
		this.meta = meta;
	}

	public String getSshHost()
	{
		return meta.getObject("ssh").getString("host");
	}

	public Integer getSshPort()
	{
		return meta.getObject("ssh").getInteger("port");
	}

	public String getSshUser()
	{
		return meta.getObject("ssh").getString("user");
	}

	public String getSshKey()
	{
		return meta.getObject("ssh").getString("key");
	}
}

