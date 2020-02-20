package org.firstinspires.ftc.teamcode;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Environment;
import android.util.Log;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.vuforia.PIXEL_FORMAT;
import com.vuforia.Vuforia;
import com.vuforia.Image;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static android.graphics.Bitmap.createBitmap;
import static android.graphics.Bitmap.createScaledBitmap;

@Autonomous(name = "Yellow Vuforia", group = "Opmode")
// @Disabled

public class ShieldsVuforiaSimple extends LinearOpMode {
    HardwareBlank robot = new HardwareBlank();

    private VuforiaLocalizer vuforia;

    public void runOpMode() {
        robot.init(hardwareMap);
        telemetry.addData("", "Greetings, Human.");
        telemetry.update();

        // Create parameters object to initialize Vuforia with
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters(cameraMonitorViewId);
        parameters.vuforiaLicenseKey = "AUOQWxb/////AAABmRP6L/V1T0Bclh/MquexUq8kKPD3h3N5sSIPraEvHInc1KyTB1KSLqkDd0mdJZibl8t7LsWmHogI6fR7p44UvkxD6uBvANg8xebRLgWIHaPvqxf3IqT8IG2VkljyPD/Unlfi357W5qXls0rtkFem3yX5kROTZEfRbmf5ZwtC3KSu6hBzriQwM7zk0zptP/MWtO6B/SZz6OWwLCR6O4I6TkKC7kQS3b1VGNonWq4fFL5jMcVPypqZKohDySdG4URcz0NqxpeEcC9P/c/VL67JKBcFaNBtix+7N/yccggWv8tUKuofNLIS1mUEv5kTzw9n4ps6ApmE2PziqmOjzpNL0MgF+V3KhRddiJjx51nFKEdX";
        parameters.cameraDirection = VuforiaLocalizer.CameraDirection.BACK;

        // Initialize Vuforia
        vuforia = ClassFactory.getInstance().createVuforia(parameters);

        // Ask Vuforia to provide RGB565 images.
        Vuforia.setFrameFormat(PIXEL_FORMAT.RGB565, true);
        // Ask Vuforia to keep just one image
        vuforia.setFrameQueueCapacity(1);

        // Wait for the driver to hit play
        waitForStart();
        // Pull raw images
        VuforiaLocalizer.CloseableFrame frame = null;
        Image rgbImage = null;
        double yellowL = 0;
        double yellowC = 0;
        double yellowR = 0;
        boolean SAVE_BITMAPS = true;
        int YELLOW_THRESHOLD = 1000;

        while (rgbImage == null) {
            try {
                frame = vuforia.getFrameQueue().take();
                long numImages = frame.getNumImages();

                for (int i = 0; i < numImages; i++) {
                    if (frame.getImage(i).getFormat() == PIXEL_FORMAT.RGB565) {
                        rgbImage = frame.getImage(i);
                        if (rgbImage != null) {
                            break;
                        }
                    }
                }
            } catch (InterruptedException exc) {

            } finally {
                if (frame != null) frame.close();
            }
        }
        if (rgbImage != null) {
            // Copy the bitmap from the Vuforia frame
            Bitmap bitmap = createBitmap(rgbImage.getWidth(), rgbImage.getHeight(), Bitmap.Config.RGB_565);
            bitmap.copyPixelsFromBuffer(rgbImage.getPixels());

            // Save the file
            if (SAVE_BITMAPS)
                saveBitmap(bitmap, "myBitmap.png");

            // Default image is 1280 x 720
            // Cropped is        640 x 720
            // Scaled is          64 x  72 (400 pixels)

            bitmap = createBitmap(bitmap, bitmap.getWidth() / 2, 0, bitmap.getWidth() / 2, bitmap.getHeight());
            // Save the cropped image
            if (SAVE_BITMAPS)
                saveBitmap(bitmap, "myBitmapCropped.png");

            // Now compress for the scan
            bitmap = createScaledBitmap(bitmap, 320, 360, true);

            // Save the scaled image
            if (SAVE_BITMAPS)
                saveBitmap(bitmap, "myBitmapScaled.png");

            int height;
            int width;
            int xStart = 0;
            int pixel;
            int pix1;
            int pix2;
            int pix3;
            int bitmapWidth = bitmap.getWidth();
            int bitmapHeight = bitmap.getHeight();
            int yellowCount;
            boolean foundX = false;
            int rowYs[] = {bitmapHeight/6, bitmapHeight/2, bitmapHeight/6*5};
            int rowYellows[] = {bitmapWidth, bitmapWidth, bitmapWidth};
            Log.i("BACON", Integer.toString(bitmapWidth));

            for (int i=0; i < rowYs.length; i++) {
                Log.i("BACON-loop", Integer.toString(i));
                for (width = bitmapWidth-1; width > 3; width -= 3) {
                    pix1 = bitmap.getPixel(width, rowYs[i]);
                    pix2 = bitmap.getPixel(width - 1, rowYs[i]);
                    pix3 = bitmap.getPixel(width - 2, rowYs[i]);
                    yellowCount = 0;
                    if (Color.red(pix1) > 100 && Color.green(pix1) > 100 && Color.blue(pix1) < 100)
                        yellowCount += 1;
                    if (Color.red(pix2) > 100 && Color.green(pix2) > 100 && Color.blue(pix2) < 100)
                        yellowCount += 1;
                    if (Color.red(pix3) > 100 && Color.green(pix3) > 100 && Color.blue(pix3) < 100)
                        yellowCount += 1;
                    if (yellowCount == 3 && !foundX) {
                        foundX = true;
                        rowYellows[i] = width;
                    }
                }
            }
            Log.i("BACON-xStart", Integer.toString(xStart));
            /*
            for (height = 0; height < bitmapHeight/3; ++height) {
                for (width = xStart; width < xStart+40; ++width) {
                    pixel = bitmap.getPixel(width, height);
                    if (Color.red(pixel) > 100 && Color.green(pixel) > 100 && Color.blue(pixel) < 100) {
                        yellowR += 1;
                    }
                }
            }
            for (height = bitmapHeight/3; height < bitmapHeight/3*2; ++height) {
                for (width = xStart; width < xStart+40; ++width) {
                    pixel = bitmap.getPixel(width, height);
                    if (Color.red(pixel) > 100 && Color.green(pixel) > 100 && Color.blue(pixel) < 100) {
                        yellowC += 1;
                    }
                }
            }
            for (height = bitmapHeight/3*2; height < bitmapHeight; ++height) {
                for (width = xStart; width < xStart+40; ++width) {
                    pixel = bitmap.getPixel(width, height);
                    if (Color.red(pixel) > 100 && Color.green(pixel) > 100 && Color.blue(pixel) < 100) {
                        yellowL += 1;
                    }
                }
            }
            */
            telemetry.addData("rowYellow0", rowYellows[0]);
            telemetry.addData("rowYellow1", rowYellows[1]);
            telemetry.addData("rowYellow2", rowYellows[2]);
            /*
            if(rowYellows[0] < rowYellows[1] && rowYellows[0] < rowYellows[2])
                telemetry.addData("Stone", "RIGHT");
            if(rowYellows[1] < rowYellows[0] && rowYellows[1] < rowYellows[2])
                    telemetry.addData("Stone", "CENTER");
            if(rowYellows[2] < rowYellows[0] && rowYellows[2] < rowYellows[1])
                telemetry.addData("Stone", "LEFT");

             */
            telemetry.update();
            while(opModeIsActive()) {}
        }
    }
    void saveBitmap(Bitmap bitmap, String fileName) {
        String path = Environment.getExternalStorageDirectory().toString();
        FileOutputStream out = null;
        try {
            File file = new File(path, fileName);
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.flush();
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
