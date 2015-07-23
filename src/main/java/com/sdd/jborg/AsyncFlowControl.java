package com.sdd.jborg;

import com.sdd.jborg.util.Callback0;

import java.util.ArrayDeque;
import java.util.Queue;

public class AsyncFlowControl
{
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
}
