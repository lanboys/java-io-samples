package com.bing.lan.socket.nonBlocking;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * IO是Input/Output的缩写。Unix网络编程中有五种IO模型：
 * <p>
 * blocking IO（阻塞IO）
 * <p>
 * nonblocking IO（非阻塞IO）
 * <p>
 * IO multiplexing（多路复用IO）
 * <p>
 * signal driven IO（信号驱动IO）
 * <p>
 * asynchronous IO（异步IO）
 */

public class SocketChannelUtil {

  public static void doWrite(SocketChannel socketChannel, String response) {
    try {
      byte[] bytes = response.getBytes();
      ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
      byteBuffer.put(bytes);
      byteBuffer.flip();
      socketChannel.write(byteBuffer);
      System.out.println(new Date() + " 写数据完成...");
    } catch (Exception e) {
      System.out.println("写失败：" + e.getLocalizedMessage());
    }
  }

  public static String doRead(SocketChannel socketChannel) {
    try {
      ByteBuffer readBuffer = ByteBuffer.allocate(1024);
      // 读取数据
      System.out.println(new Date() + " 即将读取数据...");
      int readBytes = socketChannel.read(readBuffer);// 是否阻塞，主要体现在这里
      System.out.println(new Date() + " 读取数据完成...");

      // 大于0，读取到了数据
      if (readBytes > 0) {
        readBuffer.flip();
        byte[] bytes = new byte[readBuffer.remaining()];
        readBuffer.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
      }
    } catch (Exception e) {
      System.out.println("读失败：" + e.getLocalizedMessage());
      return "close";
    }
    return null;
  }
}
