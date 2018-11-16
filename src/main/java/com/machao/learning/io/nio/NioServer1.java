package com.machao.learning.io.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

public class NioServer1 {
	private int port = 9011;
	
	private Selector sel = null;

	public NioServer1(int port) throws IOException {
		this.port = port;
		ServerSocketChannel server = ServerSocketChannel.open();
		
		server.bind(new InetSocketAddress(port));
		server.configureBlocking(false);
		
		sel = Selector.open();
		
		server.register(sel, SelectionKey.OP_ACCEPT);
		System.out.println("服务端已启动，监听端口是：" + this.port);
	}

	public void listener() throws Exception {
		while(true) {
			int wait = sel.select();
			if(wait == 0) continue;
			
			Set<SelectionKey> keys = sel.selectedKeys();
			Iterator<SelectionKey> iterator = keys.iterator();
			while(iterator.hasNext()) {
				SelectionKey key = iterator.next();
				iterator.remove();
				this.process(key);
			}
		}
	}
	
	private void process(SelectionKey key) throws Exception {
		
	}
	
	public static void main(String[] args) throws Exception {
		new NioServer1(1234).listener();
	}

}
