package com.bing.lan.socket.nonBlocking;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by backend.
 */

public class ServerSocketChannelServer {

  public static void main(String[] args) throws IOException {
    new ServerSocketChannelServer().startServer();
  }

  private void startServer() throws IOException {
    //创建socket 通道
    ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
    //设置通道为非阻塞
    serverSocketChannel.configureBlocking(false);// true 同步阻塞I/O , false 同步非阻塞I/O
    //绑定ip端口，设置最大的请求连接数为1024
    serverSocketChannel.bind(new InetSocketAddress(8088), 10);
    System.out.println("服务启动成功.....");
    while (true) {
      SocketChannel socketChannel = serverSocketChannel.accept();
      if (socketChannel != null) {
        socketChannel.configureBlocking(false);
        handleSocket(socketChannel);
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
        clientMsg = SocketChannelUtil.doRead(socketChannel);
        if (clientMsg == null) {
          continue;
        }
        if (clientMsg.contains("close")) {
          flag = false;
          System.out.println("客户端: " + client + " 掉线了");
        } else if (clientMsg.contains("心跳包")) {
          System.out.println("客户端: " + client + " 心跳包");
        } else {
          System.out.println("客户端: " + client + " 发来了消息：" + clientMsg);
          SocketChannelUtil.doWrite(socketChannel, "小娜: " + clientMsg + "\n");
        }
      }
    }).start();
  }
}
