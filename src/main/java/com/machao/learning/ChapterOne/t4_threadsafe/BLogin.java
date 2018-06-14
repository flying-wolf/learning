package com.machao.learning.ChapterOne.t4_threadsafe;

public class BLogin extends Thread {
	@Override
	public void run() {
		LoginServlet.doPost("b", "bb");
	}
}
