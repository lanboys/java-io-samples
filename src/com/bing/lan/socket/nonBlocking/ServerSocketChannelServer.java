package com.bing.lan.socket.nonBlocking;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;

/**
 * Created by backend.
 */

public class ServerSocketChannelServer {

  public static void main(String[] args) throws IOException {
    new ServerSocketChannelServer().startServer();
  }

  private void startServer() throws IOException {
    // 创建 socket 通道
    ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
    // 设置通道为非阻塞
    serverSocketChannel.configureBlocking(true);// 默认 true 同步阻塞I/O , false 同步非阻塞I/O
    // 绑定ip端口，设置最大的请求连接数为1024
    serverSocketChannel.bind(new InetSocketAddress(8088), 10);
    System.out.println("服务启动成功.....");

    while (true) {
      System.out.println(new Date() + " 开始监听连接...");
      SocketChannel socketChannel = serverSocketChannel.accept();// 阻塞，主要体现在这
      // 阻塞不好的地方在于，只能死等着，如果不阻塞，可以去干其他事情，过一段时间再来检查一下，检查完又去干其他事情了
      System.out.println(new Date() + " 监听连接结束...");

      if (socketChannel != null) {
        socketChannel.configureBlocking(false);// 默认 true 同步阻塞I/O , false 同步非阻塞I/O
        handleSocket(socketChannel);
      }

      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public void handleSocket(SocketChannel socketChannel) {
    new Thread(() -> {
      int client = socketChannel.socket().hashCode();
      System.out.println("客户端: " + client + " 连接上了");
      String clientMsg;
      boolean flag = true;
      while (flag) {
        try {
          Thread.sleep(5000);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }

        clientMsg = SocketChannelUtil.doRead(socketChannel);
        if (clientMsg == null) {
          continue;
        }

        if (clientMsg.contains("close")) {
          flag = false;
          System.out.println("客户端: " + client + " 掉线了");
        } else if (clientMsg.contains("心跳包")) {
          System.out.println("客户端: " + client + " 发来心跳包了");
          SocketChannelUtil.doWrite(socketChannel, "收到心跳\n");
        } else {
          System.out.println("客户端: " + client + " 发来了消息：" + clientMsg);
          SocketChannelUtil.doWrite(socketChannel, "小娜: " + clientMsg + "\n");
        }
      }
    }).start();
  }
}
