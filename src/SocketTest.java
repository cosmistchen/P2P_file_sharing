import java.io.IOException;
import java.net.ServerSocket;

/**
 * Created by xueliu on 4/11/15.
 */
public class SocketTest {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8000);
        serverSocket.accept();
    }
}
