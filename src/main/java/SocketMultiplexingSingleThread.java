import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

public class SocketMultiplexingSingleThread {
    // {channel,byteBuffer,selector(多路复用器！！)}
    private  ServerSocketChannel server;
    private Selector selector;
    int post=9090;

    public  void  initService(){
        try {
            server = ServerSocketChannel.open();
            server.configureBlocking(false);
            server.bind(new InetSocketAddress(post));

            //生成一个多路复用器
            selector = Selector.open();
            //server注册到多路复用器中，去接收建立连接的客服端
            server.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void start() {
        initService();
        System.out.println("serviceStart");
        try{
            while (true){
                while(selector.select(0)>0){//问内核有没有事件，内核回复有
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectionKeys.iterator();
                    while (iterator.hasNext()){
                        SelectionKey key = iterator.next();
                        iterator.remove();

                        if (key.isAcceptable()){
                            acceptHandler(key);
                        } else if(key.isReadable()){
                            readHandel(key);
                        }
                    }
                }
            }

        } catch (IOException e){
          e.printStackTrace();
        }
    }

    private void acceptHandler(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel client = serverSocketChannel.accept();
        client.configureBlocking(false);
        // channel和byteBuffer一对一绑定
        ByteBuffer byteBuffer=ByteBuffer.allocateDirect(4096);
        // 客户端注册到多路复用器
        client.register(selector,SelectionKey.OP_READ,byteBuffer);
        System.out.println("新客户端："+client.getRemoteAddress());
    }

    private void readHandel(SelectionKey key) throws IOException {
        SocketChannel client =(SocketChannel) key.channel();
        ByteBuffer byteBuffer = (ByteBuffer) key.attachment();
        byteBuffer.clear();
        while (true){
            //把客服端发送的数据读给byteBuffer
            int read = client.read(byteBuffer);
            if(read>0){
                byteBuffer.flip();
                while (byteBuffer.hasRemaining()){
                    //写给客服端，显示在控制台
                    client.write(byteBuffer);
                }
                byteBuffer.clear();
            } else if (read==0){
                break;
            } else {
                client.close();
                break;
            }
        }
    }

    public static void main(String[] args) {
        SocketMultiplexingSingleThread thread=new SocketMultiplexingSingleThread();
        thread.start();
    }
}
