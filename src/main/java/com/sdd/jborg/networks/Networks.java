package com.sdd.jborg.networks;

import com.sdd.jborg.util.Callback1;
import com.sdd.jborg.util.Func0;
import com.sdd.jborg.util.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class Networks
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


	public enum TimeZoneName
	{
		GMT
	}

	public static class ChainableCollection<T>
	{
		private final List<T> list = new ArrayList<>();

		public ChainableCollection<T> add(final T item)
		{
			list.add(item);
			return this;
		}
	}

	public static final ChainableCollection<Datacenter> DATACENTERS = new ChainableCollection<>();

	public static final class Datacenter
	{
		private final Name name;
		private Provider provider;
		private Tld tld;

		public Datacenter(final Name name)
		{
			this.name = name;
		}

		public Datacenter setProvider(final Provider provider)
		{
			this.provider = provider;
			return this;
		}

		public Datacenter setTld(final Tld tld)
		{
			this.tld = tld;
			return this;
		}

		public Datacenter getProvider(final Callback1<AwsProvider> cb)
		{
			// TODO: determine subtype automatically
			cb.call((AwsProvider) provider);
			return this;
		}

		private final ChainableCollection<Group> GROUPS = new ChainableCollection<>();

		public interface Name {}
		public interface Env {}
		public interface Tld {}

		public static final class Group
		{
			public interface Name {}

			// TODO: determine provider subclass dynamically
			private AwsProvider awsProvider;
			private Env env;
			private String awsSubnet;
			private final Name name;

			public Group(final Name datacenterGroupName)
			{
				this.name = datacenterGroupName;
			}

			public Group setEnv(final Env env)
			{
				this.env = env;
				return this;
			}

			public Group getProvider(final Callback1<AwsProvider> cb)
			{
				cb.call(awsProvider);
				return this;
			}
		}

		public Datacenter addGroup(final Group datacenterGroup)
		{
			// TODO: clone provider
			GROUPS.add(datacenterGroup);
			return this;
		}
	}

	public static abstract class Provider
	{
		public enum Type
		{
			AWS,
			OPENSTACK,
			RACKSPACE;
		}
	}

	public static final class AwsProvider extends Provider
	{
		private Region region;
		private Zone zone;
		private String image;
		private String subnet;
		private String[] securityGroups;
		private boolean associatePublicIp;
		private Size size;
		private String keyFileName;

		public AwsProvider clone()
		{
			// TODO: finish this
			try
			{
				super.clone();
			}
			catch (CloneNotSupportedException e)
			{
				e.printStackTrace();
			}
			return this;
		}

		public enum Region
		{
			US_WEST_2;

			public String toString()
			{
				return name().toLowerCase().replaceAll("_", "-");
			}
		}

		public enum Zone
		{
			US_WEST_2A;

			public String toString()
			{
				return name().toLowerCase().replaceAll("_", "-");
			}
		}

		public enum Size
		{
			T2_MICRO;

			public String toString()
			{
				return name().toLowerCase().replaceAll("_", ".");
			}
		}

		public AwsProvider setRegion(final Region region)
		{
			this.region = region;
			return this;
		}

		public AwsProvider setZone(final Zone zone)
		{
			this.zone = zone;
			return this;
		}

		public AwsProvider setImage(final String image)
		{
			this.image = image;
			return this;
		}

		public AwsProvider setSubnet(final String subnet)
		{
			this.subnet = subnet;
			return this;
		}

		public AwsProvider setSecurityGroups(final Func0<String[]> enabled)
		{
			enabled.call();
			return this;
		}

		public AwsProvider setSecurityGroups(final boolean enabled)
		{
			if (!enabled)
			{
				this.securityGroups = new String[0];
			}
			return this;
		}

		public AwsProvider setAssociatePublicIp(final boolean enabled)
		{
			this.associatePublicIp = enabled;
			return this;
		}

		public AwsProvider setSize(final Size size)
		{
			this.size = size;
			return this;
		}

		public AwsProvider setKeyFileName(final String keyFileName)
		{
			this.keyFileName = keyFileName;
			return this;
		}
	}

	public static final class OpenStackProvider extends Provider
	{

	}

	public static final class RackspaceProvider extends Provider
	{

	}
}

