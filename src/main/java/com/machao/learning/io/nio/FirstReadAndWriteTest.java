package com.machao.learning.io.nio;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FirstReadAndWriteTest {

	public static void main(String[] args) throws IOException {
		RandomAccessFile fromFile = new RandomAccessFile("/home/machao/Studio/学习/Nio/fromFile.txt", "rw");
		RandomAccessFile toFile = new RandomAccessFile("/home/machao/Studio/学习/Nio/toFile.txt", "rw");

		// 读取Channel
		FileChannel fromChannel = fromFile.getChannel();
		FileChannel toChannel = toFile.getChannel();

		// 分配缓冲区
		ByteBuffer buffer = ByteBuffer.allocate(1024);

		while (true) {
			// 清空缓冲区，切换为写模式，为下一次读做准备
			buffer.clear();
			// 从原始通道中读取数据到缓冲区
			int read = fromChannel.read(buffer);
			if (read == -1) {
				break;
			}
			// 翻转Channel，切换为写模式
			buffer.flip();
			// 将缓冲区的数据写入目标通道
			toChannel.write(buffer);
		}
		// 关闭资源
		fromFile.close();
		toFile.close();
		fromChannel.close();
		toChannel.close();

	}

}
