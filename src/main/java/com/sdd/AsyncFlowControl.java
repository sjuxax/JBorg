package com.sdd;

import com.sdd.util.Callback0;

import java.util.ArrayDeque;
import java.util.Queue;

public class AsyncFlowControl
{
	private static Queue<Callback0> queue = new ArrayDeque<>();

	public static void Then(Callback0 cb)
	{
		queue.add(cb);
	}

	public static void Go()
	{
		while (queue.size() > 0)
		{
			queue.poll().call();
		}
	}
}
