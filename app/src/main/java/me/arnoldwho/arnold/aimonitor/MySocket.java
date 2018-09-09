package me.arnoldwho.arnold.aimonitor;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class MySocket {

    private OutputStream outputStream;
    String response;

    public String getResponse(String sendData, Socket socket) {
        try{
            outputStream = socket.getOutputStream();
            outputStream.write(sendData.getBytes("utf-8"));
            outputStream.flush();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            response = bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    public void sendMsg(String sendData, Socket socket) {
        try{
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(sendData.getBytes("utf-8"));
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*public Bitmap Response(Socket socket, Bitmap bitmap) {
        try {
            InputStream inputStream = socket.getInputStream();
            byte[] picLenBuff = new byte[200];
            int picLen = inputStream.read(picLenBuff);
            String picLenString = new String(picLenBuff, 0, picLen);
            int getPicLen = Integer.valueOf(picLenString);
            int offset = 0;
            byte[] bitmapBuff = new byte[getPicLen];
            while(offset < getPicLen)
            {
                int len = inputStream.read(bitmapBuff, offset, getPicLen-offset);
                offset+=len;
            }
            bitmap = BitmapFactory.decodeByteArray(bitmapBuff, 0, offset);
            return Bitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
}
