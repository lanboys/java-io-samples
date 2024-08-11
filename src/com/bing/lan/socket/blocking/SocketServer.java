package com.bing.lan.socket.blocking;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class SocketServer {

  public static void main(String[] args) {
    SocketServer socketServer = new SocketServer();
    socketServer.startServer();
  }

  private void startServer() {
    try (ServerSocket serverSocket = new ServerSocket(57567)) {
      System.out.println("Server start...");
      Socket accept;
      while (true) {
        accept = serverSocket.accept();// 同步阻塞I/O
        handleSocket(accept);
      }
      // System.out.println("Server end");
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Server exit");
    }
  }

  public void handleSocket(Socket socket) {
    new Thread(() -> {
      int client = socket.hashCode();
      try {
        System.out.println("客户端: " + client + " 连接上了");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        String clientMsg;
        while ((clientMsg = bufferedReader.readLine()) != null) {// 阻塞，体现在这

          if (clientMsg.contains("心跳包")) {
            System.out.println("客户端: " + client + clientMsg);
            continue;
          }

          System.out.println("客户端: " + client + " 发来了消息：" + clientMsg);
          // bufferedWriter.write("服务器回传: " + clientMsg + "\n");
          // bufferedWriter.flush();
          if ("bye".equals(clientMsg)) {
            socket.close();
          }
        }
        // int read = 0;
        // while ((read = bufferedReader.read()) != -1) {
        //  System.out.println("客户端: " + client + " 发来了消息：" + read);
        //}
      } catch (SocketException e) {
        e.printStackTrace();
        System.out.println("客户端: " + client + " 断开连接了");
      } catch (IOException e) {
        e.printStackTrace();
      }
    }).start();
  }
}
