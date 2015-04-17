import com.sun.org.apache.xpath.internal.operations.Bool;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by xueliu on 4/11/15.
 */

// usage java BitServer 1001

public class BitServer extends Thread {
    public String myID;
    public ServerSocket serverSocket;
    public ArrayList<BitClient> clients;
    public ArrayList<ServerThread> servers;
    public peerProcess peer = null;


    public BitServer(String myID, ArrayList<BitClient> clients,ArrayList<ServerThread> servers, peerProcess peer) {
        this.myID = myID;
        this.clients = clients;
        this.servers = servers;
        this.peer = peer;
        try {
            this.serverSocket = new ServerSocket(Integer.valueOf(CfgPeeker.getPortByID(this.myID)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        //System.out.println(this.myID + ": server is running");
        while (true) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();

                // everything about communication should be done in thread
                ServerThread serverThread = new ServerThread(socket, myID, clients,this.peer);
                servers.add(serverThread);
                serverThread.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {

//        if (args.length > 0) {
//            new BitServer(args[0], null,new ArrayList<ServerThread>(),).run();
//        } else {
//            new BitServer("1001", null,new ArrayList<ServerThread>()).run();
//        }
//        System.out.println("server: " + "running in background thread");

    }

}



//wake up a cli//ent
//            for (int i = 0; i < clients.size(); i++) {
//                Integer id = Integer.valueOf(clients.get(i).getServerID());
//                //System.out.println(id+"waking up " + clientID);
//                if (id.equals(clientID)) {
//                    if(clients.get(i).isAwake() == false){
//                        System.out.println("client " + myID + "-->" + isShake + ": trying to wake up//");
//                        clients.get(i).wakeUp();
//                    }
//                }
//            }