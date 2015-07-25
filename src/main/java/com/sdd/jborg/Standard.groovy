package com.sdd.jborg

import com.sdd.jborg.util.Callback0
import org.reflections.Reflections

/**
 * Standard fields and methods every script should have in scope.
 */
public class Standard
{
	// Global Attributes

	public static final Networks networks = new Networks(CoffeeScript.readCsonFileToJsonObject("networks.coffee"));
	public static final Server server = new Server(); // a.k.a. "locals"
	public static Ssh ssh;

	// Async Flow Control

	private static Queue<Callback0> queue = new ArrayDeque<>();

	public static void then(final Params params)
	{
		queue.add(params.getCallback());
	}

	public static void go()
	{
		while (queue.size() > 0)
		{
			queue.poll().call();
		}
	}

	// Standard Library

	public static Callback0 decrypt(final String s)
	{
		return {

		};
	}

	public static final class DirectoryParams extends Params
	{
		private String owner;
		public DirectoryParams setOwner(final String owner)
		{
			this.owner = owner;
			return this;
		}

		public String getOwner()
		{
			return owner;
		}

		public DirectoryParams setGroup(final String group)
		{
			return this;
		}

		public DirectoryParams setMode(final String mode)
		{
			return this;
		}

		public DirectoryParams setSudo(final boolean sudo)
		{
			return this;
		}
	}

	public static class Params
	{
		private Callback0 callback;

		public void setCallback(final Callback0 callback)
		{
			this.callback = callback;
		}

		public Callback0 getCallback()
		{
			return callback;
		}
	}

	public static DirectoryParams directory(final String path)
	{
		final DirectoryParams o = new DirectoryParams();
		o.setCallback({
			then(execute("mkdir "+path+" "+o.getOwner()));
		});
		return o;
	}


	public static Params execute(final String cmd)
	{
		final Params o = new Params();
		o.setCallback({
			ssh.cmd(cmd);
		});
		return o;
	}

	public static Callback0 template(final String path)
	{
		return {

		};
	}

	public static Callback0 upload(final String path)
	{
		return {

		};
	}

	public static Callback0 remoteFileExists(final String path)
	{
		return {

		};
	}
}
