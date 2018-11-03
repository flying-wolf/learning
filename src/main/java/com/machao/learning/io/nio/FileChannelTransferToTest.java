package com.machao.learning.io.nio;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

public class FileChannelTransferToTest {

	public static void main(String[] args) throws IOException {
		RandomAccessFile fromFile = new RandomAccessFile("/home/machao/Studio/学习/Nio/fromFile.txt", "rw");
		RandomAccessFile toFile = new RandomAccessFile("/home/machao/Studio/学习/Nio/toFile.txt", "rw");

		FileChannel fromChannel = fromFile.getChannel();
		FileChannel toChannel = toFile.getChannel();

		long position = 0;
		long count = fromChannel.size();

		fromChannel.transferTo(position, count, toChannel);

		fromChannel.close();
		toChannel.close();
		fromFile.close();
		toFile.close();
	}

}
