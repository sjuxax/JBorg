package com.sdd.jborg.cloud;

import com.sdd.jborg.Logger;
import com.sdd.jborg.Main;
import com.sdd.jborg.util.JsonObject;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONObject;
import us.monoid.web.Content;
import us.monoid.web.JSONResource;
import us.monoid.web.Resty;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static com.sdd.jborg.scripts.Standard.*;

public class OpenStack
	implements Provider
{
	private static final Resty resty = new Resty();
	private String token;
	private String host;
	private String project;
	private String user;
	private String password;
	private String projectId;

	public String getHost()
	{
		return host;
	}

	public OpenStack setHost(String host)
	{
		this.host = host;
		return this;
	}

	public String getProject()
	{
		return project;
	}

	public OpenStack setProject(String project)
	{
		this.project = project;
		return this;
	}

	public String getUser()
	{
		return user;
	}

	public OpenStack setUser(String user)
	{
		this.user = user;
		return this;
	}

	public String getPassword()
	{
		return password;
	}

	public OpenStack setPassword(String password)
	{
		this.password = password;
		return this;
	}

	public String getProjectId()
	{
		return projectId;
	}

	public OpenStack setProjectId(String projectId)
	{
		this.projectId = projectId;
		return this;
	}

	private String name;
	private String image;
	private String flavor;
	private String keyName;
	private String privateNic;
	private String securityGroup;

	public String getName()
	{
		return name;
	}

	public OpenStack setName(String name)
	{
		this.name = name;
		return this;
	}

	public String getImage()
	{
		return image;
	}

	public OpenStack setImage(String image)
	{
		this.image = image;
		return this;
	}

	public String getFlavor()
	{
		return flavor;
	}

	public OpenStack setFlavor(String flavor)
	{
		this.flavor = flavor;
		return this;
	}

	@Override
	public String getKeyName()
	{
		return keyName;
	}

	public OpenStack setKeyName(String keyName)
	{
		this.keyName = keyName;
		return this;
	}

	public String getPrivateNic()
	{
		return privateNic;
	}

	public OpenStack setPrivateNic(String privateNic)
	{
		this.privateNic = privateNic;
		return this;
	}

	public String getSecurityGroup()
	{
		return securityGroup;
	}

	public OpenStack setSecurityGroup(String securityGroup)
	{
		this.securityGroup = securityGroup;
		return this;
	}

	private JSONResource request(final String uri, final String content)
	{
		JSONResource data = null;
		try
		{
			if (token != null)
			{
				resty.withHeader("X-Auth-Token", token);
			}

			final String url = "http://" + host + uri;
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
			try
			{
				Logger.stdout(new JsonObject(data.object().toString()).toString(2));
			}
			catch (final us.monoid.json.JSONException e)
			{
				Logger.stdout("Response is not valid JSON. Response code: " +
					data.http().getResponseCode() + "  " +
					data.http().getResponseMessage() +
					". Raw response:\n" + inputStreamToString(data.http().getInputStream())
				);
			}
			return data;
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			return data;
		}
	}

	private static String inputStreamToString(final InputStream in)
	{
		try
		{
			final BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
			final StringBuilder sb = new StringBuilder();
			String str;
			while ((str = r.readLine()) != null)
			{
				sb.append(str);
			}
			return sb.toString();
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	private JSONResource post(final String uri, final String content)
	{
		return request(uri, content);
	}

	private JSONResource get(final String uri)
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

	private void getToken()
	{
		final JSONResource data = post(":5000/v2.0/tokens",
			"{\"auth\": {\"tenantName\": \"" +
				project + "\", \"passwordCredentials\": {\"username\": \"" +
				user + "\", \"password\": \"" +
				password + "\"}}}");

		token = getString(data, "access.token.id");
	}

	/**
	 * Create a new instance at the cloud provider.
	 */
	public void createVirtualMachine()
	{
		getToken();

		// TODO: escape user input
		final JSONResource data = post(":8774/v2/" + projectId + "/servers", "{\n" +
			"    \"server\": {\n" +
			"        \"name\": \"" + name + "\",\n" +
			"        \"imageRef\": \"" + image + "\",\n" +
			"        \"flavorRef\": \"" + flavor + "\",\n" +
			"        \"key_name\": \"" + keyName + "\",\n" +
			"        \"min_count\": \"1\",\n" +
			"        \"max_count\": \"1\",\n" +
			"        \"networks\": [\n" +
			"          { \"uuid\": \"" + privateNic + "\" }\n" +
			"        ],\n" +
			"        \"security_groups\": [{ \"name\": \"" + securityGroup + "\" } ]\n" +
//			"        ,\"availability_zone\": \"" + datacenter.getString("os_availability_zone") + "\"\n" +
			"    }\n" +
			"}");

		final String serverId = getString(data, "server.id");

		final String ip = getAvailableIp();

		if (ip == null)
		{
			Main.die(new Exception("OpenStack reports no available public IPs."));
		}

		delay(30 * 1_000, "for instance to finish initializing");

		post(":8774/v2/" + projectId + "/servers/" + serverId + "/action",
			"{\"addFloatingIp\": {\"address\": \"" + ip + "\"}}");

		server.ssh.host = ip;
		server.ssh.port = 22;
		server.ssh.user = "ubuntu";
		server.ssh.key = keyName;
	}

	private String getAvailableIp()
	{
		try
		{
			final JSONResource data = get(":8774/v2/" + projectId + "/os-floating-ips");
			final JSONArray arr = (JSONArray) data.get("floating_ips");
			for (int i = 0; i < arr.length(); i++)
			{
				JSONObject floater = arr.getJSONObject(i);
				if (floater.get("instance_id") == JSONObject.NULL)
				{
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