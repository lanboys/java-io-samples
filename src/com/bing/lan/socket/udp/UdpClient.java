package com.bing.lan.socket.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by backend.
 */

public class UdpClient {

  public static void main(String[] argv) throws IOException {
    StringBuilder builder = new StringBuilder();
    // 本机之间的 MTU 65536, ip协议是使用两个字节( 65535 byte )来存储长度
    // 本机最多 65539 - 4 = 65535(ip包最大长度) = 20 + 8 + 65507 ;  20为ip头，8为 udp头
    for (int i = 0; i < 65507; i++) {
      builder.append("a");
    }

    String msg = builder.toString();
    System.out.println("length : " + msg.length());
    byte[] buf = msg.getBytes();
    DatagramSocket socket = new DatagramSocket();
    InetAddress address = InetAddress.getByName("localhost");
    DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 8088);

    socket.send(packet);
    socket.receive(packet);
    String received = new String(packet.getData(), 0, packet.getLength());

    System.out.format("Server echo : %s\n", received);
  }
}

