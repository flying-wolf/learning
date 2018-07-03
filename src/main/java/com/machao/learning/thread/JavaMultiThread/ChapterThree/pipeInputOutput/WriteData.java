package com.machao.learning.thread.JavaMultiThread.ChapterThree.pipeInputOutput;

import java.io.IOException;
import java.io.PipedOutputStream;

public class WriteData {
	public void writeMethod(PipedOutputStream out) {
		try {
			System.out.println("write:");
			for (int i = 0; i < 300; i++) {
				String outData = String.valueOf(i + 1);
				out.write(outData.getBytes());
				System.out.print(outData);
			}
			out.close();
			System.out.println();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
