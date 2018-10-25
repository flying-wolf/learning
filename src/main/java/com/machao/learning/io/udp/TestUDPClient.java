package com.machao.learning.io.udp;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class TestUDPClient {

	public static void main(String[] args) throws IOException {
		long n = 19891210L;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		dos.writeLong(n);
		byte[] buf = baos.toByteArray();
		
		DatagramPacket dp = new DatagramPacket(buf, buf.length, new InetSocketAddress("localhost", 5678));
		
		DatagramSocket ds = new DatagramSocket(9999);
		ds.send(dp);
		ds.close();

	}

}
