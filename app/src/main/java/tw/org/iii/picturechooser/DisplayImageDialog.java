package tw.org.iii.picturechooser;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rick.wu on 2017/1/9.
 */

public class DisplayImageDialog extends Dialog{
    private Toolbar toolbar;
    private ImageView displayImg;
    private Bitmap bitmap;
    private int grey;
    private Info info;
    private UIHandler uiHandler;
    private ProgressDialog progressDialog;
    private Button grayBtn;
    private Context mContext;
    private List<Info> list;
    private Button gCodeBtn;

    public DisplayImageDialog(Context context, Bitmap bmp) {
        super(context, R.style.display_dialog);
        mContext = context;
        bitmap = bmp;
        initData();
        initView();
    }

    private void initData() {
        uiHandler = new UIHandler();
        list = new ArrayList<>();
    }

    private void initView() {
        setContentView(R.layout.display_dialog);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("返回");
        toolbar.setTitleTextColor(Color.parseColor("#ffffff"));
        toolbar.setNavigationIcon(R.drawable.navi_icon_back2);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        displayImg = (ImageView) findViewById(R.id.img);
        if(bitmap != null) {
            displayImg.setImageBitmap(bitmap);
        }

        grayBtn = (Button) findViewById(R.id.gray_btn);
        grayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("brad","灰階onClick");
                progressDialog = ProgressDialog.show(mContext, "轉換中", "請等待...",true);
                MyThread mt1 = new MyThread();
                mt1.start();
                gCodeBtn.setEnabled(true);
                grayBtn.setEnabled(false);
            }
        });

        gCodeBtn = (Button) findViewById(R.id.gcode_btn);
        gCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GcodeThread gt1 = new GcodeThread();
                gt1.start();
                gCodeBtn.setEnabled(false);
            }
        });
    }
    private class MyThread extends Thread{
        @Override
        public void run() {

            Message mesg = new Message();
            Bundle data = new Bundle();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            greyImg(bitmap).compress(Bitmap.CompressFormat.JPEG,100,stream);
            byte[] bytes = stream.toByteArray();
            Log.v("brad","bmp轉byte");
            data.putByteArray("bmp", bytes);
            mesg.setData(data);
            uiHandler.sendMessage(mesg);
        }
    }

    public Bitmap greyImg(Bitmap img){

//-----------------------寫gcode.txt檔案------------------------------
//        File path = getExtermalStoragePublicDownLoadsDir("picturechooser");
//        Log.v("brad","greyImg:"+path.toString());
//        if(!path.exists()){
//            path.mkdir();
//        }
//        String gCodeName = "gcode"+System.currentTimeMillis()+".txt";
//        File gCodeFile = new File(path,gCodeName);
//        Log.v("brad","gCodeFile:"+gCodeFile.toString());
//-----------------------寫gcode.txt檔案------------------------------


        int width = img.getWidth();
        //int width = 600;
        int height = img.getHeight();
        //int height = 300;
        int pixels[] = new int[width*height];
        img.getPixels(pixels, 0, width, 0, 0, width, height);
        int alpha = 0xFF <<24;
        int k;


//-----------------------寫gcode.txt檔案------------------------------
//        try {
//            FileOutputStream output = new FileOutputStream(gCodeFile,true);
//-----------------------寫gcode.txt檔案------------------------------

        for(int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {

                if (i % 2 ==1){
                    k = width-j-1;
                }else{
                    k = j;
                }

                grey = pixels[width * i + k];
                int red = ((grey & 0x00FF0000) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);
                grey = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);


                float fGrey = grey;
                float elevationScale = (fGrey / 255) * 30;
                float elevation = (float) (Math.round(elevationScale * 100)) / 100;
//                Log.v("brad", "準備寫檔");
                String x = Integer.toString(k);
                String y = Integer.toString(i);
                String z = Float.toString(elevation);

                info = new Info();
                info.setX(x);
                info.setY(y);
                info.setZ(z);
                list.add(info);

//-----------------------寫gcode.txt檔案------------------------------
//                try {
//                    output.write("x".getBytes());
//                    output.write(x.getBytes());
//                    output.write(" ".getBytes());
//                    output.write("y".getBytes());
//                    output.write(y.getBytes());
//                    output.write(" ".getBytes());
//                    output.write("z".getBytes());
//                    output.write(z.getBytes());
//                    output.write("\r\n".getBytes());
//                    output.flush();
//
//                } catch (Exception e) {
//                    Log.v("brad", e.toString());
//                }
//-----------------------寫gcode.txt檔案------------------------------

                grey = alpha | (grey << 16) | (grey << 8) | grey;
                pixels[width * i + k] = grey;


            }
        }

//-----------------------寫gcode.txt檔案------------------------------
//            output.close();
//        } catch (Exception e) {
//            e.toString();
//        }
//-----------------------寫gcode.txt檔案------------------------------


        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;

    }
    private class UIHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Bundle data = msg.getData();
            byte[] bytes = data.getByteArray("bmp");
            Bitmap bmp = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
            Log.v("brad","byte轉bmp");
            displayImg.setImageBitmap(bmp);
            if(progressDialog.isShowing()) {
                progressDialog.hide();
            }
        }
    }

    private class GcodeThread extends Thread{
        @Override
        public void run() {

            File path = getExtermalStoragePublicDownLoadsDir("picturechooser");
            Log.v("brad","greyImg:"+path.toString());
            if(!path.exists()){
                path.mkdir();
            }
            String gCodeName = "gcode"+System.currentTimeMillis()+".txt";
            final File gCodeFile = new File(path,gCodeName);
            Log.v("brad","gCodeFile:"+gCodeFile.toString());
            long StartTime = System.nanoTime();
            try {
                FileOutputStream output = new FileOutputStream(gCodeFile,true);
                FileWriter fw = new FileWriter(gCodeFile);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write("G90 G01" + "\r\n");
                for(int i=0; i< list.size();i++){
                    try {
                        Info getinfo =list.get(i);
                        bw.write(("x" + getinfo.getX() + " y" + getinfo.getY() + " z" + getinfo.getZ() + "\r\n"));


//                    Info getinfo =list.get(i);
//                    output.write(("x" + getinfo.getX() + " y" + getinfo.getY() + " z" + getinfo.getZ() + "\r\n").getBytes());
                    } catch (Exception e) {
                        Log.v("brad", e.toString());
                    }
                }
                output.flush();
                output.close();
                list.clear();
            } catch (Exception e) {
                Log.v("brad",e.toString());
            }
            long EndTime = System.nanoTime();
            final long execTimeMs = (EndTime - StartTime) / 1000000;
            final long finTimes = execTimeMs/1000;
            Log.v("brad","total cost time(ms): " + execTimeMs);
            Log.v("brad","output finished");
            ((Activity)mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "檔案完成，花了"+ finTimes+"秒", Toast.LENGTH_LONG).show();
//                    progressBar.setMax(list.size());
//---------------------------------檔案更新-------------------------------------------
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    File f = new File(gCodeFile.toString());
                    Uri contentUri = Uri.fromFile(f);
                    mediaScanIntent.setData(contentUri);
                    mContext.sendBroadcast(mediaScanIntent);
//---------------------------------檔案更新-------------------------------------------
                }
            });
        }
    }
    private File getExtermalStoragePublicDownLoadsDir(String albumName) {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if(path.mkdir()){
            File f = new File(path, albumName);
            if(f.mkdir()){
                return f;
            }
        }
        return new File(path, albumName);
    }
}
