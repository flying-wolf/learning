package com.machao.learning.io.nio;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class SecondReadAndWriteTest {

	public static void main(String[] args) throws IOException {
		RandomAccessFile fromFile = new RandomAccessFile("/home/machao/Studio/学习/Nio/fromFile.txt", "rw");
		RandomAccessFile toFile = new RandomAccessFile("/home/machao/Studio/学习/Nio/toFile.txt", "rw");

		FileChannel fromChannel = fromFile.getChannel();
		FileChannel toChannel = toFile.getChannel();

		ByteBuffer buffer = ByteBuffer.allocate(1024);

		while (fromChannel.read(buffer) != -1) {
			buffer.flip();
			if (buffer.hasRemaining()) {
				toChannel.write(buffer);
			}
			buffer.clear();
		}

		fromFile.close();
		toFile.close();
		fromChannel.close();
		toChannel.close();
	}

}
