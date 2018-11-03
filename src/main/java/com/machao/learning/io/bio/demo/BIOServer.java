package com.machao.learning.io.bio.demo;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class BIOServer {
	
	private ServerSocket server;
	
	

	public BIOServer(int port) {
		try {
			server = new ServerSocket();
			server.bind(new InetSocketAddress(port));
			System.out.println(String.format("BIO服务端已启动，监听端口是：%d", port));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	public void listener() throws IOException {
		while(true) {
			Socket client = server.accept();
			
			InputStream input = client.getInputStream();
			byte[] buffer = new byte[1024];
			int len = input.read(buffer);
			if(len > 0) {
				String msg = new String(buffer, 0, len);
				System.out.println(String.format("收到：%s", msg));
			}
		}
		
	}
	


	public static void main(String[] args) throws IOException {
		new BIOServer(8818).listener();
	}






}
