import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

/**
 *  每次连接都是一条路，每条路都要看一眼
 *  非堵塞nio
 *  nc ip post
 */
public class SocketNIO {
    public static void main(String[] args) {
        List<SocketChannel> clients=new LinkedList<>();
        try {
            //打开一个ServerSocket管道
            ServerSocketChannel ss=ServerSocketChannel.open();
            //绑定
            ss.bind(new InetSocketAddress(9090));
            //重点配置堵塞和非堵塞模式
            ss.configureBlocking(false);
            //轮询
            while (true){
                //sleep 1秒钟
                Thread.sleep(1000);
                SocketChannel client = ss.accept();//不堵塞
                if(client==null){
                    //没有客户端每秒钟控制台打印null
                    System.out.println("null.......");
                } else {
                    //有客服端请求，同样设置为非堵塞
                    client.configureBlocking(false);
                    int port = client.socket().getPort();
                    System.out.println("port"+port);
                    clients.add(client);
                }
                //缓冲区，防止内存碎片有利于GC回收
                ByteBuffer byteBuffer=ByteBuffer.allocateDirect(4096);
                for (SocketChannel socketChannel : clients) {
                    int read = socketChannel.read(byteBuffer);
                    if(read>0){
                        byteBuffer.flip();
                        byte [] bytes=new byte[byteBuffer.limit()];
                        byteBuffer.get(bytes);

                        String result=new String(bytes);
                        System.out.println(socketChannel.socket().getPort()+"--->>"+result);
                        byteBuffer.clear();
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
