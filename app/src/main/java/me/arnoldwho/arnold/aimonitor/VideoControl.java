package me.arnoldwho.arnold.aimonitor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class VideoControl {
    MySocket mySocket = new MySocket();
    String response;
    Bitmap bitmap;
    private static final int UPDATE_VIDEO = 0; //更新标题的标志


    public boolean showVideo(final ImageView imageView, final Socket socket) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                response = mySocket.getResponse("/getvideo", socket);
                if (response.equals("/ok")) {
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
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        return true;
    }

   /* private Handler videoHandler= new Handler()
    {
        public void dispatchMessage(android.os.Message msg) {
            if(msg.what == UPDATE_VIDEO){
                String title = msg.getData().getString("Result");
                titleView.setText(title);
            }else{
                //其他消息
            }
        };
    };*/
}
