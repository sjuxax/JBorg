package com.sdd.jborg;

import com.sdd.jborg.cloud.Aws;
import org.reflections.Reflections;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main
{
	//public static final Networks networks = loadNetworks();
	public static Ssh ssh;

	public static void main(String[] args)
	{
		parseFqdn(args[1]);

		switch (args[0].toLowerCase())
		{
			case "assemble":
				//create();
				// flow through to assimilate
			case "assimilate":
				final Script script = findScriptClass();
				script.assimilate(); // loop 1

				ssh = new Ssh();
				ssh.connect("smullin.org", 22, "msmullin", "id_rsa");
				//ssh.cmd("ping -c 1 google.com");

				AsyncFlowControl.go(); // loop 2
				break;
		}
	}

	private static Networks loadNetworks()
	{
		final Set<Class<? extends Networks>> networks = reflections.getSubTypesOf(Networks.class);
		try
		{
			return networks.iterator().next().newInstance();
		}
		catch (InstantiationException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	private static void create()
	{
		Aws.create();
	}

	private static final Reflections reflections = new Reflections();
	private static Script findScriptClass()
	{
		final Set<Class<? extends Script>> scripts = reflections.getSubTypesOf(Script.class);
		for (Class<? extends Script> script : scripts)
		{
			try
			{
				final Script instance = script.newInstance();
				if (instance.match()) {
					return instance;
				}
			}
			catch (InstantiationException e)
			{
				e.printStackTrace();
			}
			catch (IllegalAccessException e)
			{
				e.printStackTrace();
			}
		}
		return null;
	}

	public static final class Server
	{
		public static String fqdn;
		public static String datacenter;
		public static String env;
		public static String type;
		public static String instance;
		public static String subproject;
		public static String tld;
	}

	private static Pattern FQDN_PATTERN = Pattern.compile(
		"^(test-)?([a-z]{2,3}-[a-z]{2,3})-([a-z]{1,5})-([a-z-]+)(\\d{2,4})(?:-([a-z]+))?(?:\\.(\\w+\\.[a-z]{2,3}))$",
			Pattern.CASE_INSENSITIVE);

	private static void parseFqdn(final String fqdn)
	{
		Server.fqdn = fqdn;
		final Matcher m = FQDN_PATTERN.matcher(fqdn);
		if (m.matches())
		{
			Server.datacenter = m.group(2);
			Server.env = m.group(3);
			Server.type = m.group(4);
			Server.instance = m.group(5);
			Server.subproject = m.group(6);
			Server.tld = m.group(7);
		}
	}

//	public static void oldmain(String[] args) throws IOException
//	{
//		CoffeeSipper cs = new CoffeeSipper();
//		try
//		{
//			String s = cs.toJs("a = b: c: 1");
//			System.out.println(s);
//			System.out.println("Java can compile CSON, too!");
//		}
//		catch (ScriptException e)
//		{
//			System.out.println(e.toString());
//		}
//	}
}
