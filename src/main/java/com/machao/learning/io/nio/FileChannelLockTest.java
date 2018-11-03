package com.machao.learning.io.nio;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class FileChannelLockTest {
	public static void main(String[] args) throws Exception {
		RandomAccessFile fromFile = new RandomAccessFile("/home/machao/Studio/学习/Nio/fromFile.txt", "rw");
		FileChannel fromChannel = fromFile.getChannel();
		
		System.out.println("trying to lock file...");
		long position = 0;
		long size = 10;
		FileLock fileLock = fromChannel.lock(position, size, false);
		
		System.out.println("after lock");

        System.out.println("pause...");
        System.out.println("isShared:"+fileLock.isShared());
        Thread.sleep(8000);
        
        System.out.println("releasing file lock");
        fileLock.release();
        System.out.println("after release.");

        fromChannel.close();
        fromFile.close();
	}
}
