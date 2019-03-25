package com.myself.chatroom.multi.server;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;


//执行客户业务
public class ExecuteClient implements Runnable {
    private final Socket client; //客户
    private static final Map<String, Socket> ONLINE_USER_MAP = new ConcurrentHashMap<String, Socket>();//在线用户
    private static final Map<String, Socket> OFFLINE_USER_MAP = new ConcurrentHashMap<String, Socket>();//离线用户
    //private static final Map<String, String> USER_PASSWORD_MAP = new ConcurrentHashMap<>(); //用户信息（用户名，密码）
    private static String STORE_MESSAGE_PATH_NAME = "E:" + File.separator + "ALL-CODE" + File.separator + "JAVA_code" + File.separator + "StoreMessageOfRegister.txt";//默认将新用户注册信息保存在此文件中

    public ExecuteClient(Socket client) {
        this.client = client;
    }

    //实现run方法
    public void run() {
        try {
            InputStream clientInput = client.getInputStream();  //从客户端读取数据
            Scanner scanner = new Scanner(clientInput);
            while (true) {
                String line = scanner.nextLine();
                if (line.startsWith("register")) { //注册
                    String[] segments = line.split("\\:");//以：为分隔符
                    String userName = segments[1];  //第二个参数表示用户名
                    String password = segments[2];  //第三个参数表示密码
                    this.register(userName, password, client);
                    continue;
                }
                if (line.startsWith("login")) { //登录
                    String[] segments = line.split("\\:");
                    String userName = segments[1];  //第二个参数表示用户名
                    String password = segments[2];  //第三个参数表示密码
                    this.login(userName, password, client);
                    continue;
                }
                if (line.startsWith("private")) {  //单聊
                    String[] segments = line.split("\\:");
                    String userName = segments[1];  //第二个参数表示被发送者的名称
                    String message = segments[2];   //第三个参数表示要发送的信息
                    this.privateChat(userName, message);
                    continue;
                }
                if (line.startsWith("group")) {  //群聊
                    String message = line.split("\\:")[1]; //要发送的信息
                    this.groupChat(message);
                    continue;
                }
                if (line.startsWith("bye")) {  //退出
                    this.quit();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //注册
    private void register(String userName, String password, Socket client) {
        if (userName == null) {
            this.sendMessage(client, "用户名不能为空");
            return;
        }
        if (password == null) {
            this.sendMessage(client, "密码不能为空");
            return;
        }

        char[] name = userName.toCharArray();  //字符串转数组
        char[] passWord = password.toCharArray();  //字符串转数组
        int nLength = name.length;  //用户名的长度
        int pLength = passWord.length;  //密码的长度
        if (!((nLength >= 8 && nLength <= 15) && (pLength >= 6 && pLength <= 10))) {
            this.sendMessage(client, "输入的用户名或密码长度不符合要求（用户名长度8到15，密码长度6到10）");
            return;
        }


        for (char n : name) {
            if (!((n >= 'A' && n <= 'Z') || (n >= 'a' && n <= 'z') || (n >= '0' && n <= '9'))) {
                this.sendMessage(client, "用户名仅支持字母、数字");
                return;
            }
        }
        for (char p : passWord) {
            if (!((p >= 'A' && p <= 'Z') || (p >= 'a' && p <= 'z') || (p >= '0' && p <= '9'))) {
                this.sendMessage(client, "密码仅支持字母、数字");
                return;
            }
        }

        File file = new File("E:" + File.separator + "ALL-CODE" + File.separator + "JAVA_code" + File.separator + "StoreMessageOfRegister.txt");//用户注册信息保存文件
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String s = null;
            String in = userName + ":" + password;
            while ((s = reader.readLine()) != null) {
                String[] tmp = s.split("\\:");
                if (s.equals(in)) {
                    this.sendMessage(client, "用户" + userName + "已注册，可直接登录");
                    return;
                }
                if ((userName.equals(tmp[0]))) {
                    this.sendMessage(client, "用户名" + userName + "已被占用");
                    return;
                }
            }
            OutputStream out = new FileOutputStream(file, true);//向文件中追加信息
            out.write((userName + ":").getBytes());
            out.write((password + "\n").getBytes());//userName:password
            out.close();
            this.sendMessage(this.client, "用户" + userName + "注册成功");  //通知客户端注册成功
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //USER_PASSWORD_MAP.put(userName, password);  //保存新注册的用户信息
    }


    //登录
    private void login(String userName, String password, Socket client) {
        for (Map.Entry<String, Socket> entry : ONLINE_USER_MAP.entrySet()) {
            if (userName.equals(entry.getKey())) {
                this.sendMessage(client, "用户" + userName + "已在线，不可重复登录");
                return;
            }
        }
        try {
            InputStream input = new FileInputStream(STORE_MESSAGE_PATH_NAME);
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String in = userName + ":" + password;//用户输入的信息
            String r = null;
            while ((r = reader.readLine()) != null) {
                if (r.equals(in)) {
                    System.out.println("用户" + userName + "加入聊天室");
                    ONLINE_USER_MAP.put(userName, client);  //将登录成功的用户存入 在线用户
                    printOnlineUser(); //打印在线用户
                    this.sendMessage(client, "用户" + userName + "登录成功");  //通知客户端登录成功
                    return;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.sendMessage(client, "用户名或密码输入不正确");
        return;
    }



    //单聊
    private void privateChat(String userName, String message) {
        String currentUserName = this.getCurrentUserName();
        Socket target = ONLINE_USER_MAP.get(userName);
        if (currentUserName.equals(userName)) {  //不能自己给自己发消息
            return;
        }
        if (target != null) {
            this.sendMessage(target, currentUserName + "对你说：" + message);
        } else {
            this.sendMessage(this.client, "用户" + userName + "已下线，此条消息未发出");
        }
    }

    //群聊
    private void groupChat(String message) {
        String currentUserName = this.getCurrentUserName();
        for (Socket socket : ONLINE_USER_MAP.values()) {
            if (socket.equals(this.client)) {
                continue;
            }
            this.sendMessage(socket, currentUserName + "说：" + message);
        }
    }

    //退出
    private void quit() {
        String currentUserName = this.getCurrentUserName();
        System.out.println("用户" + currentUserName + "下线");
        Socket socket = ONLINE_USER_MAP.get(currentUserName);
        this.sendMessage(socket, "bye");

        ONLINE_USER_MAP.remove(currentUserName);
        printOnlineUser();
        OFFLINE_USER_MAP.put(currentUserName, socket);
        printOfflineUser();

    }


    //给客户端发送信息
    private void sendMessage(Socket client, String message) {
        try {
            OutputStream clientOutput = client.getOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(clientOutput);
            writer.write(message + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //获取当前用户的用户名
    private String getCurrentUserName() {
        String currentUserName = null;
        for (Map.Entry<String, Socket> entry : ONLINE_USER_MAP.entrySet()) {
            if (entry.getValue().equals(this.client)) {
                currentUserName = entry.getKey();
                break;
            }
        }
        return currentUserName;
    }

    //打印在线用户
    private void printOnlineUser() {
        System.out.println("在线用户人数：" + ONLINE_USER_MAP.size());
        System.out.print("用户列表：");
        for (Map.Entry<String, Socket> entry : ONLINE_USER_MAP.entrySet()) {
            System.out.print(entry.getKey() + " ");
        }
        System.out.println("\n----------------------");
    }

    //打印离线用户
    private void printOfflineUser() {
        System.out.println("离线用户人数：" + OFFLINE_USER_MAP.size());
        System.out.print("离线用户：");
        for (Map.Entry<String, Socket> entry : OFFLINE_USER_MAP.entrySet()) {
            System.out.print(entry.getKey() + " ");
        }
        System.out.println("");
    }
}
