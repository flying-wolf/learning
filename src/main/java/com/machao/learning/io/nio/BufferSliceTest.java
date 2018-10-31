package com.machao.learning.io.nio;

import java.nio.ByteBuffer;

/**
 * 缓冲区分片
 * <p>
 * 缓冲区可以使用slice方法创建一个子缓冲区，即是创建一个新的缓冲区，但是共享原缓冲区的一部分数据.
 * </p>
 * 
 * @author chao.ma
 *
 */
public class BufferSliceTest {

	public static void main(String[] args) {
		// 创建一个容量10的字节缓冲区
		ByteBuffer buffer = ByteBuffer.allocate(10);

		// 设置缓冲区数据
		for (int i = 0; i < buffer.capacity(); i++) {
			buffer.put(i, (byte) i);
		}

		// 手动设置状态变量，创建一个包含原缓冲区index3-6的子缓冲区（分片）
		buffer.position(3);
		buffer.limit(7);
		ByteBuffer slice = buffer.slice();

		// 对新创建的子缓冲区中每个数据乘10操作
		for (int i = 0; i < slice.capacity(); i++) {
			byte b = slice.get(i);
			b *= 10;
			slice.put(i, b);
		}

		// 重设状态变量
		buffer.position(0);
		buffer.limit(buffer.capacity());

		// 输出缓冲区中的字节
		while (buffer.hasRemaining()) {
			System.out.println(buffer.get());
		}
	}

}
