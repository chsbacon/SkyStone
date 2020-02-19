
package org.firstinspires.ftc.teamcode;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;

import com.qualcomm.hardware.rev.RevBlinkinLedDriver;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.robocol.Heartbeat;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;
import com.qualcomm.robotcore.hardware.ColorSensor;


import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

import java.util.concurrent.TimeUnit;


/**
 * This file contains an minimal example of a Linear "OpMode". An OpMode is a 'program' that runs in either
 * the autonomous or the teleop period of an FTC match. The names of OpModes appear on the menu
 * of the FTC Driver Station. When an selection is made from the menu, the corresponding OpMode
 * class is instantiated on the Robot Controller and executed.
 * <p>
 * This particular OpMode just executes a basic Tank Drive Teleop for a two wheeled robot
 * It includes all the skeletal structure that all linear OpModes contain.
 * <p>
 * Use Android Studios to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this opmode to the Driver Station OpMode list
 */

@TeleOp(name = "BACON: Mechanum", group = "Opmode")
//@Disabled
public class BACONbotMechanum extends LinearOpMode {

    // Declare OpMode members.
    private ElapsedTime runtime = new ElapsedTime();
    HardwareBACONbot robot = new HardwareBACONbot();   // Use a BACONbot's hardware
    int teamcolor = 0; // 1 = Blue 2 = Red
    int blue = 1;
    int red = 2;
    int STOPPED = 1;
    int MOVINGUP = 2;
    int MOVINGDOWN = 3;
    int liftState = STOPPED;
    int ON = 1;
    int OFF = 2;
    int wheelServoState = OFF;
    int dpad2 = OFF; //dpad2 is off

