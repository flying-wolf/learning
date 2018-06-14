package com.machao.learning.thread.JavaMultiThread.ChapterOne.t4_threadsafe;

public class ALogin extends Thread {
	@Override
	public void run() {
		LoginServlet.doPost("a", "aa");
	}
}
