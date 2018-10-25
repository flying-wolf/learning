package com.machao.learning.io.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class BioServerTask implements Runnable {
	
	private Socket socket;
	private InputStream inputStream;
	private OutputStream outputStream;
	

	public BioServerTask(Socket socket) {
		this.socket = socket;
	}


	@Override
	public void run() {
		try {
			inputStream = socket.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			
			String lineStr = reader.readLine();
			socket.shutdownInput();
			
			System.out.println("BioServerTask -> reader.readLine() 读到数据：" + lineStr);
			
			String result = "hello";
			
			if(lineStr.equals("mmp")) {
				result = "cnm";
				BioServerTest.setRun(false);
			}
			
			outputStream = socket.getOutputStream();
			
			PrintWriter writer = new PrintWriter(outputStream);
			writer.write(result);
			writer.flush();
			socket.shutdownOutput();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}

}
