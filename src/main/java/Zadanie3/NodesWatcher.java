package Zadanie3;

import java.util.List;

import org.apache.zookeeper.*;
import org.apache.zookeeper.AsyncCallback.StatCallback;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.data.Stat;


public class NodesWatcher implements Watcher, StatCallback {

    ZooKeeper zk;
    String znode;
    boolean dead;
    NodesWatcherListener listener;

    public NodesWatcher(ZooKeeper zk, String znode, NodesWatcherListener listener) {
        this.zk = zk;
        this.znode = znode;
        this.listener = listener;

        this.watch();
    }


    /**
     * Other classes use the DataMonitor by implementing this method
     */
    public interface NodesWatcherListener {
        /**
         * The existence status of the node has changed.
         */
        void exists(boolean exists);

    }


    public void watch() {
        zk.exists(znode, true, this, null);
        int below = this.watchChildren(znode);

        if (below == -1) {
            System.out.println("No node " + znode );
        } else {
            System.out.println(znode + "has " + below + " descendants");
        }
    }

    private int watchChildren(String path) {
        int below = 0;
        try {
            List<String> children = zk.getChildren(path, true);
            for (String child : children) {
                below += 1 + this.watchChildren(path + "/" + child);
            }
        } catch (KeeperException e) {
            return -1;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return below;
    }


    public void process(WatchedEvent event) {
        String path = event.getPath();
        if (event.getType() == Event.EventType.None) {

            switch (event.getState()) {
                case SyncConnected:

                    break;
                case Expired:
                    // It's all over
                    dead = true;
                    break;
            }
        } else {
            if (path != null) {
                watch();
            }
        }
    }

    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        boolean exists;
        switch (rc) {
            case Code.Ok:
                exists = true;
                break;
            case Code.NoNode:
                exists = false;
                break;
            case Code.SessionExpired:
            case Code.NoAuth:
                dead = true;

                return;
            default:
                // Retry errors
                zk.exists(znode, true, this, null);
                return;
        }

        listener.exists(exists);
    }

    public void printTree() {
        System.out.println("Tree structure:");
        System.out.println(this.znode);
        printTreeRec(this.znode, 1);
    }

    private void printTreeRec(String path, int indent) {
        try {
            List<String> children = zk.getChildren(path, false);
            for (String child : children) {
                System.out.println(" ".repeat(indent) + "| /" + child);
                printTreeRec(path + "/" + child, indent+3);
            }
        } catch (KeeperException e) {
            // node not found
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}