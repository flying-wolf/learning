package com.machao.learning.thread.JavaMultiThread.ChapterThree.pipeInputOutput;

import java.io.IOException;
import java.io.PipedInputStream;

public class ReadData {
	public void readMethod(PipedInputStream input) {
		try {
			System.out.println("read:");
			byte[] byteArray = new byte[20];
			int readLength = input.read(byteArray);
			while (readLength != -1) {
				String newData = new String(byteArray, 0, readLength);
				System.out.print(newData);
				readLength = input.read(byteArray);
			}
			input.close();
			System.out.println();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
