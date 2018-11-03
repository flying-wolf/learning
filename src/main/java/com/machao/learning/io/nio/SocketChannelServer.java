package com.machao.learning.io.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.machao.learning.concurrent.Executor.ThreadPoolExecutor;

public class SocketChannelServer {

	public static void main(String[] args) throws IOException {
		ThreadPoolExecutor executor = new ThreadPoolExecutor(3, 10, 1000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(100));
		
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.bind(new InetSocketAddress(1234));
		
		while(true) {
			SocketChannel socketChannel = serverSocketChannel.accept();
			if(socketChannel != null) {
				executor.submit(new ServerSocketChannelThread(socketChannel));
			}
		}

	}

}
