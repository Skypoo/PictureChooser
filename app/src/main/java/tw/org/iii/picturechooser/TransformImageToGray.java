package tw.org.iii.picturechooser;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Skypoo on 23/12/2016.
 */

public class TransformImageToGray {
    int grey;

    public Bitmap greyImg(Bitmap img){
        int width = img.getWidth();
        //int width = 600;
        int height = img.getHeight();
        //int height = 300;
        int pixels[] = new int[width*height];
        img.getPixels(pixels, 0, width, 0, 0, width, height);
        int alpha = 0xFF <<24;
        for(int i = 0; i < height; i++)
            for (int j = 0; j < width; j++) {
                grey = pixels[width * i + j];
                int red = ((grey & 0x00FF0000) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);
                grey = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
                Log.v("brad", "grey:" + grey);
                float fGrey = grey;
                float elevationScale = (fGrey / 255) * 5;
                float elevation = (float) (Math.round(elevationScale * 100)) / 100;

                String x = Integer.toString(j);
                String y = Integer.toString(i);
                String z = Float.toString(elevation);

//                try {
//                    FileOutputStream outputStream = openFileOutput("gcode.txt", MODE_APPEND);
//                    outputStream.write("x".getBytes());
//                    outputStream.write(x.getBytes());
//                    outputStream.write(" ".getBytes());
//                    outputStream.write("y".getBytes());
//                    outputStream.write(y.getBytes());
//                    outputStream.write(" ".getBytes());
//                    outputStream.write("z".getBytes());
//                    outputStream.write(z.getBytes());
//                    outputStream.write("\r\n".getBytes());
//                    outputStream.flush();
//                    outputStream.close();
//                } catch (IOException e) {
//                    Log.v("brad", e.toString());
//                }

                grey = alpha | (grey << 16) | (grey << 8) | grey;
                pixels[width * i + j] = grey;
            }

        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;

    }

}
