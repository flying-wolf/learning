package com.machao.learning.io.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class SocketChannelSelectorServer {
	public static void main(String[] args) throws IOException {
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		serverChannel.bind(new InetSocketAddress(1234));
		serverChannel.configureBlocking(false);

		Selector selector = Selector.open();
		SelectionKey key = serverChannel.register(selector, SelectionKey.OP_ACCEPT);

		while (true) {
			int select = selector.select();
			if (select > 0) {
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> iterator = selectedKeys.iterator();
				while (iterator.hasNext()) {
					SelectionKey selectionKey = iterator.next();
					// 接收连接请求
					if (selectionKey.isAcceptable()) {
						ServerSocketChannel channel = (ServerSocketChannel) selectionKey.channel();
						SocketChannel socketChannel = channel.accept();
						System.out.println(String.format("接受到连接请求：%s", socketChannel.getRemoteAddress().toString()));
						socketChannel.configureBlocking(false);
						// 每接收请求，注册到同一个selector中处理
						socketChannel.register(selector, SelectionKey.OP_READ);
					} else if (selectionKey.isReadable()) {
						receiveMessage(selectionKey);
					}
					iterator.remove();
				}

			}
		}

	}

	private static void receiveMessage(SelectionKey selectionKey) throws IOException {
		SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
		String remoteName = socketChannel.getRemoteAddress().toString();

		ByteBuffer sizeBuffer = ByteBuffer.allocate(4);
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		StringBuilder sb = new StringBuilder();
		byte[] b;

		sizeBuffer.clear();
		int read;
		try {
			if ((read = socketChannel.read(sizeBuffer)) != -1) {
				sb.setLength(0);
				sizeBuffer.flip();
				int readCount = 0;
				int size = sizeBuffer.getInt();
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
				System.out.println(String.format("%s : %s", remoteName, sb.toString()));
			}
		} catch (IOException e) {
			System.out.println(remoteName + " 断线了,连接关闭");
			try {
				// 取消这个通道的注册，关闭资源
				selectionKey.cancel();
				socketChannel.close();
			} catch (IOException ex) {
			}
		}
	}
}
