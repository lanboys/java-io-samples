package com.bing.lan.socket.blocking;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class SocketClient {

  public static void main(String[] args) {
    SocketClient socketClient = new SocketClient();
    socketClient.start();
  }

  boolean closeFlag = false;

  private void start() {
    try (Socket socket = new Socket("127.0.0.1", 57567)) {
      System.out.println("client start");
      BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      // new Thread(() -> {
      //  try {
      //    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      //    Date mDate = new Date();
      //    while (!closeFlag) {
      //      Thread.sleep(1000000);
      //      mDate.setTime(System.currentTimeMillis());
      //      bufferedWriter.write("我是客户端心跳包: " + format.format(mDate) + "\n");
      //      bufferedWriter.flush();
      //    }
      //  } catch (Exception e) {
      //    e.printStackTrace();
      //  }
      //}).start();

      new Thread(() -> {
        try {
          String clientMsg;
          System.out.println("请输入传输个数：");
          BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

          while (!"bye".equals(clientMsg = reader.readLine())) {
            int count = 0;
            try {
              count = Integer.valueOf(clientMsg);
            } catch (NumberFormatException e) {
              e.printStackTrace();
            }

            StringBuilder sb = new StringBuilder();
            int byteCount = 0;

            for (int i = 0; i < count; i++) {

              // 发送 0-1-2-3-4-5-6-7-8-9- 字符
              sb.append(i).append("-");

              // 统计字符数量，包含结尾的"\n"
              if (i <= 9) {
                byteCount += 2;
              } else if (i <= 99) {
                byteCount += 3;
              } else if (i <= 999) {
                byteCount += 4;
              } else if (i <= 9999) {
                byteCount += 5;
              }
            }
            clientMsg = sb.append(byteCount + String.valueOf(byteCount).length() + 1).toString();

            // 0-1-2-8\n
            // 最后的数据表示一共几个字节，临界的时候可能不准确，比如输入36时，应该102字节（\n），不过这里只是简单测试，无所谓了。
            // 36
            // 客户端发送了：0-1-2-3-4-5-6-7-8-9-10-11-12-13-14-15-16-17-18-19-20-21-22-23-24-25-26-27-28-29-30-31-32-33-34-35-101
            System.out.println("客户端发送了：" + clientMsg);
            bufferedWriter.write(clientMsg + "\n");
            bufferedWriter.flush();
          }

          if ("bye".equals(clientMsg)) {
            closeFlag = true;
            bufferedWriter.write(clientMsg + "\n");
            bufferedWriter.flush();
            socket.close();
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }).start();

      while (!closeFlag) {
        // String msg = bufferedReader.readLine();
        // System.out.println("收到了服务器消息：" + msg);
        Thread.sleep(10000);
      }
      // System.out.println("client end");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
