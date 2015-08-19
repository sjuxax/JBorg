package com.sdd.jborg.cloud;

import com.sdd.jborg.Logger;
import com.sdd.jborg.Main;
import com.sdd.jborg.util.JsonObject;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONObject;
import us.monoid.web.Content;
import us.monoid.web.JSONResource;
import us.monoid.web.Resty;

import java.nio.charset.StandardCharsets;

import static com.sdd.jborg.scripts.Standard.*;

public class OpenStack
{
	private static final Resty resty = new Resty();
	private static String token = null;
	private static JsonObject provider;
	private static JsonObject datacenter;

	private static JSONResource request(final String uri, final String content) {
		JSONResource data = null;
		try
		{
			if (token != null)
			{
				resty.withHeader("X-Auth-Token", token);
			}

			final String url = "http://" + provider.getString("host") + uri;
			if (content == null)
			{
				Logger.stdin("HTTP GET " + url);
				data = resty.json(url);
			}
			else
			{
				Logger.stdin("HTTP POST " + url + "\n" + content);
				data = resty.json(url, new Content("application/json",
					content.getBytes(StandardCharsets.UTF_8)));
			}
			Logger.stdout(data.object().toString().replaceAll("\\{", "{\n"));
			return data;
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			return data;
		}
	}

	private static JSONResource post(final String uri, final String content)
	{
		return request(uri, content);
	}

	private static JSONResource get(final String uri)
	{
		return request(uri, null);
	}

	private static String getString(final JSONResource data, final String key)
	{
		try
		{
			return data.get(key).toString();
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	private static void getToken() {
		final JSONResource data = post(":5000/v2.0/tokens",
			"{\"auth\": {\"tenantName\": \"" +
				provider.getString("project") + "\", \"passwordCredentials\": {\"username\": \"" +
				provider.getString("user") + "\", \"password\": \"" +
				provider.getString("password") + "\"}}}");

		token = getString(data, "access.token.id");
	}

	/**
	 * Create a new instance at the cloud provider.
	 *
	 * @return ip address of newly created machine
	 */
	public static void create(final String name)
	{
		// TODO: lookup openstack datacenter params dynamically by server fqdn
		datacenter = networks.getObject("datacenters").getObject("sbi-slc");
		provider = networks.getObject("providers").getObject("openstack");

		getToken();

		// TODO: escape user input
		final JSONResource data = post(":8774/v2/" + provider.getString("project_id") + "/servers", "{\n" +
			"    \"server\": {\n" +
			"        \"name\": \""+ name +"\",\n" +
			"        \"imageRef\": \""+ datacenter.getString("os_image") +"\",\n" +
			"        \"flavorRef\": \""+ datacenter.getString("os_flavor") +"\",\n" +
			"        \"key_name\": \""+ datacenter.getString("os_key_name") +"\",\n" +
			"        \"min_count\": \"1\",\n" +
			"        \"max_count\": \"1\",\n" +
			"        \"networks\": [\n" +
			"          { \"uuid\": \""+ datacenter.getString("os_private_nic") +"\" }\n" +
			"        ],\n" +
			"        \"security_groups\": [{ \"name\": \""+ datacenter.getString("os_security_group") +"\" } ],\n" +
			"        \"availability_zone\": \""+ datacenter.getString("os_availability_zone") +"\"\n" +
			"    }\n" +
			"}");

		final String serverId = getString(data, "server.id");

		final String ip = getAvailableIp();

		if (ip == null) {
			Main.die(new Exception("OpenStack reports no available public IPs."));
		}

		delay(30 * 1_000, "for instance to finish initializing");

		post(":8774/v2/" + provider.getString("project_id") + "/servers/" + serverId + "/action",
			"{\"addFloatingIp\": {\"address\": \"" + ip + "\"}}");

		server.getObject("ssh").put("host", ip);
		server.getObject("ssh").put("port", 22);
		server.getObject("ssh").put("user", "ubuntu");
		server.getObject("ssh").put("key", datacenter.getString("os_key_name"));
	}

	private static String getAvailableIp() {
		try
		{
			final JSONResource data = get(":8774/v2/" + provider.getString("project_id") + "/os-floating-ips");
			final JSONArray arr = (JSONArray) data.get("floating_ips");
			for (int i = 0; i < arr.length(); i++) {
				JSONObject floater = arr.getJSONObject(i);
				if (floater.get("instance_id") == JSONObject.NULL) {
					return floater.get("ip").toString();
				}
			}

			// got through the loop with no hits; return null
			return null;
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
}