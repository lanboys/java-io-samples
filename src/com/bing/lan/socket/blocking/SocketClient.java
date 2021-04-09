package com.bing.lan.socket.blocking;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SocketClient {

  public static void main(String[] args) {
    SocketClient socketClient = new SocketClient();
    socketClient.start();
  }

  private void start() {
    try (Socket socket = new Socket("127.0.0.1", 8088)) {
      System.out.println("client start");
      BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      new Thread(() -> {
        try {
          SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
          Date mDate = new Date();
          while (true) {
            Thread.sleep(3000);
            mDate.setTime(System.currentTimeMillis());
            bufferedWriter.write("我是客户端心跳包: " + format.format(mDate) + "\n");
            bufferedWriter.flush();
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }).start();

      new Thread(() -> {
        try {
          String clientMsg;
          BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
          while (!"bye".equals(clientMsg = reader.readLine())) {
            System.out.println("客户端发送了：" + clientMsg);
            bufferedWriter.write(clientMsg + "\n");
            bufferedWriter.flush();
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }).start();

      while (true) {
        String msg = bufferedReader.readLine();
        System.out.println("收到了服务器消息：" + msg);
      }
      //System.out.println("client end");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
