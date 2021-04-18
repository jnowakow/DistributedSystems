package Zadanie3;

import java.io.*;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

public class Executor implements Watcher, NodesWatcher.NodesWatcherListener
{

    NodesWatcher dm;
    ZooKeeper zk;
    String exec[];
    Process child;

    public Executor(String hostPort, String znode, String exec[]) throws IOException {

        this.exec = exec;
        zk = new ZooKeeper(hostPort, 5000, this);
        dm = new NodesWatcher(zk, znode, this);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("USAGE: Executor hostPort znode program [args ...]");
            System.exit(2);
        }
        String zkServers = args[0];
        String znode = args[1];
        String exec[] = new String[args.length - 2];
        System.arraycopy(args, 2, exec, 0, exec.length);

        Executor executor = null;

        try {
            executor = new Executor(zkServers, znode, exec);
        } catch (Exception e) {
            e.printStackTrace();
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                String input = br.readLine();
                if (input.equals("tree")) executor.dm.printTree();
                else {
                    System.out.println("Unknown command");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /***************************************************************************
     * We do process any events ourselves, we just need to forward them on.
     */
    public void process(WatchedEvent event) {
        System.out.println(event);
        dm.process(event);
    }


    public void exists(boolean exists) {
        if (exists){
            if(child == null) {
                try {
                    System.out.println("Starting child");
                    child = Runtime.getRuntime().exec(exec);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        else {
            if (child != null){
                System.out.println("Killing process");

                child.destroy();
                try {
                    child.waitFor();
                } catch (InterruptedException e) {
                }
            }
            child = null;
        }
    }
}