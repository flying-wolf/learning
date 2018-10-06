package com.machao.learning.io.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Socket测试  作为tcp的客户端：
 * 1、创建Socket对象，指明需要连接的服务器的地址和端口号
 * 2、连接建立以后，向服务器端发送请求信息
 * 3、通过输入流获取服务器响应的信息
 * 4、关闭相关资源
 * */
public class Client {
	private final static String ADDRESS = "localhost";
	private final static int PORT = 8888;
	
	public static void main(String[] args) {
		try {
			// 1、创建客户端Socket，指定服务器地址和端口
			Socket socket = new Socket(ADDRESS, PORT);
			// 2、获取输出流，向服务器发送信息
			OutputStream os = socket.getOutputStream();// 字节输出流
			PrintWriter pw = new PrintWriter(os);// 将输出流包装为打印流
			pw.write("我是客户端 我在说话");
			pw.flush();
			socket.shutdownOutput();// 关闭输出流
			// 3、获取输入流，并且读取服务器端的响应信息
			InputStream is = socket.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String info = null;
			while ((info = br.readLine()) != null) {// 循环读取服务器信息
				System.out.println("我是客户端，服务器说" + info);
			}
			// 4、关闭资源
//            socket.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
