package com.machao.learning.io.nio;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Scanner;

public class DataGramChannelSendTest {
	public static void main(String[] args) throws IOException {
		final DatagramChannel datagramChannel = DatagramChannel.open();
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				ByteBuffer buffer = ByteBuffer.allocate(1024);
				byte[] b;
				SocketAddress socketAddress = null;
				try {
					socketAddress = datagramChannel.receive(buffer);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				if(socketAddress != null) {
					int position = buffer.position();
					b = new byte[position];
					buffer.flip();
					for (int i = 0; i < position; i++) {
						b[i] = buffer.get();
					}
					try {
						System.out.println(String.format("receive remote %s : %s", socketAddress.toString(), new String(b, "UTF-8")));
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
		}).start();
		
		while(true) {
			Scanner scanner = new Scanner(System.in);
			String next = scanner.next();
			try {
				sendMessage(datagramChannel, next);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static void sendMessage(DatagramChannel datagramChannel, String msg) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		buffer.clear();
		buffer.put(msg.getBytes("UTF-8"));
		buffer.flip();
		System.out.println(String.format("send msg : %s", msg));
		datagramChannel.send(buffer, new InetSocketAddress(2223));
		
	}
}
