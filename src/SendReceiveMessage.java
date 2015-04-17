import java.io.*;
import java.net.Socket;
import java.util.Arrays;

/**
 * Created by xueliu on 4/13/15.
 */
public class SendReceiveMessage {


    public static Integer sendMessage(Socket socket,ActualMessage msg) {
        OutputStream outputStream = null;
        try {
            outputStream = socket.getOutputStream();
            outputStream.write(msg.getActualMsgInBytes(), 0, msg.getActualMsgInBytes().length);
            outputStream.flush();
            return 1;
        } catch (IOException e) {
            return 0;
        }
    }

    public static ActualMessage receiveMessage(Socket socket) {
        InputStream inputStream = null;
        ActualMessage message = null;
        Integer messageLength = null;
        Integer messageType = null;
        byte firstBytes[] = new byte[4];
        byte secondBytes[] = new byte[1];


        byte[] payload = null;
        try {
            if((inputStream = socket.getInputStream()).available()>0){
                inputStream.read(firstBytes);
                messageLength = ActualMessage.byteArrayToInt(firstBytes);
                if (messageLength > 0){
                    inputStream.read(secondBytes);
                    messageType = ActualMessage.byteArrayToInt(secondBytes);
                    if (messageLength > 1){
                        payload = new byte[messageLength-1];
                        inputStream.read(payload);
                    }
                    message = new ActualMessage(messageLength,messageType,payload);
                }else {
                    return null;
                }
            }else {
                return null;
            }
        } catch (IOException e) {
            return null;
        }
        return message;
    }

    public static void main(String args[]) {


    }
}
