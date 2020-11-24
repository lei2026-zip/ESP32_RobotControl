package com.example.SocketServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.Buffer;

public class Socket {
    private int server_port;
    private InetAddress server_IP;
    public static DatagramSocket socket = null;
    public void Socket_init(InetAddress ip,int part){
        server_IP = ip;
        server_port = part;
        // 3.创建DatagramSocket对象
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return;
    }
    public byte[] Socket_rec(){
        /*
         * 接收服务器端响应的数据
         */
        // 1.创建数据报，用于接收服务器端响应的数据
        byte[] data2 = new byte[1024];
        DatagramPacket packet2 = new DatagramPacket(data2, data2.length);
        // 2.接收服务器响应的数据
        try {
            socket.receive(packet2);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        // 3.读取数据
        String reply = new String(data2, 0, packet2.getLength());
        // 4.关闭资源
        return data2;
    }

    public void Socket_send(byte[] data){
        // 2.创建数据报，包含发送的数据信息
        DatagramPacket packet = new DatagramPacket(data, data.length,server_IP, server_port);
        // 4.向服务器端发送数据报
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
            return ;
        }
    }
}
//class UPDServer {
//    public static void main(String[] args) throws IOException {
//        /*
//         * 接收客户端发送的数据
//         */
//        // 1.创建服务器端DatagramSocket，指定端口
//        DatagramSocket socket = new DatagramSocket(12345);
//        // 2.创建数据报，用于接收客户端发送的数据
//        byte[] data = new byte[1024];// 创建字节数组，指定接收的数据包的大小
//        DatagramPacket packet = new DatagramPacket(data, data.length);
//        // 3.接收客户端发送的数据
//        System.out.println("****服务器端已经启动，等待客户端发送数据");
//        socket.receive(packet);// 此方法在接收到数据报之前会一直阻塞
//        // 4.读取数据
//        String info = new String(data, 0, packet.getLength());
//        System.out.println("我是服务器，客户端说：" + info);
//
//        /*
//         * 向客户端响应数据
//         */
//        // 1.定义客户端的地址、端口号、数据
//        InetAddress address = packet.getAddress();
//        int port = packet.getPort();
//        byte[] data2 = "欢迎您!".getBytes();
//        // 2.创建数据报，包含响应的数据信息
//        DatagramPacket packet2 = new DatagramPacket(data2, data2.length, address, port);
//        // 3.响应客户端
//        socket.send(packet2);
//        // 4.关闭资源
//        socket.close();
//    }
//}
