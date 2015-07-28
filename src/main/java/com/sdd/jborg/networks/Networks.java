package com.sdd.jborg.networks;

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
		private final DatacenterName name;
		private Provider provider;
		private TldName tld;

		public Datacenter(final DatacenterName name)
		{
			this.name = name;
		}

		public Datacenter setProvider(final Provider provider)
		{
			this.provider = provider;
			return this;
		}

		public Datacenter setTld(final TldName tld)
		{
			this.tld = tld;
			return this;
		}

		private final ChainableCollection<DatacenterGroup> GROUPS = new ChainableCollection<>();

		public Datacenter addGroup(final DatacenterGroup datacenterGroup)
		{
			GROUPS.add(datacenterGroup);
			return this;
		}
	}

	public enum ProviderTypes
	{
		AWS,
		OPENSTACK,
		RACKSPACE;
	}

	public enum TimeZoneName
	{
		GMT
	}

	public interface DatacenterName {}
	public interface DatacenterGroupName {}
	public interface DatacenterEnvName {}
	public interface TldName {}

	public interface Provider
	{

	}

	public static final class AWSProvider implements Provider
	{
		private Region region;
		private Zone zone;
		private String image;

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

		public AWSProvider setRegion(final Region region)
		{
			this.region = region;
			return this;
		}

		public AWSProvider setZone(final Zone zone)
		{
			this.zone = zone;
			return this;
		}

		public AWSProvider setImage(final String image)
		{
			this.image = image;
			return this;
		}
	}

	public static final class OpenStackProvider implements Provider
	{

	}

	public static final class RackspaceProvider implements Provider
	{

	}

	public static final class DatacenterGroup
	{
		private final DatacenterGroupName datacenterGroupName;

		public DatacenterGroup(final DatacenterGroupName datacenterGroupName)
		{
			this.datacenterGroupName = datacenterGroupName;
		}
	}
}

