package com.sdd.jborg;

import org.reflections.Reflections;

import java.util.Set;

/**
 * Base Script class all scripts must extend.
 */
public abstract class Script
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