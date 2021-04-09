package com.bing.lan.socket.multiplexing;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by lb on 2020/8/4.
 * https://blog.csdn.net/Heron22/article/details/106132064
 */

public class MultiplexerTimeServer implements Runnable {

  private Selector selector;
  private ServerSocketChannel serverSocketChannel;
  private volatile boolean stop = false;
  private int count = 0;

  public synchronized void addCount() {
    count++;
    System.out.println("===================================================addCount(): " + count);
  }

  public static void main(String[] args) {
    MultiplexerTimeServer server = new MultiplexerTimeServer(8088);
    new Thread(server, "timerServer-001").start();
  }

  /**
   * @Desc 初始化多路复用器，绑定端口
   */
  public MultiplexerTimeServer(int port) {
    try {
      //在高性能的I/O设计中，有两个著名的模型：Reactor模型和Proactor模型，其中Reactor模型用于同步I/O，而Proactor模型运用于异步I/O操作。
      //创建socket 通道
      serverSocketChannel = ServerSocketChannel.open();
      //设置通道为非阻塞
      //不能设置为阻塞 抛异常
      // java.nio.channels.IllegalBlockingModeException
      // at java.base/java.nio.channels.spi.AbstractSelectableChannel.register(AbstractSelectableChannel.java:209)
      serverSocketChannel.configureBlocking(false);
      //绑定ip端口，设置最大的请求连接数为1024
      serverSocketChannel.bind(new InetSocketAddress(port), 2048);

      //创建Reactor线程多路复用器
      selector = Selector.open();
      //注册到多路复用器上，监听accept事件
      SelectionKey selectionKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
      System.out.println("*********************************************");
      System.out.println("****  time server is start on port " + port + "  ****");
      System.out.println("*********************************************");
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  @Override
  public void run() {
    while (!stop) {
      try {
        //System.out.println("\n\nrun(): " + Thread.currentThread().getName() + " " + System.currentTimeMillis() + " loop...");
        //设置获取就绪key的休眠时间,每间隔1秒唤醒一次
        selector.select(10000);
        Set<SelectionKey> selectionKeys = selector.selectedKeys();
        Iterator<SelectionKey> iterator = selectionKeys.iterator();
        SelectionKey key;
        while (iterator.hasNext()) {
          key = iterator.next();
          iterator.remove();
          try {
            handlerKey(key);
          } catch (Exception e) {
            e.printStackTrace();
            key.cancel();
            if (key.channel() != null)
              key.channel().close();
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
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

  private void handlerKey(SelectionKey key) throws IOException {
    System.out.println("----------handlerKey(): " + Thread.currentThread().getName() + key.hashCode());
    if (!key.isValid()) {
      System.out.println("----------handlerKey(): key无效");
      return;
    }
    //查看是否是accept事件
    if (key.isAcceptable()) {
      System.out.println("----------handlerKey(): accept事件");
      ServerSocketChannel channel = (ServerSocketChannel) key.channel();
      //接受客户端请求，三次握手结束，建立物理连接
      SocketChannel accept = channel.accept();
      //设置非阻塞模式
      accept.configureBlocking(false);
      //注册监听读取事件
      accept.register(selector, SelectionKey.OP_READ);
      return;
    }

    //判断是否可读状态
    if (!key.isReadable()) {
      return;
    }

    //查看是read事件
    System.out.println("----------handlerKey(): read事件");
    //开辟缓存空间
    ByteBuffer readBuffer = ByteBuffer.allocate(1024);
    SocketChannel socketChannel = (SocketChannel) key.channel();
    //读取数据
    int readBytes = socketChannel.read(readBuffer);
    //大于0，读取到了数据
    if (readBytes > 0) {
      readBuffer.flip();
      byte[] bytes = new byte[readBuffer.remaining()];
      readBuffer.get(bytes);
      String msg = new String(bytes, StandardCharsets.UTF_8);
      System.out.println("----------handlerKey(): 读取到了数据");
      System.out.println("--------------------<<<<<< receive msg: " + msg);
      doWrite(socketChannel, System.currentTimeMillis() + "");
    } else if (readBytes < 0) {
      //等于-1 ，链路已经关闭，需要释放资源
      System.out.println("----------handlerKey(): 链路已经关闭");
      key.cancel();
      socketChannel.close();
    } else {
      //等于0，没有可读取数据，忽略
      System.out.println("----------handlerKey(): 没有可读取数据");
    }
  }

  private void doWrite(SocketChannel socketChannel, String response) throws IOException {
    byte[] bytes = response.getBytes();
    ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
    byteBuffer.put(bytes);
    byteBuffer.flip();
    socketChannel.write(byteBuffer);
    System.out.println("-------------------->>>>>> response msg: " + response);
  }

  public void stop() {
    this.stop = true;
  }
}