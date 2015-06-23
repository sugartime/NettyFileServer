/*
 * Copyright 2014 The Netty Project
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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.commons.lang3.RandomStringUtils;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;

public class FileServerHandler extends SimpleChannelInboundHandler<Object>  {
	
	
	//private Logger logger = Logger.getLogger(this.getClass().getName());
	
	private Logger logger = Logger.getLogger(this.getClass());
	
	//DB
	private static BoneCP connectionPool = null;
	
		
	public static final int ID_LENGTH = 10;
	
	private ByteBuf mBuf;

	long mTotal;
	
	int mNameLen;
	
	boolean mIsInitFile;
	
	byte[] mBytes;
	
	long mFileLength;
	
	String mFileString;
	
	//생성자
	FileServerHandler(){
		initDbPool();
	}
	
	//디비풀링 초기화
	private void initDbPool(){
		logger.info("init db pool");
		
		try {
			Class.forName(FileServerConstants.DB_DRIVER);
 
		} catch (ClassNotFoundException e) {
			logger.info(e.getMessage());
		}
		
		try {
			// setup the connection pool
			BoneCPConfig config = new BoneCPConfig();
			config.setJdbcUrl(FileServerConstants.DB_CONNECTION);
			config.setUsername(FileServerConstants.DB_USER); 
			config.setPassword(FileServerConstants.DB_PASSWORD);
			config.setMinConnectionsPerPartition(5);
			config.setMaxConnectionsPerPartition(10);
			config.setPartitionCount(1);
			connectionPool = new BoneCP(config); 
			
		}catch (SQLException e) {
			logger.info(e.getMessage());
		}
		
	}
	
	private void insertRecordIntoTable(String fileName,String newFileName) throws SQLException {
		 
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;
 
		String sql = "INSERT INTO T_FILE_UPLOAD"
				+ "(F_NO, F_FILE_NAME, F_NEW_FILE_NAME, F_DATETIME, F_YMD) VALUES"
				+ "(null,?,?,?,?)";
 
		try {
			dbConnection = connectionPool.getConnection();
			preparedStatement = dbConnection.prepareStatement(sql);
			preparedStatement.setString(1, fileName);
			preparedStatement.setString(2, newFileName);
			preparedStatement.setTimestamp(3, getCurrentTimeStamp());
			preparedStatement.setString(4, getToday());
 
			// execute insert SQL stetement
			preparedStatement.executeUpdate();
 
			logger.info("Record is inserted into DBUSER table!");
 
		} catch (SQLException e) {
 
			logger.info(e.getMessage());
 
		} finally {
 
			if (preparedStatement != null) {
				preparedStatement.close();
			}
 
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
 
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) {
		 System.out.println("handlerAdded");
	        
		 mBuf = ctx.alloc().buffer(8192); // (1)
		 
		 mTotal = 0L;
		 
		 mIsInitFile=false;
		 
		 mBytes=null;
		 
		 mFileLength=0L;
		 
		 mFileString="";
	        
	 }

	
	
	
	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		System.err.println("handlerRemoved");
		mBuf.release();
	    mBuf = null;
	}



	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("channelActive");
	}
	
	

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("channelReadComplete");
		
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		// TODO Auto-generated method stub
		//System.out.println("channelRead");
		
		ByteBuf m = (ByteBuf) msg;
		
        mBuf.writeBytes(m);
                
        //m.release();
        
        System.out.println("mBuf.readableBytes() :"+mBuf.readableBytes());
                        
		System.out.println("mIsInitFile :"+mIsInitFile);
			
		if(mIsInitFile==false && mBuf.readableBytes()>=1024){
			
	        //파일이름 길이
	        mNameLen = mBuf.readInt();
	        System.out.println("mNameLen :"+mNameLen);
	        
	        //파일이름
	        mBytes = new byte[mNameLen];
	        mBuf.readBytes(mBytes);
	        
	        String file_string = "";
	        for (int i = 0; i < mNameLen; i ++) {
	          
	        	file_string += (char)mBytes[i];
	        }
	        String fileName=URLDecoder.decode(file_string,"UTF-8");
	        System.err.println("fileName :"+fileName);
	        
	        	        
	        	        
	        //파일전체길이
	        mFileLength=mBuf.readLong();
	        System.out.println("mFileLength :"+mFileLength);  
	        	        	        
	        
	        //공백으로 채워진 곳 읽기
	        byte[] zeroBytes = new byte[mBuf.readableBytes()];
	        mBuf.readBytes(zeroBytes);
	        	        
	        mIsInitFile=true;
	        
	        
	        //파일이름 변경
	        String[] arrFileName=fileName.split("\\.");
	        String fileNameHead = arrFileName[0];
	        String fileNameExt  = arrFileName[1];
	        System.out.println("fileNameHead "+fileNameHead);
	        System.out.println("fileNameExt "+fileNameExt);
	        
	        //String newFileName = generateUniqueId()+"."+fileNameExt;
	        String newFileName =  UUID.randomUUID().toString().replace("-", "")+"."+fileNameExt;
	        mFileString=newFileName;
	        
	        System.out.println("mFileString "+mFileString);
	        
	        //변경된 파일이름을 전송
	        String f_name ="";
	    	try {
	    		f_name = URLEncoder.encode(newFileName, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
	        ByteBuf buf=ctx.alloc().buffer(512);
	        buf.writeInt(f_name.length());					//파일이름 길이(4)
			buf.writeBytes(f_name.getBytes());				//파일이름 파일이름에따라 틀림
			buf.writeZero(buf.capacity()-buf.writerIndex()); 	//나머지 부분을 0으로 셋팅해서 버퍼크기를 맞춤
			ctx.writeAndFlush(buf);
			
	        //mBuf.markReaderIndex();
	        //System.out.println("mBuf.markReaderIndex :"+mBuf.readableBytes());
	        //System.out.println("mBuf.markReaderIndex :"+mBuf.markReaderIndex());
	        
		}
        
		//여기부터 파일 기록
		//System.out.println("m_IsInitFile :"+m_IsInitFile);
		if(mIsInitFile==true){
			
			System.err.println("File Write !!");
			
						
			FileOutputStream fos = new FileOutputStream("c:\\test\\"+mFileString,true);
						
			long len=mBuf.readableBytes();
			
			mTotal+=len;
			
			
			//System.out.println("len["+len+"] mTotal["+mTotal+"] mFileLength["+mFileLength+"]");
			
			byte b[]=new byte[(int)len];
			mBuf.readBytes(b);
			fos.write(b);
			fos.close();
			mBuf.clear();

			if(mTotal>=mFileLength){	
				
				System.err.println("=========>완료");
				//m_buf.release();
				
				
			}
		
		}
		
	    
	
	}
	


	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)	throws Exception {
		// TODO Auto-generated method stub
		cause.printStackTrace();
        ctx.close();
	}


	//uid생성
	public String generateUniqueId() {
	    return RandomStringUtils.randomAlphanumeric(ID_LENGTH);
	}
	
	private java.sql.Timestamp getCurrentTimeStamp() {
		 
		java.util.Date today = new java.util.Date();
		return new java.sql.Timestamp(today.getTime());
 
	}
	
	//오늘날짜 가져오기
	protected String getTodayDateTime()	{
		SimpleDateFormat formatter = new SimpleDateFormat ( "yyyyMMddHHmmss", Locale.KOREA );
		Date currentTime = new Date();
		String dTime = formatter.format ( currentTime );
		return dTime;
	}
	
	//오늘 가져오기
	protected String getToday()	{
			SimpleDateFormat formatter = new SimpleDateFormat ( "yyyyMMdd", Locale.KOREA );
			Date currentTime = new Date();
			String dTime = formatter.format ( currentTime );
			return dTime;
		}

	//현재달 가져오기
	protected String getMonth()	{
		SimpleDateFormat formatter = new SimpleDateFormat ( "yyyyMM", Locale.KOREA );
		Date currentTime = new Date();
		String dMonth = formatter.format ( currentTime );
		return dMonth;
	}

    
    
    
    
    
}
