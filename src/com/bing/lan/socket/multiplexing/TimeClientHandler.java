package com.bing.lan.socket.multiplexing;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by lb on 2020/8/4.
 */
public class TimeClientHandler {

  private Selector selector;
  private volatile boolean stop = false;
  private int count = 0;

  public synchronized void addCount() {
    count++;
    System.out.println("addCount(): " + count);
  }

  public static void main(String[] args) {
    TimeClientHandler timeClientHandler = new TimeClientHandler();
    Selector selector = timeClientHandler.getSelector();

    for (int i = 0; i < 1; i++) {
      // new Thread(new Runnable() {
      //    @Override
      //    public void run() {
      try {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress("127.0.0.1", 8088));
        // 注册到多路复用器上，监听连接事件
        socketChannel.register(selector, SelectionKey.OP_CONNECT);
      } catch (Exception e) {
        e.printStackTrace();
        System.exit(1);
      }
      //    }
      //}).start();
    }
    timeClientHandler.start();
  }

  public TimeClientHandler() {
    try {
      selector = Selector.open();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public Selector getSelector() {
    return selector;
  }

  public void start() {
    while (!stop) {
      try {
        selector.select();
        Set<SelectionKey> selectionKeys = selector.selectedKeys();
        Iterator<SelectionKey> iterator = selectionKeys.iterator();

        while (iterator.hasNext()) {
          SelectionKey key = iterator.next();
          iterator.remove();
          try {
            handInput(key);
          } catch (Exception e) {
            e.printStackTrace();
            key.cancel();
            if (key.channel() != null) {
              key.channel().close();
            }
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
        System.exit(-1);
      }
    }

    if (selector != null) {
      try {
        selector.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void handInput(SelectionKey key) throws IOException {
    if (!key.isValid()) {
      return;
    }
    SocketChannel channel = (SocketChannel) key.channel();

    // 判断是否连接成功
    if (key.isConnectable()) {

      // 完成了连接
      if (channel.finishConnect()) {
        channel.register(selector, SelectionKey.OP_READ);
        addCount();
        doWrite(channel);
      } else {
        System.exit(-1);
      }
    }

    // 判断是否可读状态
    if (!key.isReadable()) {
      return;
    }

    ByteBuffer readBuffer = ByteBuffer.allocate(1024);
    int readBytes = channel.read(readBuffer);
    if (readBytes > 0) {
      readBuffer.flip();
      byte[] bytes = new byte[readBuffer.remaining()];
      readBuffer.get(bytes);
      String msg = new String(bytes, StandardCharsets.UTF_8);
      System.out.println("<<<<<< client receive msg: " + msg);
      channel.close();
    } else if (readBytes < 0) {
      key.cancel();
      channel.close();
    }
  }

  private void stop() {
    this.stop = true;
  }

  private void doWrite(SocketChannel socketChannel) throws IOException {
    byte[] bytes = "query for time".getBytes();
    ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
    writeBuffer.put(bytes);
    writeBuffer.flip();
    socketChannel.write(writeBuffer);
    if (!writeBuffer.hasRemaining()) {
      System.out.println(">>>>>> client send msg success");
    }
  }
}