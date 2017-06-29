/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.vsm.xtension.stickmantts;

import java.io.IOException;
import java.net.*;

/**
 *
 * @author Robbie
 */

public class UdpClient {

    public static void setCommandValue(String s) throws SocketException, UnknownHostException, IOException {

        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName("localhost");
        byte[] sendData = new byte[1024];
        byte[] receiveData = new byte[1024];
        sendData = s.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9777);
        clientSocket.send(sendPacket);

        System.out.println(s + ":  IS SENDED");
        clientSocket.close();
    }
}
