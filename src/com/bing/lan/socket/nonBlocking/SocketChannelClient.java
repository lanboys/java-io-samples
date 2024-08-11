package com.bing.lan.socket.nonBlocking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by backend.
 */

public class SocketChannelClient {

  public static void main(String[] args) throws IOException {
    new SocketChannelClient().start();
  }

  volatile boolean flag = true;

  public void start() throws IOException {
    // 创建 socket 通道
    SocketChannel socketChannel = SocketChannel.open();
    // 设置通道为非阻塞
    socketChannel.configureBlocking(true);// 默认 true 同步阻塞I/O , false 同步非阻塞I/O
    socketChannel.connect(new InetSocketAddress(8088));

    while (!socketChannel.finishConnect()) {
      System.out.println("waiting connect finish .....");
      try {
        Thread.sleep(1);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    System.out.println("客户端连接成功...");

    new Thread(() -> {
      try {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date mDate = new Date();
        while (flag) {
          Thread.sleep(20000);
          mDate.setTime(System.currentTimeMillis());
          SocketChannelUtil.doWrite(socketChannel, "我是客户端心跳包: " + format.format(mDate) + "\n");
          System.out.println("客户端发送了心跳包...");
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }).start();

    new Thread(() -> {
      try {
        String clientMsg;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (flag && !"bye".equals(clientMsg = reader.readLine())) {
          System.out.println("客户端发送了：" + clientMsg);
          SocketChannelUtil.doWrite(socketChannel, clientMsg + "\n");
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }).start();

    while (flag) {

      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }

      String serverMsg = SocketChannelUtil.doRead(socketChannel);
      if (serverMsg == null) {
        continue;
      }
      if (serverMsg.contains("close")) {
        flag = false;
        System.out.println("连接不上服务端...");
      } else {
        System.out.println("收到了服务器消息：" + serverMsg);
      }
    }
  }
}