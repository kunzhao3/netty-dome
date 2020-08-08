import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class SockerMultiplexingThread {
    private ServerSocketChannel server;
    private Selector selector1;
    private Selector selector2;
    private Selector selector3;

    int post=9090;

    public void initService(){
        try {
            server = ServerSocketChannel.open();
            server.configureBlocking(false);
            server.bind(new InetSocketAddress(post));

            selector1 = Selector.open();
            server.register(selector1, SelectionKey.OP_ACCEPT);

            selector2 = Selector.open();
            selector3 = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SockerMultiplexingThread server=new SockerMultiplexingThread();
        server.initService();
        NioThread boss=new NioThread(server.selector1,2);
        NioThread worker0=new NioThread(server.selector2);
        NioThread worker1=new NioThread(server.selector3);

        boss.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        worker0.start();
        worker1.start();

        System.out.println("----------服务启动了------");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
class NioThread  extends Thread{
    Selector selector;
    static int selectors=0;
    int id=0;
    boolean boss=false;
    static BlockingQueue<SocketChannel>[] queue;
    static AtomicInteger idx=new AtomicInteger();

    public NioThread(Selector selector, int n) {
        this.selector = selector;
        this.selectors = n;
        queue=new LinkedBlockingDeque[selectors];
        for (int i = 0; i <n ; i++) {
            queue[i]=new LinkedBlockingDeque<>();
        }
        boss=true;
        System.out.println("Boss启动");
    }

    public NioThread(Selector selector) {
        this.selector = selector;
        id=idx.getAndIncrement()%selectors;
        System.out.println("Work:"+id+"启动");
    }

    @Override
    public void run() {
        try {
            while (true){
                while (selector.select(10)>0){
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
                if(!queue[id].isEmpty() && !boss){
                    ByteBuffer byteBuffer=ByteBuffer.allocateDirect(4096);
                    SocketChannel client = queue[id].take();
                    client.register(selector,SelectionKey.OP_READ,byteBuffer);
                    System.out.println("新客户端："+client.socket().getPort()+"分配的work："+id);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void acceptHandler(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel client = serverSocketChannel.accept();
        client.configureBlocking(false);
        int num=idx.getAndIncrement()%selectors;
        queue[num].add(client);
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

}