package com.machao.learning.thread.JavaMultiThread.ChapterThree.pipeReaderWriter;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;

public class Run {

	public static void main(String[] args) {
		try {
			WriteData write = new WriteData();
			ReadData read = new ReadData();

			PipedWriter out = new PipedWriter();
			PipedReader input = new PipedReader();

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
