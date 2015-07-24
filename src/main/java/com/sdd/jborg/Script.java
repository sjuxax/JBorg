package com.sdd.jborg;

import java.util.HashSet;
import java.util.Set;

public abstract class Script
{
	protected static final Set<Script> REGISTRY = new HashSet<>();

	public static Script findMatch()
	{
		for (final Script script : REGISTRY)
		{
			if (script.match())
			{
				return script;
			}
		}
		return null;
	}

	abstract public boolean match();
	abstract public void assimilate();
}
