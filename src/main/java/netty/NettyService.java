package netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

public class NettyService {
    public  void start(String hostname,Integer post){
        ServerBootstrap serverBootstrap=new ServerBootstrap();
        EventLoopGroup boss=new NioEventLoopGroup();
        EventLoopGroup work = new NioEventLoopGroup();
        try {
            serverBootstrap.group(boss,work);
            serverBootstrap.channel(NioServerSocketChannel.class);
            // 设置参数，TCP参数
            serverBootstrap.option(ChannelOption.SO_BACKLOG, 2048);// serverSocketchannel的设置，链接缓冲池的大小
            serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);// socketchannel的设置,维持链接的活跃，清除死链接
            serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);// socketchannel的设置,关闭延迟发送
            serverBootstrap.childHandler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel channel) throws Exception {
                    channel.pipeline().addLast(new NettyServiceHandler()); // 客户端触发操作
                }
            });
            // 绑定端口，并且返回ChannelFuture对象
            ChannelFuture channelFuture = serverBootstrap.bind(new InetSocketAddress(hostname, post));
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            try {
                boss.shutdownGracefully().sync();
                work.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        NettyService nettyService=new NettyService();
        nettyService.start("localhost",8080);
    }
}
