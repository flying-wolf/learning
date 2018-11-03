package com.machao.learning.io.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class DataGramChannelReceiveTest {
	public static void main(String[] args) throws IOException {
		DatagramChannel datagramChannel = DatagramChannel.open();
		datagramChannel.bind(new InetSocketAddress(2223));

		ByteBuffer buffer = ByteBuffer.allocate(1024);
		byte[] b;

		while (true) {
			buffer.clear();
			SocketAddress socketAddress = datagramChannel.receive(buffer);
			if (socketAddress != null) {
				int position = buffer.position();
				b = new byte[position];
				buffer.flip();
				for (int i = 0; i < position; i++) {
					b[i] = buffer.get(i);
				}
				System.out.println(
						String.format("receive remot %s : %s", socketAddress.toString(), new String(b, "UTF-8")));
				sendReback(socketAddress, datagramChannel);
			}
		}
	}

	private static void sendReback(SocketAddress socketAddress, DatagramChannel datagramChannel) throws IOException {
		String message = "I has receive your message";
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		buffer.put(message.getBytes("UTF-8"));
		buffer.flip();
		datagramChannel.send(buffer, socketAddress);
	}
}
