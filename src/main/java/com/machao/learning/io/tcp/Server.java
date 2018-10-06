package com.machao.learning.io.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * ServerSocket测试 作为tcp网络通信的服务器端
 * 1、创建ServerSocket对象，绑定监听端口
 * 2、通过accept()方法监听客户端请求
 * 3、连接建立后，通过输入流读取客户端发送的请求信息 
 * 4、通过输出流向客户端发送响应信息
 * 5、关闭相关资源
 * */
public class Server {
	private final static int PORT = 8888;
	
	
	public static void main(String[] args) {
		try {
			// 1、创建一个服务器端Socket，即ServerSocket，绑定指定的端口，进行监听
			ServerSocket serverSocket = new ServerSocket(PORT);
			System.out.println("服务器即将启动，等待客户端连接");
			// 2、调用accept()方法 开始监听 等待客户端连接
			Socket socket = serverSocket.accept();// ctrl+shift+o快速导入需要的包
			// 3、获取输入流 并读取客户端信息
			InputStream is = socket.getInputStream();// 字节输入流
			InputStreamReader isr = new InputStreamReader(is);// 将字节流转化为字符流
			BufferedReader br = new BufferedReader(isr);// 为输入流添加缓冲
			String info = null;
			while ((info = br.readLine()) != null) {// 循环读取客户端信息
				System.out.println("我是服务器，客户端说" + info);
			}
			socket.shutdownInput();
			// 4、获取输出流 响应客户端的请求
			OutputStream os = socket.getOutputStream();
			PrintWriter pw = new PrintWriter(os);// 包装为打印流
			pw.write("欢迎您");
			pw.flush();// 缓冲输出
			// 5、关闭资源
			// 服务器调用了socket.close()和serverSocket.close()以后就不需要调用其它close()方法了 因为相当于中断了连接
			// 相应的流通道也断开了
			// 同理客户端也是调用socket.close()就可以了
			socket.close();
			serverSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
