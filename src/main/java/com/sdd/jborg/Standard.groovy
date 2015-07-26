package com.sdd.jborg

import com.sdd.jborg.util.Callback0

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

    public static void die(final String reason)
    {
        Logger.stderr "Aborting. Reason: ${reason}"
        System.exit 1
    }

    public static Callback0 decrypt(final String s)
    {
        return {

        };
    }

    public static final class DirectoryParams
        extends Params
    {
        public String sudo;
        public String owner;
        public String group;
        public String mode;

        public DirectoryParams setSudo(sudo)
        {
            this.sudo = sudo
            return this
        }

        public DirectoryParams setOwner(owner)
        {
            this.owner = owner
            return this
        }

        public DirectoryParams setGroup(group)
        {
            this.group = group
            return this
        }

        public DirectoryParams setMode(mode)
        {
            this.mode = mode
            return this
        }
    }

    public static class Params
    {
        private Callback0 callback;

        private void setCallback(final Callback0 callback)
        {
            this.callback = callback;
        }

        private Callback0 getCallback()
        {
            return callback;
        }
    }

    public static final class ChownParams
        extends Params
        implements SudoAbility, OwnAbility
    {
    }

    public static ChownParams chown(final String path)
    {
        final ChownParams o = new ChownParams()
        o.setCallback {
            if (o.setSudo("a"))
                die "chown owner and group are required."
            then execute("chown ${path} " + o.getOwner())
        }
        return o
    }

    public static final class ChmodParams
        extends Params
        implements SudoAbility, ModeAbility
    {
    }

    public static ChmodParams chmod(final String path)
    {
        final ChmodParams o = new ChmodParams()
        o.setCallback {
            then execute("chmod ${path} " + o.getOwner())
        }
        return o
    }

    public static DirectoryParams directory(final String path)
    {
        final DirectoryParams o = new DirectoryParams()
        o.setCallback {
            then execute("mkdir ${path} " + o.getOwner())
        }
        return o
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
