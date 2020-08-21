package netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;


public class NettyServiceHandler extends ChannelInboundHandlerAdapter {
    /**
     * 功能：读取客服端发送过来的信息
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //读取数据
        System.out.println("接收客户端数据");
        ByteBuf buf = (ByteBuf) msg;
        byte[] con = new byte[buf.readableBytes()];
        buf.readBytes(con);
        System.out.println(new String(con, "UTF8"));
        //向客户端写数据
        System.out.println("server向client发送数据");
        String str="Netty 1213";
        byte[] req = str.getBytes("UTF-8");
        ByteBuf pingMessage = Unpooled.buffer();
        pingMessage.writeBytes(req);
        ctx.writeAndFlush(pingMessage);
        System.out.println(str);
    }
}
