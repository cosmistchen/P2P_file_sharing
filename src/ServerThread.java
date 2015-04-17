import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by xueliu on 4/15/15.
 */
class ServerThread extends Thread {
    private Socket socket;
    public Integer isShake = 0;
    public Integer clientID;
    public String myID;
    public ArrayList<BitClient> clients; // clients pool
    public StringBuilder commands = new StringBuilder();
    public Integer isBitFieldSent = 0;
    public ArrayList<Integer> haveQueue = new ArrayList<Integer>();
    public boolean isInterested = true;
    public peerProcess peer = null;
    public boolean allDone = false;
    public boolean isChoked = true;
    public boolean doUnchoke = false;
    public boolean isOptimisticChoked = true;
    public boolean doOtimisticUnchoked = false;

    public ServerThread(Socket socket, String myID, ArrayList<BitClient> clients, peerProcess peer) {
        this.clients = clients;
        this.peer = peer;
        this.myID = myID;
        this.socket = socket;
        this.peer.pieceServed++;
    }

    public void sendBitField() {

        if (isBitFieldSent == 0 && isShake != 0) {
            System.out.println("server " + myID + "-->" + clientID + ": trying to send bitField");
            Integer msgLength = CfgPeeker.getBytesNumberOfBitField() + 1;
            byte[] tempBitFieldInBytes = ActualMessage.booleanArrayToByteArray(this.peer.uniqueBitField);
            System.out.println("client " + myID + "-->" + clientID + ": send BitField of length: " + tempBitFieldInBytes.length);
            ActualMessage tempActualMessage = new ActualMessage(msgLength, 5, tempBitFieldInBytes);
            SendReceiveMessage.sendMessage(this.socket, tempActualMessage);

            isBitFieldSent = 1;
        }
    }

    public void sendHave(Integer index) {

        System.out.println("server " + myID + "-->" + clientID + ": trying to send have: " + index);
        ActualMessage tempActualMessage = new ActualMessage(5, 4, ActualMessage.intToFourByteArray(index));
        SendReceiveMessage.sendMessage(this.socket, tempActualMessage);

    }

    @Override
    public void run() {
        //System.out.println("server " + myID + "-->" + isShake + ": doing handshaking");

        Integer retryTimes = 3;

        // socket has been created here

        while (isShake == 0 && retryTimes > 0) { // wait for handshake
            isShake = HandShake.receiveShake(socket, myID);
            System.out.println("server " + myID + "<--" + isShake + ": receiving handshake");
            retryTimes--;
        }

        if (isShake != 0) { //establish connection
            clientID = isShake;
            System.out.println("server " + myID + "<--" + isShake + ": finished handshaking");
            try {
                System.out.println("server " + myID + "<-->" + isShake + ": connection established, client:(" + socket.getInetAddress() + ":" + socket.getPort() + ")");
                Boolean isClosed = false;
                while (true) {

                    //check all clients completion

                    if (peerStore.peers.size() == CfgPeeker.getUncompletePeerCount()) {
                        boolean flag = true;

                        for (int i = 0; i < peerStore.peers.size(); i++) {
                            if (peerStore.peers.get(i).fileComplete == false) {
                                flag = false;
                            }
                        }
                        if (flag == true) {
                            System.out.println("server " + myID + "--|" + isShake + " deleting temp files");
                            FileUtilities.deletePieces("peer" + myID + "/");
                        }
                    }



                    try {
                        socket.sendUrgentData(0xFF);
                    } catch (Exception ex) {
                        System.out.println("server " + myID + "<-->" + isShake + " disconnected");
                        this.isInterested = false;
                        isClosed = true;
                        this.peer.pieceFileUseful--;
                        System.out.println("servered " + this.peer.pieceServed + "<--> shouldbe: " + CfgPeeker.getOtherUncompletePeerID(myID).size());
                        if (this.peer.pieceServed == (CfgPeeker.getOtherUncompletePeerID(myID).size()) && this.peer.fileComplete == true) {
                            boolean flag2 = true;

                            for(int i =0 ;i<this.peer.servers.size();i++){
                                if (this.peer.servers.get(i).isInterested == true){
                                    flag2=false;
                                }
                            }
                            if (flag2 == true) {
                                this.allDone = true;
                                this.peer.allDone = true;
                                System.out.println("server " + myID + "--|" + isShake + " deleting temp files");
                                FileUtilities.deletePieces("peer" + myID + "/");
                            }

                        }
                    }
                    if (isClosed == true) {
                        break;
                    }
                    if (isClosed == false) {
                        if (commands.length() > 0) {

                            if (commands.charAt(0) == '5' && isShake != 0) {
                                sendBitField();
                                commands.delete(0, 1);
                            } else if (commands.charAt(0) == '4' && isShake != 0 && (!haveQueue.isEmpty())) {
                                sendHave(haveQueue.get(0));
                                commands.delete(0, 1);
                                haveQueue.remove(0);
                            }
                        }
                    }

                    if (isChoked == true && doUnchoke == true) {
                        isChoked = false;
                        doUnchoke = false;
                        System.out.println("server " + myID + "-->" + isShake + ": you are unchoked");
                        ActualMessage sendMsg = new ActualMessage(1, 1, null);
                        SendReceiveMessage.sendMessage(this.socket, sendMsg);
                    }

                    if (isOptimisticChoked == true && doOtimisticUnchoked == true) {
                        isOptimisticChoked = false;
                        doOtimisticUnchoked = false;
                        System.out.println("server " + myID + "-->" + isShake + ": you are optimistically unchoked");
                        ActualMessage sendMsg = new ActualMessage(1, 1, null);
                        SendReceiveMessage.sendMessage(this.socket, sendMsg);
                    }

                    ActualMessage message = SendReceiveMessage.receiveMessage(this.socket);
                    if (message != null) {
                        switch (message.type) {
                            case 0: {
                                break;
                            }
                            case 1: {
                                break;
                            }
                            case 2: {
                                this.isInterested = true;
                                System.out.println("server " + myID + "-->" + isShake + ": you're interested with me");
                                //System.out.println(message.toString());
                                //System.out.println("end");
                                break;
                            }
                            case 3: {
                                this.isInterested = false;
                                System.out.println("server " + myID + "-->" + isShake + ": you're not interested with me");
                                //System.out.println(message.toString());
                                //System.out.println("end1");
                                break;
                            }
                            case 4: {
                                break;
                            }
                            case 5: {
                                break;
                            }
                            case 6: {
                                if (this.isChoked == true && this.isOptimisticChoked == true) {
                                    //choked
                                    System.out.println("server " + myID + "-->" + isShake + ": you are choked");
                                    ActualMessage sendMsg = new ActualMessage(1, 0, null);
                                    SendReceiveMessage.sendMessage(this.socket, sendMsg);

                                } else {
                                    Integer index = ActualMessage.byteArrayToInt(message.payload);
                                    System.out.println("server " + myID + "<--" + isShake + ": " + index + " piece requested");
                                    File f = new File("peer" + myID + "/" + index);
                                    if (f.exists()) {
                                        ActualMessage sendMsg = ActualMessage.generatePiece(myID, index);
                                        SendReceiveMessage.sendMessage(this.socket, sendMsg);
                                    }else {
                                        System.out.println("server " + myID + "<--" + isShake + ": " + index + " file not exists");
                                    }
                                }
                                break;
                            }
                            case 7: {
                                break;
                            }
                            default: {
                                System.out.println("server " + myID + "-->" + isShake + ": unknown message received");
                                break;
                            }
                        }
                    } else {
                        Thread.sleep(10);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            // handshake failed
            // do nothing
        }

    }
}