    @Override
    public void runOpMode() {
        telemetry.addData("Status", "Initializing");
        telemetry.update();

        int relativeLayoutId = hardwareMap.appContext.getResources().getIdentifier("RelativeLayout", "id", hardwareMap.appContext.getPackageName());
        final View relativeLayout = ((Activity) hardwareMap.appContext).findViewById(relativeLayoutId);
        /* Initialize the hardware variables.
         * The init() method of the hardware class does all the work here
         */
        robot.init(hardwareMap);
        /*
        telemetry.addData("Press X for Blue, B for Red", "");
        telemetry.update();
        // TODO: What will go in this while loop?
        while (!gamepad1.x && !gamepad1.b) {
        }
        if (gamepad1.x) {
            teamcolor = blue;
            // Set the panel back to the default color
            relativeLayout.post(new Runnable() {
                public void run() {
                    relativeLayout.setBackgroundColor(Color.BLUE);
                    robot.pattern = RevBlinkinLedDriver.BlinkinPattern.HEARTBEAT_BLUE;
                    robot.blinkinLedDriver.setPattern(robot.pattern);
                }
            });
        }
        if (gamepad1.b) {
            teamcolor = red;
            // Set the panel back to the default color
            relativeLayout.post(new Runnable() {
                public void run() {
                    relativeLayout.setBackgroundColor(Color.RED);
                    robot.pattern = RevBlinkinLedDriver.BlinkinPattern.HEARTBEAT_RED;
                    robot.blinkinLedDriver.setPattern(robot.pattern);
                }
            });
        }
        */

        robot.capstoneServo.setPosition(.5);

        // Tell the driver that initialization is complete.
        telemetry.addData("Status", "Initialized");

        // Wait for the game to start (driver presses PLAY)
        waitForStart();
        runtime.reset();

        double meetDistance = 790; //Distance from wall to the Blocks/Mat (CM From Wall (BackSensor))
        double leftMeetDistance = 10;

        float grabPos = 0;  //change these later (written 12-3-19)
        float freePos = 1;  //change these later  (written 12-3-19)

        float currentPos = 0;
        double clawServoState;
        double x;
        double y;
        double r;
        double frontLeft;
        double frontRight;
        double backLeft;
        double backRight;
        double max;
        double step = 0.2;
        double interval = 75;
        double targAng;
        double currAng;
        double error;
        double lastSpeedTime = runtime.milliseconds();
        double fastSlow;
        double xUp = 0;  // gamepad2 x button is up
        double aUp = 0;  // gamepad2 a button is up
        double bUp = 0;  // gamepad2 b button is up
        Orientation currOrient;
        Orientation target = robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
        ;
        Boolean released = true;
        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {
            /*relativeLayout.post(new Runnable() {
                public void run() {
                    robot.pattern = RevBlinkinLedDriver.BlinkinPattern.WHITE;  //used to be green but want white for blue tape
                    robot.blinkinLedDriver.setPattern(robot.pattern);
                }
            });*/
            // In this mode the Left stick moves the robot in the direction pointed to by x,y
            //              the Right stick x controls rotation; right (positive) rotates clockwise
            // Get x and y values from left joystick. (With the Logitech 310 the joystick y goes negative when pushed forwards, so negate it)
            // Get the x value of the right joystick.
            if (gamepad1.left_stick_button) {
                fastSlow = 2;
            } else {
                fastSlow = 1;
            }
            y = gamepad1.left_stick_y;
            x = gamepad1.left_stick_x;
            r = gamepad1.right_stick_x;
            currOrient = robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
            // do not let rotation dominate movement
            r = r / 3;
            if (r > 0.01 || r < 0.01) {
                target = currOrient;
            }
            if (r == 0) {

                currAng = currOrient.angleUnit.DEGREES.normalize(currOrient.firstAngle);
                targAng = target.angleUnit.DEGREES.normalize(target.firstAngle);
                error = (targAng - currAng) / 180 * .4;
                if ((error < .05 || error > -.05)) {
                    error = 0;
                }
                r += error;
            }
            // calculate the power for each wheel

            backRight = +y - x + r;
            frontRight = +y + x + r;
            backLeft = -y - x + r;
            frontLeft = -y + x + r;

            if (runtime.milliseconds() > lastSpeedTime + interval) {
                lastSpeedTime = runtime.milliseconds();

                frontLeft = getRampPower(frontLeft, robot.frontLeftMotor.getPower(), step);
                frontRight = getRampPower(-frontRight, -robot.frontRightMotor.getPower(), step);
                backLeft = getRampPower(backLeft, robot.backLeftMotor.getPower(), step);
                backRight = getRampPower(-backRight, -robot.backRightMotor.getPower(), step);

                frontRight = -frontRight;
                backRight = -backRight;


                // Set power on each wheel
                robot.frontLeftMotor.setPower(frontLeft / fastSlow);
                robot.frontRightMotor.setPower(frontRight / fastSlow);
                robot.backLeftMotor.setPower(backLeft / fastSlow);
                robot.backRightMotor.setPower(backRight / fastSlow);
            }
            //Raise and Lower Lift Motor (Manual)
            //Left Bumper = Up
            //Right Bumper = Down
            if (gamepad2.right_bumper && robot.liftMotor.getCurrentPosition() < 0) {
                robot.liftMotor.setPower(1); //down
            } else if (gamepad2.left_bumper && robot.liftMotor.getCurrentPosition() > -18000) {
                robot.liftMotor.setPower(-1);  //up
            } else {
                robot.liftMotor.setPower(0);
            }
            /*  this is code to move all the way down or up one block level
                if we turn it on we need to find another way to reset encoder
                old gamepad2.b code needs to be commented out below.

            if (gamepad2.a && aUp == 1) {
                aUp = 0;
                liftState = MOVINGUP;
                robot.liftMotor.setPower(-1);
            }
            if (liftState == MOVINGUP && (robot.liftMotor.getCurrentPosition() < -2000)) {
                robot.liftMotor.setPower(0);
                liftState = STOPPED;
            }
            if (!gamepad2.a) {
                aUp = 1;
            }
            if (gamepad2.b && bUp == 1) {
                bUp = 0;
                liftState = MOVINGDOWN;
                robot.liftMotor.setPower(1);
            }
            if (liftState == MOVINGDOWN && (robot.liftMotor.getCurrentPosition() > 0)) {
                robot.liftMotor.setPower(0);
                liftState = STOPPED;
            }
            if (!gamepad2.b) {
                bUp = 1;
            }
            *********  */

            if (gamepad2.b) {
                liftState = MOVINGDOWN;
                robot.liftMotor.setPower(1);
            }
            if (liftState == MOVINGDOWN && (robot.liftMotor.getCurrentPosition() > 0)) {
                robot.liftMotor.setPower(0);
                liftState = STOPPED;
            }

            //matServo Servo Position
            double spL;
            spL = robot.matServoL.getPosition();
            double spR;
            spR = robot.matServoR.getPosition();

            //Grab mat
            //Down D-pad - Grab
            //Up D-pad - Free
            if (gamepad1.dpad_down) {
                robot.matServoL.setPosition(spL + .1);
                robot.matServoR.setPosition(spR - .1);
            }
            if (gamepad1.dpad_up) {
                robot.matServoL.setPosition(spL - .1);
                robot.matServoR.setPosition(spR + .1);
            }

            double GrabPos = 1;
            double FreePos = 0;

            if (gamepad1.dpad_down) {
                robot.matServoL.setPosition(freePos);
                robot.matServoR.setPosition(grabPos);
            }
            if (gamepad1.dpad_up) {
                robot.matServoL.setPosition(grabPos);
                robot.matServoR.setPosition(freePos);
            }




            /*
            if(gamepad1.x){
                currrentPos = grabPos
                robot.clawServo.setPosition(currentPos);
            }
            else if (gamepad1.y){
                robot.clawServo.setPosition(freePos);
            }
            else{
            }
            */

            //Claw Servo (Blocks)
            double sp;
            sp = robot.clawServo.getPosition();


            if (gamepad2.x && (xUp == 1)) {
                xUp = 0;

                if (sp == 0) {
                    robot.clawServo.setPosition(.2);
                    robot.pattern = RevBlinkinLedDriver.BlinkinPattern.WHITE;
                    robot.blinkinLedDriver.setPattern(robot.pattern);
                    robot.wheelServoL.setPosition(.75);
                    robot.wheelServoR.setPosition(.25);
                }
                if (sp == 1) {
                    robot.clawServo.setPosition(.75);
                    robot.pattern = RevBlinkinLedDriver.BlinkinPattern.HEARTBEAT_BLUE;
                    robot.blinkinLedDriver.setPattern(robot.pattern);
                    robot.wheelServoL.setPosition(.5);
                    robot.wheelServoR.setPosition(.5);
                }
            }


            if (!gamepad2.x) {
                xUp = 1;
            }
            //Capstone Servo
            double capstone;
            capstone = robot.capstoneServo.getPosition();

            if (gamepad2.y) {
                if (capstone == .5) {
                    robot.capstoneServo.setPosition(1);
                    sleep(500);
                }
                if (capstone == 1) {
                    robot.capstoneServo.setPosition(.5);
                    sleep(500);
                }
                telemetry.addData("Capstone Servo Position", robot.capstoneServo.getPosition());
                telemetry.update();
            }
            if (gamepad2.a) {
                robot.liftMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                robot.liftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            }

            if (gamepad2.dpad_down && (dpad2 == OFF)) {
                if (wheelServoState == OFF) {
                    robot.wheelServoL.setPosition(1.0);
                    // robot.wheelServoR.setDirection(DcMotorSimple.Direction.REVERSE);
                    robot.wheelServoR.setPosition(0.0);
                    wheelServoState = ON;

                } else if (wheelServoState == ON) {
                    robot.wheelServoL.setPosition(0.48);
                    robot.wheelServoR.setPosition(0.49);
                    wheelServoState = OFF;

                }
                dpad2 = ON;
            }
            if (!gamepad2.dpad_down) {
                dpad2 = OFF;
            }



            telemetry.addData("Left Sensor","Alpha:" + robot.colorSensorL.alpha());
            telemetry.addData("Right Snesor", "Alpha:" + robot.colorSensorR.alpha());
            //telemetry.addData("Blue  ", robot.colorSensorDown.blue());
            telemetry.update();

            /*
            telemetry.addData("FrontLeft:", frontLeft);
            telemetry.addData("FrontRight", frontRight);
            telemetry.addData("BackLeft", backLeft);
            telemetry.addData("BackRight", backRight);
/*
            // Show wheel power to driver
            telemetry.addData("Status", "Run Time: " + runtime.toString());
            telemetry.addData("lastSpeedTime",  lastSpeedTime);
            telemetry.addData("front left", "%.2f", frontLeft);
            telemetry.addData("front right", "%.2f", frontRight);
            telemetry.addData("back left", "%.2f", backLeft);
            telemetry.addData("back right", "%.2f", backRight);
            telemetry.addData("lift pos", robot.liftMotor.getCurrentPosition());
            telemetry.addData("claw pos", robot.clawServo.getPosition());
            // telemetry.addData("back distance--", String.format("%.01f mm", robot.backDistance.getDistance(DistanceUnit.MM)));
            //telemetry.addData("left distance--", String.format("%.01f mm", robot.leftDistance.getDistance(DistanceUnit.MM))); //Added this one
           // telemetry.addData("colorSensor--", String.format("%.01f mm", robot.distanceSensorL.getDistance(DistanceUnit.MM))); //Added this one

*/
            telemetry.update();
        }
    }

    // Functions --------------------------------------

    double getRampPower(double t, double a, double step) {
        double delta;
        double returnPower = 0;

        delta = t - a;
        if (delta > 0) {  // speeding up
            returnPower = a + step;
            if (returnPower > t) {
                returnPower = t;
            }
        }
        if (delta < 0) {  //slowing down
            returnPower = a - (step);
            if (returnPower < t)
                returnPower = t;
        }
        if (delta == 0) {
            returnPower = a;
        }
        return returnPower;
    }
}