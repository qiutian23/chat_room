package com.myself.chatroom.multi.client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;


//往服务器写入数据
public class WriteDataToServerThread extends Thread {
    private final Socket client;

    public WriteDataToServerThread(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
            OutputStream clientOutput = client.getOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(clientOutput);
            Scanner scanner = new Scanner(System.in);
            show();
            while (true) {
                Thread.sleep(1000);
                System.out.println("请输入消息：");
                String message = scanner.nextLine();
                writer.write(message + "\n");
                writer.flush();
                if (message.equals("bye")) {
                    client.close();  //客户端关闭退出
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void show() {
        System.out.println("输入register:<userName>:<password>时，可实现用户注册");
        System.out.println("输入login:<userName>:<password>时，可实现用户登录");
        System.out.println("输入private:<userName>:<message>时，可实现单聊");
        System.out.println("输入group:<message>时，可实现群聊");
        System.out.println("输入bye时，表示用户退出");
    }

}
