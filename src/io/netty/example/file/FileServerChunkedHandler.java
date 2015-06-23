package io.netty.example.file;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.stream.ChunkedFile;

public class FileServerChunkedHandler extends SimpleChannelInboundHandler<ChunkedFile>{

	 	@Override
	    public void channelActive(ChannelHandlerContext ctx) {
	        System.out.println("in channel active method");
	    }

	    @Override
	    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
	        cause.printStackTrace();

	        if (ctx.channel().isActive()) {
	            ctx.writeAndFlush("ERR: " +
	                    cause.getClass().getSimpleName() + ": " +
	                    cause.getMessage() + '\n').addListener(ChannelFutureListener.CLOSE);
	        }
	    }

	    @Override
	    protected void channelRead0(ChannelHandlerContext ctx, ChunkedFile msg)
	            throws Exception {
	        System.out.println("in channelRead0");

	    }

	    @Override
	    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
	    	 System.out.println("in channelRead");

	        ByteBuf buf = (ByteBuf) msg;
	        byte[] bytes = new byte[buf.readableBytes()];
	        buf.readBytes(bytes);
	    }
	
	
}
