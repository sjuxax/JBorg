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

    public static final int SERIAL = 1;
    public static final int PARALLEL = 2;

    public static Async then(final int type)
    {
        queue.add(cb);
        return Async.instance;
    }

    public static void go()
    {
        while (queue.size() > 0)
        {
            queue.poll().call();
        }
    }

    /**
     * methods that may only be called asynchronously
     * e.g., from within second loop
     */
    public static final class Async
    {
        private static final Async instance = new Async();

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

        public static abstract class DirectoryParams
        {
            String owner;
            String group;
            String mode;
            String sudo;

            public DirectoryParams setOwner(final String owner)
            {
                this.owner = owner
                return this
            }

            public DirectoryParams setGroup(final String group)
            {
                this.group = group
                return this
            }

            public DirectoryParams setMode(final String mode)
            {
                this.mode = mode
                return this
            }

            public DirectoryParams setSudo(final String sudo)
            {
                this.sudo = sudo
                return this
            }
        }

        public static DirectoryParams directory(final String path)
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
}
