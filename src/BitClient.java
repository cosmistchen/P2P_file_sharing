import com.sun.imageio.plugins.common.BitFile;
import com.sun.org.apache.xpath.internal.operations.Bool;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by xueliu on 4/11/15.
 */

// usage: java BitClient myID serverID

public class BitClient extends Thread {
    public boolean[] serverBitField = null;
    public peerProcess peer = null;
    public String myID = "not been set";
    public String serverID;
    public Boolean isConnected = false;
    public Socket socket;
    public Integer isShake = 0;
    public Boolean isInterest = null;
    public Boolean awake = true;
    public Boolean allFinished = false;
    public StringBuilder commands = new StringBuilder();
    public StringBuilder peerCommands = new StringBuilder();
    public ArrayList<Integer> haveQueue = new ArrayList<Integer>();
    public boolean isChoked = false;
    public Integer speed = 0;
    public boolean clearSpeed = false;


    public BitClient(String myID, String serverID, peerProcess peer) {
        this.myID = myID;
        this.serverID = serverID;
        this.peer = peer;
        isConnected = false;
    }

    public BitClient(String serverID) {
        this.serverID = serverID;
        isConnected = false;
    }

    // try to reconnect to the server, it might be called after the server relieve a handshake
    public Boolean tryConnect() {
        isConnected = true;
        String serverAddress = CfgPeeker.getAddressByID(serverID);
        Integer serverPort = Integer.valueOf(CfgPeeker.getPortByID(serverID));
        try {
            socket = new Socket(serverAddress, serverPort);
            System.out.println(serverID + " connect to " + serverAddress + ":" + serverPort + " succeed");
        } catch (IOException e) {
            isConnected = false;
            //System.out.println(serverID + " connect to " + serverAddress + ":" + serverPort + " failed");
        }
        return isConnected;
    }

    private PrintWriter getWriter(Socket socket) throws IOException {
        return new PrintWriter(socket.getOutputStream(), true);
    }

