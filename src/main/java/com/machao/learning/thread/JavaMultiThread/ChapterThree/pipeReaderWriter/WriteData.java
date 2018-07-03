package com.machao.learning.thread.JavaMultiThread.ChapterThree.pipeReaderWriter;

import java.io.IOException;
import java.io.PipedWriter;

public class WriteData {
	public void writeMethod(PipedWriter out) {
		try {
			System.out.println("write:");
			for (int i = 0; i < 300; i++) {
				String outData = String.valueOf(i + 1);
				out.write(outData);
				System.out.print(outData);
			}
			out.flush();
			out.close();
			System.out.println();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
