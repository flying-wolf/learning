package com.machao.learning.io.bio.demo;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.UUID;

public class BIOClient2 {

	public static void main(String[] args) {
		try {
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(8818));
			
			String msg = UUID.randomUUID().toString();
			
			OutputStream os = socket.getOutputStream();
			
			os.write(msg.getBytes());
			os.close();
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
