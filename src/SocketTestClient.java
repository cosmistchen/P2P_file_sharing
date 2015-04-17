import java.io.IOException;
import java.net.Socket;

/**
 * Created by xueliu on 4/11/15.
 */
public class SocketTestClient{
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("127.0.0.1",8000);
    }
}
