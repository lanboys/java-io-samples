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
    for (int i = 0; i < 65507; i++) {//加上协议头32，最多 65539=65507+32
      builder.append("a");
    }
    byte[] buf = builder.toString().getBytes();
    DatagramSocket socket = new DatagramSocket();
    InetAddress address = InetAddress.getByName("localhost");
    DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 8088);

    socket.send(packet);
    socket.receive(packet);
    String received = new String(packet.getData(), 0, packet.getLength());

    System.out.format("Server echo : %s\n", received);
  }
}

