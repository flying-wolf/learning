package com.machao.learning.io.bio;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class BioServerTest {
	
	private static boolean isRun = true;

	public static boolean isRun() {
		return isRun;
	}

	public static void setRun(boolean isRun) {
		BioServerTest.isRun = isRun;
	}
	
	public static void main(String[] args) throws Exception {
		ServerSocket serverSocket = new ServerSocket();
		
		serverSocket.bind(new InetSocketAddress("127.0.0.1", 8888));
		
		while(isRun()) {
			Socket socket = serverSocket.accept();
			System.out.println("BioServerTest -> serverSocket.accept()");
			new Thread(new BioServerTask(socket)).start();
		}
	}

}
