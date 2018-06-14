package com.machao.learning.thread.JavaMultiThread.ChapterOne.t4_threadsafe;

public class BLogin extends Thread {
	@Override
	public void run() {
		LoginServlet.doPost("b", "bb");
	}
}
