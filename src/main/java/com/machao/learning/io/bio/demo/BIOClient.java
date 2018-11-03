package com.machao.learning.io.bio.demo;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class BIOClient {

	public static void main(String[] args) {
		int count = 100;
		
		CountDownLatch latch = new CountDownLatch(count);
		
		for (int i = 0; i < count; i++) {
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						Socket socket = new Socket();
						socket.connect(new InetSocketAddress(8818));
						
						OutputStream os = socket.getOutputStream();
						
						os.write(UUID.randomUUID().toString().getBytes());
						os.close();
						socket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						try {
							latch.await();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				}
			}).start();
			latch.countDown();
		}
		System.out.println("ok!");
	}

}
