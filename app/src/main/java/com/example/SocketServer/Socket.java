package com.example.SocketServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Socket {
    private InetAddress ip;
    private int port;
    private DatagramPacket sendpacket;  //发送包
    public DatagramPacket receivepacket;  //接收包
    public DatagramSocket  socket=null;
    //IP port 目标ip port; timeout 最大响应等待时间,超时发生中断,用于receive UDP接收
    public void SockInit(InetAddress IP,int Port,int timeout) throws SocketException {
        ip = IP;
        port = Port;
        socket = new DatagramSocket(8080);
        socket.setSoTimeout(timeout);
    }
    public void SendData(byte[] buff) throws IOException {
        sendpacket = new DatagramPacket(buff,buff.length,ip,port);
        socket.send(sendpacket);
    }

    public byte[] RecData(int datasize) throws IOException {
        byte[] buff  = new byte[datasize];
        receivepacket = new DatagramPacket(buff,datasize);
        socket.receive(receivepacket);
        System.out.println("BufferSize:"+receivepacket.getLength());
        return buff;
    }
    public byte[] UDPgetimage(int packagesize) throws IOException {
        byte[] UdpBuffer;
        int PackageSize;  //数据包大小
        int PackageLen=0;
        int Packageid=0;   //数据包序号
        int Packageidnow=0;
        int PackageCount = 0; //数据包数量
        int frameSize=0;   //图像大小
        int frameSizenow = 0;
        int frameSizeOk=0;
        int frameid;    //帧id
        int frameidnow=0;
        byte[] FrameBuffer=null;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        do {
            UdpBuffer = RecData(packagesize);
            frameid =  (UdpBuffer[1] << 24) + (UdpBuffer[2] << 16) + (UdpBuffer[3] << 8) + UdpBuffer[4]; //获取帧号
            frameSize = (UdpBuffer[5] << 24) + (UdpBuffer[6] << 16) + (UdpBuffer[7] << 8) + UdpBuffer[8]; //获取帧大小
            Packageid = UdpBuffer[10]; //获取包号
            PackageSize = (UdpBuffer[13] << 8) + UdpBuffer[14]; //获取包体积
            if (frameidnow != frameid) { //换帧，记录新一帧的数据信息
                frameidnow = frameid;    //更新帧号
                frameSizenow = frameSize;  //更新帧体积
                PackageCount = UdpBuffer[9];   //更新数据包数量
                PackageLen = (UdpBuffer[11] << 8) + UdpBuffer[12];  //更新数据包长度
                frameSizeOk = 0 ;  //清除当前帧已接收数据量
                Packageidnow = 0;    //最新已接收数据包号清零
                FrameBuffer = null;  //清空图片数据缓存
            }
            if((Packageid<=PackageCount)&&(Packageid>Packageidnow)){
                 if(PackageSize==(UdpBuffer.length-15)){
                     if(PackageSize==PackageLen||Packageid==PackageCount){
                         Packageidnow = Packageid;
                         os.write(UdpBuffer);
                         frameSizeOk+=PackageSize;
                     }
                 }
            }
            if(frameSizeOk==frameSizenow){

            }
        }while (true);
    }
    public static byte[] subBytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count];
        System.arraycopy(src, begin, bs, 0, count);
        return bs;
    }

}