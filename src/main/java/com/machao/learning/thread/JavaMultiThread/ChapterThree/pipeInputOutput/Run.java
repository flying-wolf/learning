package com.machao.learning.thread.JavaMultiThread.ChapterThree.pipeInputOutput;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class Run {

	public static void main(String[] args) {
		try {
			WriteData write = new WriteData();
			ReadData read = new ReadData();
			PipedOutputStream out = new PipedOutputStream();
			PipedInputStream input = new PipedInputStream();
			out.connect(input);
			ThreadRead readThread = new ThreadRead(read, input);
			readThread.start();

			Thread.sleep(2000);

			ThreadWrite writeThread = new ThreadWrite(write, out);
			writeThread.start();
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
