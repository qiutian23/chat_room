package com.myself.chatroom.multi.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Scanner;


//从服务器读取数据
public class ReadDataFromServerThread extends Thread {
    private final Socket client;

    public ReadDataFromServerThread(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
            InputStream clientInput = client.getInputStream();
            Scanner scanner = new Scanner(clientInput);
            while (true) {
                String message = scanner.nextLine();
                System.out.println(message);
                if (message.equals("bye")) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
