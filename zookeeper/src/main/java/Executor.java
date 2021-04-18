import org.apache.log4j.PropertyConfigurator;

import java.io.*;
import java.util.List;
import java.util.Scanner;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

public class Executor implements Watcher, Runnable, DataMonitor.DataMonitorListener {
    private final String znode;
    private final DataMonitor dataMonitor;
    private final ZooKeeper zooKeeper;
    private final String exec;
    private Process child;

    public Executor(String hostPort, String znode, String exec) throws IOException {
        this.znode = znode;
        this.exec = exec;
        zooKeeper = new ZooKeeper(hostPort, 3000, this);
        dataMonitor = new DataMonitor(zooKeeper, znode, this);
    }

    public static void main(String[] args) {
        String log4jConfPath = ".\\src\\main\\resources\\log4j.properties";
        PropertyConfigurator.configure(log4jConfPath);

        if (args.length < 3) {
            System.err.println("Wrong number of arguments. Arguments: [hostPort] [znode] [program to execute]");
            System.exit(2);
        }

        String hostPort = args[0];
        String znode = args[1];
        String exec = args[2];

        Executor executor = null;
        try {
            executor = new Executor(hostPort, znode, exec);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Scanner scanner = new Scanner(System.in);
        while (true) {
            scanner.nextLine();
            executor.printTree(znode);
        }
    }

    public void process(WatchedEvent event) {
        if (dataMonitor != null) {
            dataMonitor.process(event);
        }
    }

    public void run() {
        try {
            synchronized (this) {
                while (!dataMonitor.dead) {
                    wait();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void closing(int rc) {
        synchronized (this) {
            notifyAll();
        }
    }

    public void exists(boolean ex) {
        if (ex && child == null) {
            System.out.println("--- STARTING EXTERNAL APP ---");
            try {
                child = Runtime.getRuntime().exec(exec);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (!ex && child != null) {
            System.out.println("--- CLOSING EXTERNAL APP ---");
            child.destroy();
            try {
                child.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            child = null;
        }
    }

    private void printTree(String path) {
        System.out.println("---------- NODE TREE ----------");
        try {
            printTreeStructure(path);
        } catch (Exception e) {
            System.out.println("Tree is empty.");
        }
        System.out.println("-------------------------------");
    }

    private void printTreeStructure(String path) throws KeeperException, InterruptedException {

        List<String> nodes = zooKeeper.getChildren(path, false);
        for (String node : nodes) {
            String newNode = path + "/" + node;
            if (zooKeeper.exists(newNode, false) != null) {
                System.out.println(newNode);
                printTreeStructure(newNode);
            }
        }

    }
}
