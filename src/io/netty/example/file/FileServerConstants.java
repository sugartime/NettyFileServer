package io.netty.example.file;

public class FileServerConstants {
	
	static final String TAG = "File Server";
	
	/** DB */
	static final String DB_DRIVER = "com.mysql.jdbc.Driver";
	static final String DB_CONNECTION = "jdbc:mysql://127.0.0.1/file_server";
	static final String DB_USER = "tester";
	static final String DB_PASSWORD = "12345";
	
	/** NETTY */
	static final int PORT 		= 8023;
	static final int SSL_PORT 	= 8992;
	
	
}