    private BufferedReader getReader(Socket socket) throws IOException {
        return new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public synchronized void wakeUp() {
        System.out.println("client " + myID + "<--" + serverID + ": waking up");
        awake = true;
        this.notify();
    }

    public synchronized void fallSleep() throws InterruptedException {
        System.out.println("client " + myID + "<--" + serverID + ": falling asleep");
        awake = false;
        this.wait();
    }


    public void handShake() {

        if (isShake == 0 && isConnected == false) {
            tryConnect();
        }
        if (isShake == 0 && isConnected == true) {
            System.out.println("client " + myID + "-->" + serverID + ": trying to connect");
            try {
                isShake = HandShake.sendShake(socket, myID, serverID);
                if (isShake != 0) {
                    System.out.println("client " + myID + "-->" + isShake + ": finished handshaking");
                } else {
                    System.out.println("client " + myID + "-->" + isShake + ": failed in handshaking");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (isShake != 0) { // handshaking finished
                System.out.println("client " + myID + "<-->" + isShake + ": connection established, server:(" + socket.getInetAddress() + ":" + socket.getPort() + ")");
            }

            if (isShake == 0) {
                System.out.println("client " + myID + "<-->" + serverID + ": failed in connection, please try again");
            }
        }
    }


    public void run() {
        ActualMessage message = null;
        boolean isClosed = false;
        while (true) {

            if (clearSpeed == true) {
                speed = 0;
                clearSpeed = false;
            }
            //System.out.println("I am alive");
//            Boolean nothingDone = true;
//
//            if (allFinished == true) {
//                try {
//                    System.out.println("client " + myID + "-->" + serverID + ": all things are done, slept");
//                    awake = false;
//                    this.wait();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                break;
//            }


            boolean flag = true;
            for (boolean b : this.peer.uniqueBitField) {
                if (b == false) {
                    flag = false;
                }
            }
            if (flag) {

                FileUtilities.combineFilesIntoOne("peer" + myID + "/", CfgPeeker.getFileName());
                this.peer.fileComplete = true;
                try {
                    if (this.socket != null && this.socket.isConnected()) {
                        this.socket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            // the things after connected
            if (isConnected == true) {


                try {
                    socket.sendUrgentData(0xFF);
                } catch (Exception ex) {
                    System.out.println("client " + myID + "<-->" + serverID + ": disconnected");
                    try {
                        isClosed = true;
                        this.socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (isClosed == true) {
                    break;
                }


                //receive part

                message = SendReceiveMessage.receiveMessage(this.socket);

                if (message != null) {
                    System.out.println("client " + myID + "<--" + serverID + ": message received");
                    switch (message.type) {
                        case (0): {
                            System.out.println("client " + myID + "<--" + serverID + ": I'm choked");
                            this.isChoked = true;
                            break;
                        }
                        case (1): {
                            System.out.println("client " + myID + "<--" + serverID + ": I'm unchoked");
                            this.isChoked = false;
                            break;
                        }
                        case (2): {
                            break;
                        }
                        case (3): {
                            break;
                        }
                        case (4): {
                            // have
                            if (this.serverBitField != null) {
                                Integer indexFromPayload = ActualMessage.byteArrayToInt(message.payload);
                                System.out.println("client " + myID + "<--" + serverID + ": 'have' " + indexFromPayload + " received, updating the bitField");
                                this.serverBitField[indexFromPayload] = true;
                                this.isInterest = BitField.bitFieldDiffer(this.peer.uniqueBitField, this.serverBitField);
                                if (this.isInterest == true) {
                                    System.out.println("client " + myID + "-->" + serverID + ": I'm interested with you");
                                    SendReceiveMessage.sendMessage(this.socket, new ActualMessage(1, 2, null));
                                }
                            }
                            break;
                        }
                        case (5): {
                            //bitField received
                            this.serverBitField = BitField.byteArrayToBooleanArrayForBitField(message.payload);
                            System.out.println("client " + myID + "<--" + serverID + ": BitField stored");
                            System.out.println(ActualMessage.booleanArrayToString(this.serverBitField));
                            boolean interested = BitField.bitFieldDiffer(this.peer.uniqueBitField, this.serverBitField);
                            if (interested == true) {
                                this.isInterest = true;
                                System.out.println("client " + myID + "-->" + serverID + ": I'm interested with you");
                                SendReceiveMessage.sendMessage(this.socket, new ActualMessage(1, 2, null));
                            } else {
                                this.isInterest = false;
                                System.out.println("client " + myID + "-->" + serverID + ": I'm not interested with you");
                                SendReceiveMessage.sendMessage(this.socket, new ActualMessage(1, 3, null));
                            }


                            break;
                        }
                        case (6): {
                            break;
                        }
                        case (7): {
                            //piece received
                            speed++;
                            byte[] indexBytes = Arrays.copyOfRange(message.payload, 0, 4);
                            byte[] fileBytes = Arrays.copyOfRange(message.payload, 4, message.length - 1);
                            Integer index = ActualMessage.byteArrayToInt(indexBytes);
                            System.out.println(index + " received");
                            if (this.peer.uniqueBitField[index] == false) {
                                //System.out.println(ActualMessage.booleanArrayToString(this.peer.uniqueBitField));

                                //Integer fileLength = (message.length - 1);
                                System.out.println("client " + myID + "-->" + serverID + ": " + index + " piece received");
                                FileOutputStream os = null;
                                try {
                                    os = new FileOutputStream("peer" + myID + "/" + index);
                                    os.write(fileBytes);
                                    this.peer.uniqueBitField[index] = true;
                                    os.close();

                                    // tell server of same peer to send "have" to all clients
                                } catch (IOException e) {
                                    this.peer.uniqueBitField[index] = false;
                                    e.printStackTrace();
                                }
                                System.out.println(index + " written");
                                peerCommands.append('4');
                                haveQueue.add(index);
                                System.out.println(ActualMessage.booleanArrayToString(this.peer.uniqueBitField));
                            }


                            boolean flag2 = true;
                            for (boolean b : this.peer.uniqueBitField) {
                                if (b == false) {
                                    flag2 = false;
                                }
                            }
                            if (flag2) {

                                FileUtilities.combineFilesIntoOne("peer" + myID + "/", CfgPeeker.getFileName());
                                this.peer.fileComplete = true;
                                try {
                                    this.socket.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }


                            break;
                        }
                        default: {
                            System.out.println("client " + myID + "-->" + serverID + ": unknown message received");
                            break;
                        }
                    }
                } else {

                    // if there is no message received

                    if (this.isInterest != null && this.isInterest == true && isChoked == false) {
                        if (this.serverBitField != null) {
                            Integer index = BitField.getRandomDiffIndex(this.peer.uniqueBitField, serverBitField);
                            if (index != -1) {
                                ActualMessage sendMsg = ActualMessage.generateRequest(index);
                                SendReceiveMessage.sendMessage(this.socket, sendMsg);
                                System.out.println("client " + myID + "-->" + serverID + ": " + index + " sending piece request");
                            } else {
                                ActualMessage sendMsg = new ActualMessage(1, 3, null);
                                SendReceiveMessage.sendMessage(this.socket, sendMsg);
                                this.isInterest = false;
                                System.out.println(ActualMessage.booleanArrayToString(this.peer.uniqueBitField));
                                System.out.println(ActualMessage.booleanArrayToString(this.serverBitField));
                                System.out.println("client " + myID + "-->" + serverID + ": " + index + " I'm not interested with you now");

                            }
                        }
                    }
                }



            }


            if (commands.length() > 0) {

                if (commands.charAt(0) == '8' && isShake == 0) {
                    //System.out.println("shake");
                    handShake();
                    commands.delete(0, 1);
                } else if (commands.charAt(0) == '8' && isShake != 0) { //ignore command
                    //System.out.println("ignore");
                    commands.delete(0, 1);
                }


            } else {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    public static void main(String[] args) {
//        BitClient bitClient;
//        if (args.length > 0) {
//            bitClient = new BitClient(args[0], args[1]);
//        } else {
//            bitClient = new BitClient("1002", "1001");
//        }
//        bitClient.start();
//        bitClient.wakeUp();
    }


}