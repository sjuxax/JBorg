package com.sdd.jborg;

import com.sdd.jborg.util.Callback0;
import org.reflections.Reflections;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Set;

/**
 * Standard fields and methods every script should have in scope.
 */
public class Standard
{
	/**
	 * Base Script class all scripts must extend.
	 */
	public static abstract class Script
	{
		private static final Set<Class<? extends Script>> scripts = new Reflections().getSubTypesOf(Script.class);

		public static Script findMatch()
		{
			for (Class<? extends Script> script : scripts)
			{
				try
				{
					final Script instance = script.newInstance();
					if (instance.match())
					{
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

		abstract public boolean match();

		abstract public void assimilate();
	}

	// Global Attributes

	public static final Networks networks = new Networks(CoffeeScript.readCsonFileToJsonObject("networks.coffee"));
	public static final Server server = new Server(); // a.k.a. "locals"
	public static Ssh ssh;

	// Async Flow Control

	private static Queue<Callback0> queue = new ArrayDeque<>();

	public static void then(Callback0 cb)
	{
		queue.add(cb);
	}

	public static void go()
	{
		while (queue.size() > 0)
		{
			queue.poll().call();
		}
	}

	// Standard Library

	public static Callback0 execute(final String bashCommand)
	{
		return () -> {
			ssh.cmd(bashCommand);
		};
	}
}
