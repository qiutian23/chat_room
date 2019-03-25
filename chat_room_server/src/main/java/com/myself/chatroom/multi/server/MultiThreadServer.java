package com.myself.chatroom.multi.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


//服务器
public class MultiThreadServer {
    public static void main(String[] args) {
        final ExecutorService executorService = Executors.newFixedThreadPool(10);//核心线程10个
        int port = 6666;//默认端口
        try {
            if (args.length > 0) {  //命令行参数大于0，则将第一个参数作为端口值
                port = Integer.parseInt(args[0]);
            }
        } catch (NumberFormatException e) {
            System.out.println("端口参数不正确，将采用默认端口：" + port);
        }
        try {
            ServerSocket serverSocket = new ServerSocket(port);  //servereSocket表示服务器
            System.out.println("等待客户端连接...");
            while (true) {
                Socket client = serverSocket.accept();  //支持多客户端连接
                System.out.println("客户端连接端口号：" + client.getPort());
                executorService.submit(new ExecuteClient(client));  //通过提交任务到线程池来执行每个客户的业务
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
