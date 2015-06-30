package io.netty.example.file;

public class FileServerConstants {
	
	static final String TAG = "File Server";
	
	/** DB */
	static final String DB_DRIVER = "com.mysql.jdbc.Driver";
	static final String DB_CONNECTION = "jdbc:mysql://127.0.0.1/file_server";
	static final String DB_USER = "tester";
	static final String DB_PASSWORD = "12345";
	
	/** NETTY */
	static final boolean  	IS_SSL 			= false; //SSL 동작여부
	static final int 		PORT 			= 8023;
	static final int 		SSL_PORT 		= 8992;
	static final int 		INIT_BUF_SIZE 	= 512;   //파일전송전 보내는 파일내용 버퍼 크기
	static final int 		SND_BUF_SIZE 	= 4096;  //파일전송 버퍼 크기
	
	
}
