import com.sun.org.apache.xpath.internal.operations.Bool;
import sun.management.snmp.jvminstr.JvmThreadInstanceEntryImpl;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by xueliu on 4/13/15.
 */
public class ActualMessage {

    public Integer length;
    public Integer type;
    public byte[] payload;
    public byte[] actualMsg;


    public Integer getLength() {
        return length;
    }

    public void send(){

    }


    public static byte[] getBooleanArray(byte b) {
        byte[] array = new byte[8];
        for (int i = 7; i >= 0; i--) {
            array[i] = (byte) (b & 1);
            b = (byte) (b >> 1);
        }
        return array;
    }

    public static String byteToBit(byte b) {
        return ""
                + (byte) ((b >> 7) & 0x1) + (byte) ((b >> 6) & 0x1)
                + (byte) ((b >> 5) & 0x1) + (byte) ((b >> 4) & 0x1)
                + (byte) ((b >> 3) & 0x1) + (byte) ((b >> 2) & 0x1)
                + (byte) ((b >> 1) & 0x1) + (byte) ((b >> 0) & 0x1);
    }

    public static Boolean validate(byte[] messageBytes) {
        byte[] fifthBytes = Arrays.copyOfRange(messageBytes, 4, 5);
        if (byteArrayToInt((Arrays.copyOfRange(messageBytes, 0, 4))) >= 0 && byteArrayToInt(fifthBytes) <= 7 && byteArrayToInt(fifthBytes) >= 0) {
            return true;
        } else {
            return false;
        }
    }

    public ActualMessage(Integer length, Integer type, byte[] messageBytes) {
        this.length = length;
        this.type = type;
        this.payload = messageBytes;
        encode(length, type, messageBytes);
    }

    public ActualMessage(byte[] messageBytes) {
        decode(messageBytes);
    }

    public byte[] getActualMsgInBytes() {
        return actualMsg;
    }

    public void decode(byte[] messageBytes) {
        this.length = byteArrayToInt(Arrays.copyOfRange(messageBytes, 0, 4));
        this.type = byteArrayToInt(new byte[]{(byte) 0, (byte) 0, (byte) 0, (Arrays.copyOfRange(messageBytes, 4, 5))[0]});
        this.payload = null;
        if (type >= 4) {
            this.payload = new byte[this.length-5];
            for (int i=5;i<(this.length);i++){
                this.payload[i-5] = messageBytes[i];
            }
        }
        this.actualMsg = messageBytes;

    }

    public void encode(Integer l, Integer t, byte[] m) { //generate actualMsg
        Integer actualLength = 4 + 1;

        if (type >= 4) { //4: have  6: request
            actualLength += m.length;
        }
        actualMsg = new byte[actualLength];
        byte[] temp = intToFourByteArray(l);
        for (int i = 0; i < 4; i++) {
            actualMsg[i] = temp[i];
        }
        actualMsg[4] = intToFourByteArray(t)[3];

        if (type >= 4) {
            for (int i = 5; i < actualLength; i++) {

                actualMsg[i] = m[i - 5];
            }
        }

    }


    public static byte[] intToFourByteArray(int i) {
        byte[] result = new byte[4];
        result[0] = (byte) ((i >> 24) & 0xFF);
        result[1] = (byte) ((i >> 16) & 0xFF);
        result[2] = (byte) ((i >> 8) & 0xFF);
        result[3] = (byte) (i & 0xFF);
        return result;
    }

    public static int byteArrayToInt(byte[] bytes) {
        if (bytes.length == 1){
            bytes = new byte[]{(byte) 0, (byte) 0, (byte) 0, bytes[0]};
        }
        if (bytes.length == 2){
            bytes = new byte[]{(byte) 0, (byte) 0, bytes[0]};
        }
        if (bytes.length == 3){
            bytes = new byte[]{(byte) 0, bytes[0]};
        }
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (bytes[i] & 0x000000FF) << shift;
        }
        return value;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < actualMsg.length; i++) {
            stringBuilder.append(byteToBit(actualMsg[i]));
        }
        return stringBuilder.toString();
    }

    public static String byteArrayToString(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            stringBuilder.append(byteToBit(bytes[i]));
        }
        return stringBuilder.toString();
    }


    public static void main(String args[]) {
        ActualMessage a1 = new ActualMessage(30, 5, new byte[]{3,1,8,8});
        ActualMessage a2 = new ActualMessage(a1.getActualMsgInBytes());
        System.out.print(a2.toString());
    }

    public static byte[] booleanArrayToByteArray(boolean[] b) {
        byte[] bytes = new byte[(b.length + 7) / 8];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = 0;
        }
        for (int i = 0; i < b.length; i++) {
            if (b[i] == true) {
                bytes[i / 8] |= 1 << (7 - i % 8);
            }
        }
        return bytes;
    }

    public static String booleanArrayToString(boolean[] b) {
        return byteArrayToString(booleanArrayToByteArray(b));
    }

    public static ActualMessage generateRequest(Integer index){
        return new ActualMessage(5,6,intToFourByteArray(index));
    }

    public static ActualMessage generatePiece(String id, Integer index){
        FileInputStream fileInputStream=null;
        File f = new File("peer"+id+"/"+index);
        byte[] bFile = new byte[(int) f.length()];
        try {
            //convert file into array of bytes

            fileInputStream = new FileInputStream(f);
            fileInputStream.read(bFile);
            fileInputStream.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        byte[] payload = new byte[((int) f.length())+4];
        byte[] indexBytes = ActualMessage.intToFourByteArray(index);
        for (int i=0;i<payload.length;i++){
            if (i<4){
                payload[i] = indexBytes[i];
            }else {
                payload[i] = bFile[i-4];
            }
        }
        ActualMessage msg = new ActualMessage((payload.length+1),7,payload);
        return msg;

    }
}
