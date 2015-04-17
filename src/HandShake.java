import java.io.*;
import java.net.Socket;
import java.util.Arrays;

/**
 * Created by xueliu on 4/13/15.
 */
public class HandShake {
    private byte[] msg;

    public byte[] getMsg() {
        return msg;
    }

    public static byte[] intToByteArray(int i) {
        byte[] result = new byte[4];
        result[0] = (byte) ((i >> 24) & 0xFF);
        result[1] = (byte) ((i >> 16) & 0xFF);
        result[2] = (byte) ((i >> 8) & 0xFF);
        result[3] = (byte) (i & 0xFF);
        return result;
    }

    public static int byteArrayToInt(byte[] bytes) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (bytes[i] & 0x000000FF) << shift;
        }
        return value;
    }

    public static byte[] encode(String id){
        byte bytes[] = new byte[32];
        String prefix = "P2PFILESHARINGPROJ";
        byte prefixBytes[] = prefix.getBytes();
        int i = 0;

        // fill zeros into the front of 18 bytes
        for (; i < (18 - prefixBytes.length); i++) {
            bytes[i] = (byte) 0;
        }

        for (; i < prefixBytes.length; i++) {
            bytes[i] = prefixBytes[i];
        }

        int j = i + 10;
        for (; i < j; i++) {
            bytes[i] = (byte) 0;
        }

        int intID = Integer.valueOf(id);
        byte myIDBytes[] = intToByteArray(intID);

        // fill zeros into the front of 18 bytes
        for (; i < 32; i++) {
            bytes[i] = myIDBytes[i - 28];
        }
        return bytes;
    }

    public HandShake(String myID) {
        this.msg = encode(myID);
    }

    public static Integer sendShake(Socket socket,String myID,String serverID) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        byte sendBytes[] = encode(myID);
        byte receiveBytes[] = new byte[32];
        Integer receivedID = 0;
        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            outputStream.write(sendBytes, 0, sendBytes.length);
            outputStream.flush();
            inputStream.read(receiveBytes);
        } catch (IOException e) {
            return 0;
        }
        receivedID = byteArrayToInt(Arrays.copyOfRange(receiveBytes,28,32));
        //System.out.println(receivedID);
        if (receivedID.equals(Integer.valueOf(serverID))){
            return receivedID;
        }else {
            return 0;
        }

    }

    public static Integer receiveShake(Socket socket,String myID) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        byte sendBytes[] = encode(myID);
        byte receiveBytes[] = new byte[32];
        Integer validID = 0;
        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            inputStream.read(receiveBytes);
            String validateString = new String(Arrays.copyOfRange(receiveBytes,0,18), "UTF-8");
            if (validateString.equals("P2PFILESHARINGPROJ")){
                outputStream.write(sendBytes, 0, sendBytes.length);
                outputStream.flush();
                validID = byteArrayToInt(Arrays.copyOfRange(receiveBytes,28,32));
            }
        } catch (IOException e) {
            return 0;
        }
        return validID;

    }

    public static void main(String args[]) {
        HandShake handShake = new HandShake("1101");
        for (int i = 0; i < handShake.getMsg().length; i++) {
            System.out.println((handShake.getMsg()[i]));
        }

    }
}
