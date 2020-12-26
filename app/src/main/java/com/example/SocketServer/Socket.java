package com.example.SocketServer;

import android.os.Message;

import com.example.ImgDispose.ImgDispose;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Socket {
    private InetAddress ip;
    private int port;
    private DatagramPacket sendpacket;  //发送包
    public DatagramPacket receivepacket;  //接收包
    public DatagramSocket  socket=null;
    // 协议
    private byte[] UdpBuffer = new byte[1050];
    private int PackageSize=1024;  //数据包大小
    private int PackageLen=0;
    private int Packageid=0;   //数据包序号
    private int Packageidnow=0;
    private int PackageCount = 0; //数据包数量
    private int frameSize=0;   //图像大小
    private int frameSizenow = 0;
    private int frameSizeOk=0;
    private int frameid;    //帧id
    private int frameidnow=0;
    //
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
        return buff;
    }

    public boolean UDPgetimage(ByteArrayOutputStream os,Handler Msg, int packagesize) throws IOException {
        try {
            UdpBuffer =RecData(packagesize);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(receivepacket.getLength()==0){
            System.out.println("PackageLenth:0");
            return false;
        }
        System.out.println("PackageLenth:"+receivepacket.getLength());
        frameid =  (int)(((UdpBuffer[1]&0xff) << 24) + (UdpBuffer[2]&0xff) << 16) + ((UdpBuffer[3]&0xff) << 8) + (UdpBuffer[4]&0xff); //获取帧号
        frameSize = (int)(((UdpBuffer[5]&0xff) << 24) + ((UdpBuffer[6]&0xff) << 16) + ((UdpBuffer[7]&0xff) << 8) + (UdpBuffer[8]&0xff)); //获取帧大小
        Packageid = (int)UdpBuffer[10]&0xff; //获取包号
        PackageSize = (int)((UdpBuffer[13]&0xff)<< 8)|(UdpBuffer[14]&0xff);//获取包体积
        if (frameidnow != frameid) { //换帧，记录新一帧的数据信息
            frameidnow = frameid;    //更新帧号
            frameSizenow = frameSize;  //更新帧体积
            PackageCount = UdpBuffer[9]&0xff;   //更新数据包数量
            PackageLen = ((UdpBuffer[11]&0xff) << 8)|(UdpBuffer[12]&0xff);  //更新数据包长度
            System.out.println("framesize:"+frameSize+";frameID:"+frameid+";PackageCount:"+PackageCount);
            //TODO 2020-11-30 10:02:57
            frameSizeOk = 0 ;  //清除当前帧已接收数据量
            Packageidnow = 0;    //最新已接收数据包号清零
            os.reset();
        }
        if((Packageid<=PackageCount)&&(Packageid>Packageidnow)){
            if(PackageSize<=(UdpBuffer.length-15)){
                if(PackageSize==PackageLen||Packageid==PackageCount){
                    Packageidnow = Packageid;
                    try {
                        os.write(subBytes(UdpBuffer,15,PackageSize));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    frameSizeOk+=PackageSize;
                }
            }
            if(frameSizeOk==frameSizenow){
                byte[] byteArray = os.toByteArray();  //读取图像帧
                System.out.println("picture:"+byteArray.length);
                ImgDispose img = new ImgDispose();
                Message msg =new Message();
                msg.obj = img.getPicFromBytes(byteArray,null);//可以是基本类型，可以是对象，可以是List、map等
                msg.what = 1;
                Msg.sendMessage(msg);
                return true;
            }
        }
        return false;
    }
    public static byte[] subBytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count];
        System.arraycopy(src, begin, bs, 0, count);
        return bs;
    }

}