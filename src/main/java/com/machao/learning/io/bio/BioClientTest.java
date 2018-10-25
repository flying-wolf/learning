package com.machao.learning.io.bio;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class BioClientTest {
	public static void main(String[] args) throws Exception {
		Socket socket = new Socket("127.0.0.1", 8888);
		
		OutputStream outputStream = socket.getOutputStream();
		PrintWriter writer = new PrintWriter(outputStream);
		writer.write("mmp");
		writer.flush();
		socket.shutdownOutput();
		
		System.out.println("BioClientTest -> writer.flush() 发送数据成功！");
		
		InputStream inputStream = socket.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		String lineStr = reader.readLine();
		socket.shutdownInput();
		
		System.out.println("BioClientTest -> reader.readLine() 服务器返回数据：" + lineStr);
	}
}
