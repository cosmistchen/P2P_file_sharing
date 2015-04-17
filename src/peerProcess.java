import java.io.*;
import java.util.ArrayList;

/**
 * Created by xueliu on 4/12/15.
 */

// usage peerProcess 1001

public class peerProcess {
    private String myID;
    public BitServer bitServer; // there is only one server to listen all request on specific port
    public ArrayList<BitClient> clients = new ArrayList<BitClient>(); // a numbers of clients are used to connect to other peers
    public ArrayList<ServerThread> servers = new ArrayList<ServerThread>();
    public boolean[] uniqueBitField = null;
    public Integer pieceFileUseful = 0;
    public Integer pieceServed = 0;
    public Integer numberOfPreferredNeighbors = null;
    public peerTimer peerTimer = null;
    public boolean allDone = false;
    public boolean fileComplete = false;

    public peerProcess(String myID) throws InterruptedException {

        System.out.print(peerStore.peers.size());
        this.numberOfPreferredNeighbors = Integer.valueOf(CfgPeeker.getNumberOfPreferredNeighbors());
        this.myID = myID;
        // split file
        String fileName = CfgPeeker.getFileName();
        String path = "peer" + myID + "/";
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdir();
        }
        if (CfgPeeker.isCompleteAtBegainning(myID)) {
            File file = new File(path + fileName);
            if (!file.exists()) {
                System.out.println("File Was Not Found!");
                System.exit(0);
            }
            if (file.exists()) {
                FileUtilities.splitFileIntoPieces("peer" + myID + "/", fileName, Integer.valueOf(CfgPeeker.getPieceSize()));
            }
        }


        //combine files

//        if(FileUtilities.isComplete(path)){
//            FileUtilities.combineFilesIntoOne(path,fileName);
//        }else {
//            System.out.println("not complete");
//        }


        //generate bitField
        this.uniqueBitField = BitField.getBitFieldByPeerID(myID);


        this.peerTimer = new peerTimer(this);
        peerTimer.start();


        //opening server
        this.bitServer = new BitServer(this.myID, clients, servers, this);
        bitServer.start();
        System.out.println("peer: " + this.myID + ": server is running is a background thread");

        // check completion
        boolean flag = true;
        for (boolean b : this.uniqueBitField) {
            if (b == false) {
                flag = false;
            }
        }

        if (flag == true) {
            this.fileComplete = true;
        }


        //initialize clients
        if (fileComplete == false) {
            ArrayList otherPeers = CfgPeeker.getOtherPeerID(this.myID);
            for (int i = 0; i < otherPeers.size(); i++) {
                pieceFileUseful++;
                String anotherPeer = (String) otherPeers.get(i);
                System.out.println("peer: " + this.myID + ": client is running, the target server is " + anotherPeer);
                BitClient client = new BitClient(this.myID, anotherPeer, this);
                client.start();
                clients.add(client);
            }
        }

        while (true) {

            if (this.pieceServed == (CfgPeeker.getOtherUncompletePeerID(myID).size()) && this.fileComplete == true) {
                boolean flag2 = true;

                for(int i =0 ;i<this.servers.size();i++){
                    if (this.servers.get(i).isInterested == true){
                        flag2=false;
                    }
                }
                if (flag2 == true) {
                    System.out.println("peer: " + this.myID + ": deleting temp files");
                    this.allDone = true;
                    FileUtilities.deletePieces("peer" + myID + "/");
                    System.out.println("peer: " + this.myID + ": Done");
                    System.exit(0);
                }

            }
            // check all clients
            if (!clients.isEmpty()) {
                for (int i = 0; i < clients.size(); i++) {
                    if (clients.get(i).isShake == 0) {
                        clients.get(i).commands.append('8');
                    }

                    // some client told peer to let all servers send a "have"
                    if (clients.get(i).peerCommands.length() > 0 && clients.get(i).peerCommands.charAt(0) == '4' && clients.get(i).haveQueue.get(0) > 0) {

                        Integer indexForServers = clients.get(i).haveQueue.get(0);
                        for (int j = 0; j < servers.size(); j++) {
                            if (servers.get(j).isShake != 0) {
                                servers.get(j).commands.append('4');
                                servers.get(j).haveQueue.add(indexForServers);
                            }
                        }
                        clients.get(i).peerCommands.delete(0, 1);
                        clients.get(i).haveQueue.remove(0);
                    }
                }
            }

            // check all servers
            for (int i = 0; i < servers.size(); i++) {
                if (servers.get(i).isShake != 0) {
                    servers.get(i).commands.append('5');
                }
            }

            Thread.sleep(10);
//            if (test == 1 && clients.size()>0 && clients.get(0).serverBitField != null){
//                System.out.println(ActualMessage.byteArrayToString(ActualMessage.booleanArrayToByteArray(clients.get(0).serverBitField)));
//                test--;
//            }

        }
    }


    public static void main(String args[]) throws InterruptedException, IOException {

        //do something for initializing

        peerProcess peer = null;

        if (args.length > 0) {
            peer = new peerProcess(args[0]);
        } else {
            peer = new peerProcess("1001");
        }
        peerStore.peers.add(peer);

    }
}


