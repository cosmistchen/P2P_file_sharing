import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by xueliu on 4/12/15.
 */
public class CfgPeeker {
    private static File peerInfoFile = new File("PeerInfo.cfg");
    private static File commonFile = new File("Common.cfg");


    public static String getPortByID(String peerID) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(peerInfoFile));
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                String[] info = tempString.split(" ");
                if (info[0].equals(peerID)) {
                    reader.close();
                    return info[2];
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return null;
    }


    public static String getAddressByID(String peerID) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(peerInfoFile));
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                String[] info = tempString.split(" ");
                if (info[0].equals(peerID)) {
                    reader.close();
                    return info[1];
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return null;
    }

    public static ArrayList getOtherUncompletePeerID(String peerID) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(peerInfoFile));
            String tempString = null;
            ArrayList otherPeers = new ArrayList();
            while ((tempString = reader.readLine()) != null) {
                String[] info = tempString.split(" ");
                if ((!info[0].equals(peerID)) && (!info[3].equals("1"))) {
                    otherPeers.add(info[0]);
                }
            }
            reader.close();
            return otherPeers;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return null;
    }

    public static ArrayList getOtherPeerID(String peerID) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(peerInfoFile));
            String tempString = null;
            ArrayList otherPeers = new ArrayList();
            while ((tempString = reader.readLine()) != null) {
                String[] info = tempString.split(" ");
                if ((!info[0].equals(peerID)) ) {
                    otherPeers.add(info[0]);
                }
            }
            reader.close();
            return otherPeers;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return null;
    }

    public static Integer getUncompletePeerCount() {
        BufferedReader reader = null;
        Integer peerCount = 0;
        try {
            reader = new BufferedReader(new FileReader(peerInfoFile));
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                String[] info = tempString.split(" ");
                if (!info[3].equals("1")) {
                    peerCount++;
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return peerCount;
    }


    public static Integer getPeerCount() {
        BufferedReader reader = null;
        Integer peerCount = 0;
        try {
            reader = new BufferedReader(new FileReader(peerInfoFile));
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                peerCount++;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return peerCount;
    }

    public static boolean isCompleteAtBegainning(String peerID) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(peerInfoFile));
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                String[] info = tempString.split(" ");
                if (info[0].equals(peerID)) {
                    if (info[3].equals("1")) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return false;
    }

    public static Integer getBitsNumberOfBitField() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(commonFile));
            String tempString = null;
            double fileSize = 0.0;
            double pieceSize = 0.0;
            double nBits = 0.0;
            while ((tempString = reader.readLine()) != null) {
                String[] info = tempString.split(" ");
                if (info[0].equals("FileSize")) {
                    fileSize = Double.valueOf(info[1]);
                }
                if (info[0].equals("PieceSize")) {
                    pieceSize = Double.valueOf(info[1]);
                }
            }
            reader.close();
            nBits = Math.ceil(fileSize / pieceSize);
            return (int) nBits;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return 0;
    }

    public static Integer getBytesNumberOfBitField() {
        return (int) Math.ceil(((double) CfgPeeker.getBitsNumberOfBitField()) / 8.0);
    }

    public static String getFileName() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(commonFile));
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                String[] info = tempString.split(" ");
                if (info[0].equals("FileName")) {
                    reader.close();
                    return info[1];
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return null;
    }

    public static String getPieceSize() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(commonFile));
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                String[] info = tempString.split(" ");
                if (info[0].equals("PieceSize")) {
                    reader.close();
                    return info[1];
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return null;
    }

    public static String getUnchokingInterval() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(commonFile));
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                String[] info = tempString.split(" ");
                if (info[0].equals("UnchokingInterval")) {
                    reader.close();
                    return info[1];
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return null;
    }


    public static String getNumberOfPreferredNeighbors() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(commonFile));
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                String[] info = tempString.split(" ");
                if (info[0].equals("NumberOfPreferredNeighbors")) {
                    reader.close();
                    return info[1];
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return null;
    }

    public static String getOptimisticUnchokingInterval() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(commonFile));
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                String[] info = tempString.split(" ");
                if (info[0].equals("OptimisticUnchokingInterval")) {
                    reader.close();
                    return info[1];
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return null;
    }

    public static void main(String args[]) {
        System.out.println(CfgPeeker.getBytesNumberOfBitField());
    }
}
