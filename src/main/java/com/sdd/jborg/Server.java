package com.sdd.jborg;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sdd.jborg.scripts.Standard.*;

public final class Server
{
	private static final Pattern FQDN_PATTERN = Pattern.compile(
		"^(test-)?([a-z]{2,3}-[a-z]{2,3})-([a-z]{1,5})-([a-z-]+)(\\d{2,4})(?:-([a-z]+))?(?:\\.(\\w+\\.[a-z]{2,3}))$",
		Pattern.CASE_INSENSITIVE);

	public class Fqdn
	{
		public String datacenter;
		public String env;
		public String type;
		public String instance;
		public String subproject;
		public String tld;

		public Fqdn(final String fqdn)
		{
			final Matcher m = FQDN_PATTERN.matcher(fqdn.toLowerCase());
			if (m.matches())
			{
				this.datacenter = m.group(2);
				this.env = m.group(3);
				this.type = m.group(4);
				this.instance = m.group(5);
				this.subproject = m.group(6);
				this.tld = m.group(7);
			}
		}

		@Override
		public String toString() {
			return getHostname() + "." + tld;
		}

		public String getHostname()
		{
			return datacenter + "-" +
				env + "-" +
				type +
				instance +
				(subproject != null ? "-" + subproject : "");
		}
	}
	public Fqdn fqdn;
	public Server setFqdn(final String fqdn)
	{
		this.fqdn = new Fqdn(fqdn);
		return this;
	}

	private Datacenter datacenter;

	public Server setDatacenter(final Datacenter datacenter)
	{
		this.datacenter = datacenter;
		return this;
	}

	public Datacenter getDatacenter()
	{
		return datacenter;
	}

	public class Ssh
	{
		public String host;
		public String user;
		public int port;
		public String key;
	}
	public Ssh ssh = new Ssh();

	private String privateIp;
	private String publicIp;

	public String getPrivateIp()
	{
		return privateIp;
	}

	public Server setPrivateIp(String privateIp)
	{
		this.privateIp = privateIp;
		return this;
	}

	public String getPublicIp()
	{
		return publicIp;
	}

	public Server setPublicIp(String publicIp)
	{
		this.publicIp = publicIp;
		return this;
	}
}