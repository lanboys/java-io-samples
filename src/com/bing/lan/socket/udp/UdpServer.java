package com.bing.lan.socket.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by backend.
 */

public class UdpServer {

  public static void main(String[] argv) throws IOException {
    DatagramSocket socket = new DatagramSocket(8088);
    byte[] buf = new byte[5];//如果包字节数大于数组大小，多余的数据将丢失
    while (true) {
      DatagramPacket packet = new DatagramPacket(buf, buf.length);
      System.out.println("try receive...");
      socket.receive(packet);

      InetAddress address = packet.getAddress();
      int port = packet.getPort();
      packet = new DatagramPacket(buf, buf.length, address, port);

      String received = new String(packet.getData(), 0, packet.getLength());
      System.out.println(String.format("received [%s:%s]: %s ", address, port, received));

      socket.send(packet);
    }
  }
}