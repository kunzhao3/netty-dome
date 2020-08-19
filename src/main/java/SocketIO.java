import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 每一个连接对应一个线程
 * nc ip post
 */
public class SocketIO {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket=new ServerSocket(9090);
            System.out.println("Set1:new ServerSocket(9090)");
            while (true){
                final Socket client = serverSocket.accept();//堵塞
                System.out.println("Set2:"+client.getPort());
                /* //多线程访问
                new Thread(){
                    @Override
                    public void run() {
                        read(client);
                    }
                }.start();*/
                read(client);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void read(Socket client){
        try {
            InputStream inputStream = client.getInputStream();
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
            String read = bufferedReader.readLine();//堵塞
            System.out.println(read);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
