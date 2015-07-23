package com.sdd.jborg.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class Logger
{
	private static final SimpleDateFormat PREFERRED_TS_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS ");

	public static void out(final String msg)
	{
		System.out.println(
			PREFERRED_TS_FORMAT.format(new Date()) +
				msg);
	}

	public interface Func0<T>
	{
		T call();
	}

	public static void debug(final Func0<String> msg)
	{
//		if (Config.getInstance().getLogVerbosity() > 5)
//		{
			out(msg.call());
//		}
	}
}
