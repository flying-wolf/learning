package com.machao.learning.io.nio;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class SocketChannelClient {

	public static void main(String[] args) throws IOException {
		SocketChannel socketChannel = SocketChannel.open();
		socketChannel.connect(new InetSocketAddress(1234));
		
		while(true) {
			Scanner scnner = new Scanner(System.in);
			String next = scnner.next();
			sendMessage(socketChannel, next);
		}
		
	}

	private static void sendMessage(SocketChannel socketChannel, String msg) throws IOException {
		if(msg == null || msg.isEmpty())
			return;
		
		byte[] bytes = msg.getBytes("UTF-8");
		int size = bytes.length;
		
		ByteBuffer buffer = ByteBuffer.allocate(size);
		ByteBuffer sizeBuffer = ByteBuffer.allocate(4);
		
		sizeBuffer.putInt(size);
		buffer.put(bytes);
		
		sizeBuffer.flip();
		buffer.flip();
		
		ByteBuffer[] dest = {sizeBuffer, buffer};
		while(sizeBuffer.hasRemaining() || buffer.hasRemaining()) {
			socketChannel.write(dest);
		}
		
	}

}
