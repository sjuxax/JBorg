package com.sdd.jborg

import com.sdd.jborg.util.Callback0
import com.sdd.jborg.util.Callback1

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

    public static void then(final Callback0 cb)
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

    public static void die(final String reason)
    {
        Logger.stderr "Aborting. Reason: ${reason}"
        System.exit 1
    }

    public static String decrypt(final String s)
    {
        return "";
    }

    public static Callback0 chown(final Map o = [:], final String path)
    {
        if (!o['owner'] || !o['group'])
            die "chown owner and group are required."
        return {
            execute "chown ${o['owner']}.${o['group']} ${path}"
        }
    }

    public static Callback0 chmod(final Map o = [:], final String path)
    {
        return { execute "chmod ${o['mode']} ${path}" }
    }

    public static class DirectoryParams
    {
        String owner;
        String group;
        String mode;
        String sudo;
    }

    public static Callback0 directory(final String path, final Callback1<DirectoryParams> cb)
    {
        o['mode'] ?: '0755'
        return {
            execute "test -d ${path}", test: { code ->
                if (code == 0)
                    log 'Skipping existing directory.'
                else
                    execute
            }
            execute "mkdir -p ${path}"
        }
    }

    public static void execute(final Map o = [:], final String cmd)
    {
        ssh.cmd cmd
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
