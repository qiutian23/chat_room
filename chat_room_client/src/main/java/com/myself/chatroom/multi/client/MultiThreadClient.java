package com.myself.chatroom.multi.client;

import java.io.IOException;
import java.net.Socket;

public class MultiThreadClient {
    public static void main(String[] args) {
        try {
            String host = "127.0.0.1";
            int port = 6666;
            try {
                if (args.length > 0) {
                    port = Integer.parseInt(args[0]);
                }
            } catch (NumberFormatException e) {
                System.out.println("端口参数不正确，将采用默认端口：" + port);
            }
            if (args.length > 1) {
                host = args[1];
            }

            final Socket client = new Socket(host, port);
            new WriteDataToServerThread(client).start();
            new ReadDataFromServerThread(client).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
