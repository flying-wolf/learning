package com.machao.learning.io.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ServerSocketChannelThread implements Runnable {

	private SocketChannel socketChannel;
	private String remoteName;

	public ServerSocketChannelThread(SocketChannel socketChannel) throws IOException {
		this.socketChannel = socketChannel;
		this.remoteName = socketChannel.getRemoteAddress().toString();
		System.out.println(String.format("客户：%s 连接成功！", this.remoteName));
	}

	@Override
	public void run() {
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		ByteBuffer sizeBuffer = ByteBuffer.allocate(4);
		StringBuilder sb = new StringBuilder();
		byte[] b;
		while (true) {
			try {
				sizeBuffer.clear();
				int read;
				if ((read = socketChannel.read(sizeBuffer)) != -1) {
					sb.setLength(0);
					sizeBuffer.flip();
					int size = sizeBuffer.getInt();
					int readCount = 0;
					b = new byte[size];
					while (readCount < size) {
						buffer.clear();
						if ((read = socketChannel.read(buffer)) != -1) {
							readCount += read;
							buffer.flip();
							int index = 0;
							while (buffer.hasRemaining()) {
								b[index++] = buffer.get();
								if (index >= b.length) {
									index = 0;
									sb.append(new String(b, "UTF-8"));
								}
							}
							if (index > 0) {
								sb.append(new String(b, "UTF-8"));
							}
						}
					}
					System.out.println(String.format("%s : %s", this.remoteName, sb.toString()));
				}
			} catch (Exception e) {
				System.out.println(String.format("%s 断线了，连接关闭！", this.remoteName));
				try {
					this.socketChannel.close();
				} catch (IOException ie) {
					ie.printStackTrace();
				}
				break;
			}
		}
	}

}
