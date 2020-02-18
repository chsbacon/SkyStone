package org.firstinspires.ftc.teamcode;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Environment;

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
            // Cropped is         20 x 400
            // Scaled is          10 x  40 (400 pixels)
            int x = 706;
            int y1 = 160;
            int y2 = 360;
            int y3 = 540;
            bitmap = createBitmap(bitmap, x-10, y1-10, 20, y3-y1+20);
            // bitmap = createBitmap(bitmap, bitmap.getWidth() / 4, bitmap.getHeight() / 4, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
            // Save the cropped image
            if (SAVE_BITMAPS)
                saveBitmap(bitmap, "myBitmapCropped.png");

            // Now compress for the scan
            bitmap = createScaledBitmap(bitmap, 10, 40, true);

            // Save the scaled image
            if (SAVE_BITMAPS)
                saveBitmap(bitmap, "myBitmapScaled.png");

            int height;
            int width;
            int pixel;
            int bitmapWidth = bitmap.getWidth();
            int bitmapHeight = bitmap.getHeight();

            for (height = 0; height < bitmapHeight/3; ++height) {
                for (width = 0; width < bitmapWidth; ++width) {
                    pixel = bitmap.getPixel(width, height);
                    if (Color.red(pixel) > 100 && Color.green(pixel) > 100 && Color.blue(pixel) < 100) {
                        yellowR += 1;
                    }
                }
            }
            for (height = bitmapHeight/3; height < bitmapHeight/3*2; ++height) {
                for (width = 0; width < bitmapWidth; ++width) {
                    pixel = bitmap.getPixel(width, height);
                    if (Color.red(pixel) > 100 && Color.green(pixel) > 100 && Color.blue(pixel) < 100) {
                        yellowC += 1;
                    }
                }
            }
            for (height = bitmapHeight/3*2; height < bitmapHeight; ++height) {
                for (width = 0; width < bitmapWidth; ++width) {
                    pixel = bitmap.getPixel(width, height);
                    if (Color.red(pixel) > 100 && Color.green(pixel) > 100 && Color.blue(pixel) < 100) {
                        yellowL += 1;
                    }
                }
            }
            telemetry.addData("Left", yellowL);
            telemetry.addData("Center", yellowC);
            telemetry.addData("Right", yellowR);
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
