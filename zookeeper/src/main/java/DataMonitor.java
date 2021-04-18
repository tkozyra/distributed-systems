import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.AsyncCallback.StatCallback;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.data.Stat;

import java.util.List;

public class DataMonitor implements Watcher, StatCallback {

    private final ZooKeeper zooKeeper;
    private final String znode;
    boolean dead;
    private final DataMonitorListener listener;

    public DataMonitor(ZooKeeper zooKeeper, String znode, DataMonitorListener listener) {
        this.zooKeeper = zooKeeper;
        this.znode = znode;
        this.listener = listener;
        zooKeeper.exists(znode, true, this, null);
        watchChildren(znode);
    }

    public interface DataMonitorListener {
        void exists(boolean ex);

        void closing(int rc);
    }

    public void process(WatchedEvent watchedEvent) {
        String path = watchedEvent.getPath();
        Event.EventType eventType = watchedEvent.getType();

        if (eventType == Event.EventType.None) {
            switch (watchedEvent.getState()) {
                case SyncConnected:
                    break;
                case Expired:
                    dead = true;
                    listener.closing(KeeperException.Code.SessionExpired);
                    break;
            }
        } else if (eventType.equals(Event.EventType.NodeChildrenChanged)) {
            if (path != null) {
                watchChildren(znode);
                zooKeeper.exists(znode, true, this, null);
                printNumberOfChildren(zooKeeper);
            }
        }

        if (eventType.equals(Event.EventType.NodeCreated)) {
            zooKeeper.exists(znode, true, this, null);
            watchChildren(znode);
        } else if (eventType.equals(Event.EventType.NodeDeleted)) {
            zooKeeper.exists(znode, true, this, null);
        }
    }

    private void watchChildren(String node) {
        try {
            if (zooKeeper.exists(node, false) != null) {
                List<String> children = zooKeeper.getChildren(node, this);
                for (String child : children) {
                    watchChildren(node + "/" + child);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printNumberOfChildren(ZooKeeper zooKeeper) {
        try {
            System.out.println("--- NUMBER OF ALL CHILDREN --- ");
            System.out.println(zooKeeper.getAllChildrenNumber(znode));
            System.out.println("-------------------------------");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
                listener.closing(rc);
                return;
            default:
                zooKeeper.exists(znode, true, this, null);
                return;
        }
        listener.exists(exists);
    }
}
