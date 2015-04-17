import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;

/**
 * Created by xueliu on 4/16/15.
 */
public class peerTimer extends Thread {
    public peerProcess peer;

    public peerTimer(peerProcess peer) {
        this.peer = peer;
    }

    public void run() {
        Timer unchokingTimer = new Timer();
        unchokingTimer.schedule(new unchokingTask(this.peer), 1000, Integer.valueOf(CfgPeeker.getUnchokingInterval()) * 1000);

        Timer optimisticTimer = new Timer();
        optimisticTimer.schedule(new optimisticTask(this.peer), 1000, Integer.valueOf(CfgPeeker.getOptimisticUnchokingInterval()) * 1000);

        while (true) {
            // to stop timer
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static class unchokingTask extends java.util.TimerTask {
        private peerProcess peer;

        public unchokingTask(peerProcess peer) {
            this.peer = peer;
        }

        public void run() {
            if (this.peer.fileComplete == true) {
                ArrayList<Integer> interestServersIndex = new ArrayList<Integer>();
                for (Integer i = 0; i < this.peer.servers.size(); i++) {
                    if (this.peer.servers.get(i).isInterested == true) {
                        interestServersIndex.add(i);
                    }
                }
                if (interestServersIndex.size() > 0) {
                    int max = Integer.valueOf(CfgPeeker.getNumberOfPreferredNeighbors());
                    if (interestServersIndex.size() > max) {
                        java.util.Random random = new java.util.Random();
                        while (interestServersIndex.size() > max) {
                            interestServersIndex.remove(random.nextInt(interestServersIndex.size()));
                        }
                    }
                }
                for (int i = 0; i < this.peer.servers.size(); i++) {
                    boolean flag = false;
                    for (int j = 0; j < interestServersIndex.size(); j++) {
                        if (i == interestServersIndex.get(j)) {
                            flag = true;
                        }
                    }
                    if (flag == true && this.peer.servers.get(i).isChoked == true) {
                        this.peer.servers.get(i).doUnchoke = true;
                    }
                    if (flag == false && this.peer.servers.get(i).isChoked == false) {
                        this.peer.servers.get(i).isChoked = true;
                    }
                }
            } else {
                int max = Integer.valueOf(CfgPeeker.getNumberOfPreferredNeighbors());

                ArrayList<Integer> interestOtherPeerIndex = new ArrayList<Integer>();
                for (Integer i = 0; i < this.peer.servers.size(); i++) {
                    if (this.peer.servers.get(i).isInterested == true) {
                        interestOtherPeerIndex.add(this.peer.servers.get(i).clientID);
                    }
                }

                ArrayList<Integer> fastOtherPeerID = new ArrayList<Integer>();
                ArrayList<Integer> fastOtherPeerSpeed = new ArrayList<Integer>();
                for (int i = 0; i < this.peer.clients.size(); i++) {
                    if (fastOtherPeerID.size() < max) {
                        boolean flag = false;
                        for (int j = 0; j < interestOtherPeerIndex.size(); j++) {
                            if (this.peer.clients.get(i).serverID.equals(  interestOtherPeerIndex.get(j)    )) {
                                flag = true;
                            }
                        }
                        if(flag == true){
                            fastOtherPeerID.add(Integer.valueOf(this.peer.clients.get(i).serverID));
                            fastOtherPeerSpeed.add(this.peer.clients.get(i).speed);
                        }
                    }else{
                        boolean in = false;
                        for (int j=0; j<fastOtherPeerID.size();j++){
                            if(in==false && this.peer.clients.get(i).speed > fastOtherPeerSpeed.get(j)){
                                fastOtherPeerID.remove(j);
                                fastOtherPeerSpeed.remove(j);
                                fastOtherPeerID.add(Integer.valueOf(this.peer.clients.get(i).serverID));
                                fastOtherPeerSpeed.add(this.peer.clients.get(i).speed);
                                in = true;
                            }
                        }
                    }
                }

                for (int i=0;i<this.peer.servers.size();i++){
                    boolean flag = false;
                    for (int j=0;j<fastOtherPeerID.size();j++){
                        if (this.peer.servers.get(i).clientID.equals(fastOtherPeerID.get(j))){
                            flag = true;
                        }
                    }
                    if (flag == true && this.peer.servers.get(i).isChoked == true) {
                        this.peer.servers.get(i).doUnchoke = true;
                    }
                    if (flag == false && this.peer.servers.get(i).isChoked == false) {
                        this.peer.servers.get(i).isChoked = true;
                    }
                }

            }
        }
    }

    static class optimisticTask extends java.util.TimerTask {
        private peerProcess peer;

        public optimisticTask(peerProcess peer) {
            this.peer = peer;
        }

        public void run() {

            ArrayList<Integer> interestServersIndex = new ArrayList<Integer>();
            for (Integer i = 0; i < this.peer.servers.size(); i++) {
                if (this.peer.servers.get(i).isInterested == true) {
                    interestServersIndex.add(i);
                }
            }

            if (interestServersIndex.size() > 0){
                java.util.Random random = new java.util.Random();
                Integer randomIndex = interestServersIndex.get(random.nextInt(interestServersIndex.size()));


                for (int i = 0; i < interestServersIndex.size(); i++) {
                    if (this.peer.servers.get(interestServersIndex.get(i)).isOptimisticChoked == true && i == randomIndex) {
                        this.peer.servers.get(interestServersIndex.get(i)).doOtimisticUnchoked = true;
                    } else {
                        this.peer.servers.get(interestServersIndex.get(i)).isOptimisticChoked = true;
                    }
                }
            }
        }
    }


}
