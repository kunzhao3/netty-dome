package netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.UnsupportedEncodingException;


public class NettyClientHandler extends ChannelInboundHandlerAdapter {
    /**
     * 功能：客户端连接服务器后被调用,向服务器发送信息
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx){
        String str="你好，服务器";
        byte[] data = str.getBytes();
        ByteBuf firstMessage  = Unpooled.buffer();
        firstMessage.writeBytes(data);
        ctx.writeAndFlush(firstMessage);
        System.out.println("客户端向服务器发送信息:"+str);
    }

    /**
     * 功能：读取服务器返回的信息
     */
    @Override
    public  void channelRead(ChannelHandlerContext ctx, Object msg)  {
        ByteBuf buf = (ByteBuf) msg;
        byte[] con = new byte[buf.readableBytes()];
        buf.readBytes(con);
        String str = null;
        try {
            str = new String(con, "UTF8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        System.out.println("客户端接收到服务器的消息:"+str);
    }
}
