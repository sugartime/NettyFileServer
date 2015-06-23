 /*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.example.file;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.PropertyConfigurator;



/**
 * Server that accept the path of a file an echo back its content.
 */
public final class FileServer {

    
    //static final File f_certificate = new File("src/server.pem");
	//static final File f_privatekey = new File("src/serverkey.pem");
	
   		
	//static File f_certificate;
	//final File f_privatekey = new File("src/serverkey.pem");
		
	
	private boolean mIsSsl;
	private int mPort;
	
			
	
	
	public FileServer(boolean isSsl) {
		 
		this.mIsSsl	= isSsl;
		this.mPort = (this.mIsSsl ? FileServerConstants.SSL_PORT : FileServerConstants.PORT);
		
		System.out.println("mIsSsl :"+mIsSsl+" mPort:"+mPort);
		
    }

	
	public void run() throws Exception {
		
		
		File f_certificate = new File(getClass().getResource("server.pem").toURI());
		File f_privatekey  = new File(getClass().getResource("serverkey.pem").toURI());

		// Configure SSL.
        final SslContext sslCtx;
        if (mIsSsl) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        	//sslCtx = SslContextBuilder.forServer(f_certificate, f_privatekey,"12345").build();
        } else {
            sslCtx = null;
        }

        // Configure the server.
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .option(ChannelOption.SO_BACKLOG, 100)
             .handler(new LoggingHandler(LogLevel.INFO))
             .childHandler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 public void initChannel(SocketChannel ch) throws Exception {
                     ChannelPipeline p = ch.pipeline();
                     if (sslCtx != null) {
                         p.addLast(sslCtx.newHandler(ch.alloc()));
                     }
                     p.addLast(
                             //new StringEncoder(CharsetUtil.UTF_8),
                             //new LineBasedFrameDecoder(8192),
                             //new StringDecoder(CharsetUtil.UTF_8),
                    		 //new FileDecoder(),
                             new ChunkedWriteHandler(),
                    		 //new FileServerChunkedHandler());
                             new FileServerHandler());
                 }
             });

            // Start the server.
            ChannelFuture f = b.bind(mPort).sync();

            // Wait until the server socket is closed.
            f.channel().closeFuture().sync();
        } finally {
            // Shut down all event loops to terminate all threads.
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
		
	}
	 
    public static void main(String[] args) throws Exception {
    	
    	PropertyConfigurator.configure("resources/log4j.properties");
    	
    	// true : ssl, false : modern
    	new FileServer(false).run();
    }
}