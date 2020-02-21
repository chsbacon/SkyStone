// FTC Team 7080 BACON
// Autonomous code for red & blue side, Stone & Mat side

package org.firstinspires.ftc.teamcode;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.view.View;

import com.qualcomm.hardware.rev.RevBlinkinLedDriver;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.hardware.rev.Rev2mDistanceSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.teamcode.HardwareBACONbot;

import java.util.Locale;
//import org.firstinspires.ftc.teamcode.Teleops.HardwareMap;

@Autonomous(name = "BACON: Autonomous 2020", group = "Opmode")
//@Disabled

//name file AutonomousB
public class Auto extends LinearOpMode {
    private ElapsedTime runtime = new ElapsedTime();
    HardwareBACONbot robot = new HardwareBACONbot();   // Use BACONbot's hardware
    // === DEFINE CONSTANTS HERE! ===
    double STRAFE_SPEED = 0.3;  // Motor power global variables
    double FAST_SPEED = 1.0;
    double SLOW_SPEED = 0.2;
    int BLUETAPE = 30; // Blue tape down sensor color value
    int REDTAPE = 35; // Red tape down sensor color value
    int blue = 1;
    int red = 0;
    boolean parkonly = false;
    int mat = 1;
    int stones = 2;
    int FRONTDIST = 120; //used to be 160
    int parkWait = 20000;

    // ==============================
    public void runOpMode() {
        int teamcolor = 0; // 1 = Blue 2 = Red
        int task = 0; //1 = mat 2 = stones

        double meetDistance = 860; //Distance from wall to the Blocks/Mat (CM From Wall (BackSensor))
        double lastTime = runtime.milliseconds();

        float grabPos = 0;  //change these later (written 12-3-19)
        float freePos = 1;  //change these later  (written 12-3-19)


        // State used for updating telemetry
        Orientation angles;
        Acceleration gravity;

        // get a reference to the RelativeLayout so we can change the background
        // color of the Robot Controller app to match the hue detected by the RGB sensor.
        int relativeLayoutId = hardwareMap.appContext.getResources().getIdentifier("RelativeLayout", "id", hardwareMap.appContext.getPackageName());
        final View relativeLayout = ((Activity) hardwareMap.appContext).findViewById(relativeLayoutId);


        robot.init(hardwareMap);
        // Choosing the team color
        telemetry.addData("Press X for Blue, B for Red", "");
        telemetry.update();
        openClaw();

        while (!gamepad1.x && !gamepad1.b) {
        }
        //This sets the strips of lights and the screen of the phones to the team color
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

        telemetry.addData("teamcolor ", teamcolor);
        telemetry.update();

        // Choosing the task
        telemetry.addData("Press A for mat, Y for stones", "");
        telemetry.update();
        while (!gamepad1.a && !gamepad1.y) {
        }
        if (gamepad1.a) {
            task = mat;
        }
        if (gamepad1.y) {
            task = stones;
        }
        telemetry.addData("Press X for parkonly, b for full auto", "");
        telemetry.update();
        while (!gamepad1.x && !gamepad1.b) {
        }

        if (gamepad1.x) {
            parkonly = true;
        }
        if (gamepad1.b) {
            parkonly = false;
        }

        telemetry.addData("parkonly:", parkonly);
        telemetry.update();



        Rev2mDistanceSensor sensorTimeOfFlight = (Rev2mDistanceSensor) robot.backDistance;

        //mat servos up
        robot.matServoL.setPosition(freePos);
        robot.matServoR.setPosition(grabPos);

        waitForStart();
        runtime.reset();

        // run until the end of the match (driver presses STOP)
        if((parkonly==true)&&(teamcolor==red)&&(task==stones)){
            robot.pattern = RevBlinkinLedDriver.BlinkinPattern.WHITE;
            robot.blinkinLedDriver.setPattern(robot.pattern);
            closeClaw();
            sleep(parkWait);
            driveForwardSlow();
            while(robot.backDistance.getDistance(DistanceUnit.MM) < 650 ){

            }
            stopDriving();

            Orientation targOrient;
            targOrient = robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

            while (robot.colorSensorDown.red() < REDTAPE && opModeIsActive()) {
                strafeLeft(mat, .3,targOrient);
                sleep(10);
                telemetry.addData("parking Red  ", robot.colorSensorDown.red());
                telemetry.addData("parking Alpha  ", robot.colorSensorDown.alpha());
                telemetry.update();
            }
            stopDriving();
            driveForwardSlow();
            sleep(100);


        }
        if((parkonly==true)&&(teamcolor==red)&&(task==mat)){
            robot.pattern = RevBlinkinLedDriver.BlinkinPattern.WHITE;
            robot.blinkinLedDriver.setPattern(robot.pattern);
            closeClaw();
            sleep(parkWait);
            driveForward();
            sleep(100);

            Orientation targOrient;
            targOrient = robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

            while (robot.colorSensorDown.red() < REDTAPE && opModeIsActive()) {
                strafeRight(mat, .3,targOrient);
                sleep(10);
                telemetry.addData("parking Red  ", robot.colorSensorDown.red());
                telemetry.addData("parking Alpha  ", robot.colorSensorDown.alpha());
                telemetry.update();
            }
            stopDriving();
            sleep(100);


        }
        if((parkonly==true)&&(teamcolor ==blue)&&(task==stones)){
            robot.pattern = RevBlinkinLedDriver.BlinkinPattern.WHITE;
            robot.blinkinLedDriver.setPattern(robot.pattern);
            closeClaw();
            sleep(parkWait);
            driveForwardSlow();
            while(robot.backDistance.getDistance(DistanceUnit.MM) < 650 ){

            }
            stopDriving();

            Orientation targOrient;
            targOrient = robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

            while (robot.colorSensorDown.blue() < BLUETAPE && opModeIsActive()) {
                strafeRight(mat, .3,targOrient);
                sleep(10);
                telemetry.addData("parking Blue  ", robot.colorSensorDown.blue());
                telemetry.addData("parking Alpha  ", robot.colorSensorDown.alpha());
                telemetry.update();
            }
            stopDriving();
            driveForwardSlow();
            sleep(100);


        }
        if((parkonly==true)&&(teamcolor==blue)&&(task==mat)){
            robot.pattern = RevBlinkinLedDriver.BlinkinPattern.WHITE;
            robot.blinkinLedDriver.setPattern(robot.pattern);
            closeClaw();
            sleep(parkWait);
            driveForward();
            sleep(100);
            Orientation targOrient;
            targOrient = robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

            while (robot.colorSensorDown.blue() < BLUETAPE && opModeIsActive()) {
                strafeLeft(mat, .3,targOrient);
                sleep(10);
                telemetry.addData("parking Blue  ", robot.colorSensorDown.blue());
                telemetry.addData("parking Alpha  ", robot.colorSensorDown.alpha());
                telemetry.update();
            }
            stopDriving();
            sleep(100);


        }
        //Stones --------------------------------------------------------------------------++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        //First troubleshooting steps for this section would be to check the direction of the strafes in scan and grab
        if ((task == stones) && (teamcolor == red)&& !parkonly) {
            robot.pattern = RevBlinkinLedDriver.BlinkinPattern.WHITE;
            robot.blinkinLedDriver.setPattern(robot.pattern);
            //This lifts the claw one level so that it won't be in the way of the blocks while scanning
            raiseClaw();
            //raiseClaw();
            //This gets the robot in the proper place to sense the Skystones
            positionRobot();
            //This performs the scanning operation until we find a Skystone
            scan(red);
            //Setting it up to go up and grab the Skystone
            grabPrep();
            //Pick up the Skystone
            grabStone();
            //rotate to face mat side
            rotateR(-80.0, 0.3);
            //Park on the tape
            parkStonesRed();
            //go drop the stone in the build zone and return to the parking line
            outAndBackRed();
            stopDriving();

        }
        if ((task == stones) && (teamcolor == blue)&& !parkonly) {
            robot.pattern = RevBlinkinLedDriver.BlinkinPattern.WHITE;
            robot.blinkinLedDriver.setPattern(robot.pattern);
            //This lifts the claw one level so that it won't be in the way of the blocks while scanning
            raiseClaw();
            raiseClaw();
            //This gets the robot in the proper place to sense the Skystones
            positionRobot();
            //This performs the scanning operation until we find a Skystone
            scan(blue);
            //Setting it up to go up and grab the Skystone
            grabPrep();
            //Pick up the Skystone
            grabStone();
            //rotate to face mat side
            rotateL(80.0, 0.3);
            //Park on the tape
            parkStonesBlue();
            //go drop the stone in the build zone and return to the parking line
            outAndBackBlue();
            stopDriving();

        }

        //----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        //BLUE TEAM MAT
        if ((task == mat) && (teamcolor == blue)&& !parkonly) {
            //robot.pattern = RevBlinkinLedDriver.BlinkinPattern.WHITE;
            //robot.blinkinLedDriver.setPattern(robot.pattern);

            closeClaw();
            raiseClaw();
            raiseClaw();

            while ((robot.backDistance.getDistance(DistanceUnit.MM) < meetDistance) && opModeIsActive()) //drive to mat
            {
                driveForwardSlow();
            }
            stopDriving();
            lastTime = runtime.milliseconds();
            Orientation targOrient;
            targOrient = robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
            while (runtime.milliseconds() < lastTime + 1250) { //1000 to 1500
                strafeRight(mat, .3, targOrient);
            }
            stopDriving();
            driveForwardSlow();
            sleep(500); //used to be 250
            stopDriving();

            //servos down
            robot.matServoL.setPosition(grabPos);
            robot.matServoR.setPosition(freePos);
            sleep(1000); //We can edit this delay based on it we need more time or not
            driveBackwardsSlow();
            lastTime = runtime.milliseconds();

            while ((robot.backDistance.getDistance(DistanceUnit.MM) > 50/*used to be75*/) && ((runtime.milliseconds()-lastTime) < 5000) && opModeIsActive()) //drivetomat
            {
                telemetry.addData("backing up", "Back Distance: " + robot.backDistance.getDistance(DistanceUnit.MM));
                telemetry.update();
            }
            stopDriving();


            //mat servos up
            robot.matServoL.setPosition(freePos);
            robot.matServoR.setPosition(grabPos);
            sleep(1000); //this makes sure we don't knock the mat when we begin to go towards parking

            lastTime = runtime.milliseconds();
            //this actually makes it go left toward the center of the mat
            while (runtime.milliseconds() < lastTime + 4000) {
                strafeLeft(mat, .3, targOrient);
            }

            stopDriving();
            lowerClaw();
            robot.pattern = RevBlinkinLedDriver.BlinkinPattern.WHITE;
            robot.blinkinLedDriver.setPattern(robot.pattern);
            sleep(500);

            while (robot.colorSensorDown.blue() < BLUETAPE && opModeIsActive()) {
                strafeLeft(mat,.3, targOrient);
            }
            stopDriving();

            driveBackwardsSlow();
            sleep(500);
            stopDriving();
        }


        if ((task == mat) && (teamcolor == red)&& !parkonly) {
            robot.pattern = RevBlinkinLedDriver.BlinkinPattern.WHITE;
            robot.blinkinLedDriver.setPattern(robot.pattern);

            closeClaw();
            raiseClaw();
            raiseClaw();

            driveForwardSlow();
            while ((robot.backDistance.getDistance(DistanceUnit.MM) < meetDistance) && opModeIsActive()) //drive to mat
            {
            }
            stopDriving();
            lastTime = runtime.milliseconds();
            Orientation targOrient;
            targOrient = robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

            while (runtime.milliseconds() < lastTime + 1500) {  //Different from red side
                strafeLeft(mat, .3, targOrient);
            }
            stopDriving();
            driveForwardSlow();
            sleep(500);
            stopDriving();

            //servos down
            robot.matServoL.setPosition(grabPos);
            robot.matServoR.setPosition(freePos);
            sleep(1000); //We can edit this delay based on it we need more time or not

            driveBackwardsSlow();
            lastTime = runtime.milliseconds();

            while ((robot.backDistance.getDistance(DistanceUnit.MM) > 50) && ((runtime.milliseconds()-lastTime) < 5000) && opModeIsActive()) //drivetomat
            {
                telemetry.addData("backing up", "Back Distance: " + robot.backDistance.getDistance(DistanceUnit.MM));
                telemetry.update();
            }
            stopDriving();

            //mat servos up
            robot.matServoL.setPosition(freePos);
            robot.matServoR.setPosition(grabPos);
            sleep(1000); //this makes sure we don't knock the mat when we begin to go towards parking

            lastTime = runtime.milliseconds();
            //this actually makes it go left toward the center of the mat
            while (runtime.milliseconds() < lastTime + 4000) {
                strafeRight(mat, .3, targOrient);
            }

            stopDriving();
            lowerClaw();
            robot.pattern = RevBlinkinLedDriver.BlinkinPattern.WHITE;
            robot.blinkinLedDriver.setPattern(robot.pattern);
            sleep(500);

            while (robot.colorSensorDown.red() < REDTAPE && opModeIsActive()) {
                strafeRight(mat,.3, targOrient);
                telemetry.addData("Red  ", robot.colorSensorDown.red());
                telemetry.update();
            }
            stopDriving();

            driveBackwardsSlow();
            sleep(500);
            stopDriving();
        }
    }
    // Functions ----------------------------------------------------------------------------------------------------------------
    //Driving Functions

    //Stop Driving - Kill power to all the motors
    void stopDriving() {
        robot.frontLeftMotor.setPower(0);
        robot.frontRightMotor.setPower(0);
        robot.backLeftMotor.setPower(0);
        robot.backRightMotor.setPower(0);
    }

    //Drive Backwards - Used for starting the game
    void driveBackwards() {
        robot.frontLeftMotor.setPower(-0.5);
        robot.frontRightMotor.setPower(0.5);
        robot.backLeftMotor.setPower(-0.5);
        robot.backRightMotor.setPower(0.5);
    }

    //Drive Backwards - Used for starting the game
    void driveBackwardsSlow() {
        robot.frontLeftMotor.setPower(-0.3);
        robot.frontRightMotor.setPower(0.3);
        robot.backLeftMotor.setPower(-0.3);
        robot.backRightMotor.setPower(0.3);
    }

    //Drive Forwards - Towards where the Backsensor is facing
    void driveForward() {
        robot.frontLeftMotor.setPower(0.5);
        robot.backLeftMotor.setPower(0.5);
        robot.backRightMotor.setPower(-0.5);
        robot.frontRightMotor.setPower(-0.5);
    }

    //Drive Forwards - Towards where the Backsensor is facing
    void driveForwardSlow() {
        robot.frontLeftMotor.setPower(SLOW_SPEED);
        robot.backLeftMotor.setPower(SLOW_SPEED);
        robot.backRightMotor.setPower(-1 * SLOW_SPEED);
        robot.frontRightMotor.setPower(-1 * SLOW_SPEED);
    }

    //Maintain a target heading (not used in the code)
    double maintainHeading(Orientation target, double pwr) {
        //get the current heading
        Orientation currOrient = robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

        //compare the current angle to the target angle
        double currAngle = currOrient.angleUnit.DEGREES.normalize(currOrient.firstAngle);
        double targAngle = target.angleUnit.DEGREES.normalize(target.firstAngle);

        //scale the error to the target value, and scale it by pwr so that it
        //doesn't overpower the movement
        //this may need to be adjusted positive or negative
        double error = (targAngle - currAngle) / 180 * pwr;

        //return that value so that it can be used to adjust the power
        return error;
    }

    //Strafe Left - (used to strafe towards the center line for parking)
    void strafeLeft(int side, double pwr, Orientation target) {  //added int pwr to reduce initial power
        //Get the current orientation
        Orientation currOrient;
        currOrient = robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

        //Compare the current orientation to the target
        double currAng = currOrient.angleUnit.DEGREES.normalize(currOrient.firstAngle);
        double targAng = 0.0;  // target.angleUnit.DEGREES.normalize(target.firstAngle);
        double error = targAng - currAng;
        double frontLeft;
        double frontRight;
        double backLeft;
        double backRight;
        double max;
        //scale the error so that it is a motor value and
        //then scale it by a third of the power to make sure it
        //doesn't dominate the movement
        double r = -error / 180 * (pwr * 10);
        double d; // Front distance correction
        d = 0;  //-(FRONTDIST - 45 - robot.frontDistance.getDistance(DistanceUnit.MM)) / 200;
        if (side == mat) {
            d = 0;
        }
        // Normalize the values so none exceeds +/- 1.0
        frontLeft = pwr + r + d;
        backLeft = -pwr + r + d;
        backRight = -pwr + r - d;
        frontRight = pwr + r - d;
        max = Math.max(Math.max(Math.abs(frontLeft), Math.abs(frontRight)), Math.max(Math.abs(frontRight), Math.abs(frontRight)));
        if (max > 1.0) {
            frontLeft = frontLeft / max;
            frontRight = frontRight / max;
            backLeft = backLeft / max;
            backRight = backRight / max;
        }
        //send the power to the motors
        robot.frontLeftMotor.setPower(frontLeft);
        robot.backLeftMotor.setPower(backLeft); //Changing the order in which the wheels start
        robot.backRightMotor.setPower(backRight);
        robot.frontRightMotor.setPower(frontRight);
    }

    void strafeRight(int side, double pwr, Orientation target) {  //added int pwr to reduce initial power
        //Get the current orientation
        Orientation currOrient;
        currOrient = robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

        //Compare the current orientation to the target
        double currAng = currOrient.angleUnit.DEGREES.normalize(currOrient.firstAngle);
        double targAng = 0.0;  // target.angleUnit.DEGREES.normalize(target.firstAngle);
        double error = targAng - currAng;
        double frontLeft;
        double frontRight;
        double backLeft;
        double backRight;
        double max;
        //scale the error so that it is a motor value and
        //then scale it by a third of the power to make sure it
        //doesn't dominate the movement
        double r = -error / 180 * (pwr * 10);
        double d; // Front distance correction
        d = -(FRONTDIST - 45 - robot.frontDistance.getDistance(DistanceUnit.MM)) / 200;
        if (side == mat) {
            d = 0;
        }
        // Normalize the values so none exceeds +/- 1.0
        frontLeft = -pwr + r + d;
        backLeft = pwr + r + d;
        backRight = pwr + r - d;
        frontRight = -pwr + r - d;
        max = Math.max(Math.max(Math.abs(frontLeft), Math.abs(frontRight)), Math.max(Math.abs(frontRight), Math.abs(frontRight)));
        if (max > 1.0) {
            frontLeft = frontLeft / max;
            frontRight = frontRight / max;
            backLeft = backLeft / max;
            backRight = backRight / max;
        }
        //send the power to the motors
        robot.frontLeftMotor.setPower(frontLeft);
        robot.backLeftMotor.setPower(backLeft); //Changing the order in which the wheels start
        robot.backRightMotor.setPower(backRight);
        robot.frontRightMotor.setPower(frontRight);
    }


    void outAndBackRed() {
        driveForwardSlow(); //Out from the parking tape under the skybridge
        sleep(1000);
        stopDriving();
        raiseClaw();
        driveForwardSlow();
        sleep(1500); //used to be 2000
        stopDriving();
        openClaw(); //Claw servo in the open position
        sleep(300);
        stopDriving();
        driveBackwardsSlow();  //Back to the parking tape under the skybridge
        sleep(1000);
        stopDriving();
        lowerClaw();
        sleep(250);
        driveBackwardsSlow();

        //Stop at the red tape
        while (robot.colorSensorDown.red() < REDTAPE && opModeIsActive()) {
        }
        stopDriving();
    }

    void outAndBackBlue() {
        driveForwardSlow(); //Out from the parking tape under the skybridge
        sleep(1000);
        stopDriving();
        raiseClaw();
        driveForwardSlow();
        sleep(1500); //used to be 2000
        stopDriving();
        openClaw(); //Claw servo in the open position
        sleep(300);
        stopDriving();
        driveBackwardsSlow();  //Back to the parking tape under the skybridge
        sleep(1000);
        stopDriving();
        lowerClaw();
        sleep(250);
        driveBackwardsSlow();
        //Stop at the blue tape
        while (robot.colorSensorDown.blue() < BLUETAPE && opModeIsActive()) {
        }
        stopDriving();
    }

    void scan(int color) {
        int lalpha;
        int ralpha;
        boolean bothYellow = true;

        stopDriving();
        sleep(500);
        telemetry.addData("Start", "scan");
        telemetry.update();

        robot.pattern = RevBlinkinLedDriver.BlinkinPattern.WHITE;
        robot.blinkinLedDriver.setPattern(robot.pattern);

        Orientation targOrient;
        targOrient = robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
        telemetry.addData("heading", targOrient);
        telemetry.update();
        ///ALL STRAFES ARE INVERTED IN AUTONOMOUS
        //STRAFE RIGHT IN THE AUTONOMOUS CODE IS STRAFE LEFT IN REAL LIFE
        //sorry for the all caps, it's just important
        //-Love, Graham
        //sleep(100);
        //We may need to change the alpha values to get consistent readings
        bothYellow = true;
        double lastTime = runtime.milliseconds(); // Added this to create a failsafe
        lastTime = runtime.milliseconds(); //Added this to create a failsafe

        //Added the last part of the while () to create a failsafe
        while ((bothYellow == true) && opModeIsActive() && (runtime.milliseconds()<lastTime + 4000) ) {
            if (color == red) {
                strafeRight(stones,STRAFE_SPEED, targOrient);
            } else if (color == blue) {
                strafeLeft(stones,STRAFE_SPEED, targOrient);
            }
            int skyStoneThresholdRed = 75; //Was 90, changed to 60 on 2/14 at AHS, changed to 150 on 2/17, changed to 75 on 2/18
            if(runtime.milliseconds() > 12000){
                skyStoneThresholdRed = 200;
            }
            int skyStoneThresholdBlue = 140;
            int skyStoneThreshold;
            if(color == red){
                skyStoneThreshold = skyStoneThresholdRed;
            }
            else{
                skyStoneThreshold = skyStoneThresholdBlue;
            }
            ralpha = robot.colorSensorR.alpha();
            lalpha = robot.colorSensorL.alpha();
            telemetry.addData("Left Color Sensor:", lalpha);
            telemetry.addData("Right Color Sensor", ralpha);
            telemetry.update();
            if ((lalpha > skyStoneThreshold) && (ralpha > skyStoneThreshold)) {
                bothYellow = true;
                //both are yellow or air
            }
            //The next two are just extra cases if it isn't reading properly
            if ((lalpha < skyStoneThreshold) && (ralpha > skyStoneThreshold)) {
                bothYellow = true;
                //left is black and right is yellow or air
            }
            if ((lalpha > skyStoneThreshold) && (ralpha < skyStoneThreshold)) {
                bothYellow = true;
                //left is yellow or air and right is black
            }
            // If it's black then bothYellow is false
            if ((lalpha < skyStoneThreshold) && (ralpha < skyStoneThreshold)) {
                sleep(100);
                bothYellow = false;
            }

            telemetry.addData("leftVal = ", "leftVal = " + robot.colorSensorL.alpha());
            telemetry.addData("rightVal = ", "rightVal = " + robot.colorSensorR.alpha());
            //telemetry.addData("bothYellowVal: ", "Yellow State: " + bothYellow);
            telemetry.update();
            Log.i("BACON:", "SAYWHAT?");
        }
        stopDriving();
        telemetry.addData("SICK", "I SEE A SKYSTONE");
        telemetry.update();
        robot.pattern = RevBlinkinLedDriver.BlinkinPattern.WHITE;
        robot.blinkinLedDriver.setPattern(robot.pattern);
    }

    void raiseClaw() {
        robot.liftMotor.setPower(-1);
        while ((robot.liftMotor.getCurrentPosition() > -2250) && opModeIsActive()) {
            telemetry.addData("raiseClaw pos: ", robot.liftMotor.getCurrentPosition());
            telemetry.update();
        }
        robot.liftMotor.setPower(0.0);
    }

    void lowerClaw() {
        robot.liftMotor.setPower(1);
        while ((robot.liftMotor.getCurrentPosition() < 0) && opModeIsActive()) {
            telemetry.addData("lowerClaw pos: ", robot.liftMotor.getCurrentPosition());
            telemetry.update();
        }
        robot.liftMotor.setPower(0.0);
    }

    void positionRobot() {
        driveForwardSlow();
        //TODO: Get a more accurate distance
        while ((robot.frontDistance.getDistance(DistanceUnit.MM) > FRONTDIST) && opModeIsActive()) {
            telemetry.addData("positionRobot  dist(mm): ", robot.frontDistance.getDistance(DistanceUnit.MM));
            telemetry.update();
            Log.i("BACON", Double.toString(robot.frontDistance.getDistance(DistanceUnit.MM)) );
        }
    }

    void grabPrep() {
        driveBackwardsSlow();
        while ((robot.frontDistance.getDistance(DistanceUnit.MM) < 300) && opModeIsActive()) {
            telemetry.addData("driveBackwards  dist(mm): ", robot.backDistance.getDistance(DistanceUnit.MM));
            telemetry.update();
            sleep(10);
        }
        stopDriving();
        lowerClaw();
        sleep(500);
    }

    void grabStone() {
        stopDriving();
        openClaw();
        sleep(1000);
        robot.wheelServoL.setPosition(.75);
        robot.wheelServoR.setPosition(.25);
        driveForwardSlow();
        sleep(1500);
        telemetry.addData("I stop", "I have entered the Grab Phase");
        telemetry.update();
        lowerClaw();
        sleep(500);
        closeClaw();
        sleep(500);

        robot.wheelServoL.setPosition(.5);
        robot.wheelServoR.setPosition(.5);

        driveBackwardsSlow();
        sleep(1500);
        stopDriving();
    }

    void openClaw() {
        robot.clawServo.setPosition(.75);
    }

    void closeClaw() {
        robot.clawServo.setPosition(.2);
    }

    void parkStonesRed() {
        driveForwardSlow(); //Back to the parking tape under the skybridge
        //Stop at the red tape
        robot.pattern = RevBlinkinLedDriver.BlinkinPattern.WHITE;
        robot.blinkinLedDriver.setPattern(robot.pattern);
        while (robot.colorSensorDown.red() < REDTAPE && opModeIsActive()) {
            sleep(10);
            telemetry.addData("parking Red  ", robot.colorSensorDown.red());
            telemetry.addData("parking Alpha  ", robot.colorSensorDown.alpha());
            telemetry.update();
        }
        stopDriving();
        sleep(100);
    }

    void parkStonesBlue() {
        driveForwardSlow();  //Back to the parking tape under the skybridge
        //Stop at the blue tape
        //TODO create a blue version of the red check
        robot.pattern = RevBlinkinLedDriver.BlinkinPattern.WHITE;
        robot.blinkinLedDriver.setPattern(robot.pattern);
        while (robot.colorSensorDown.blue() < BLUETAPE && opModeIsActive()) {
            sleep(10);
            telemetry.addData("parking Blue  ", robot.colorSensorDown.red());
            telemetry.addData("parking Alpha  ", robot.colorSensorDown.alpha());
            telemetry.update();
        }
        stopDriving();
        sleep(100);
    }

    void rotateR(double heading, double speed) {
        Orientation angles;
        angles = robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
        telemetry.addData("current heading", formatAngle(angles.angleUnit, angles.firstAngle));
        telemetry.update();

        rotateRight(speed);
        while ((angles.angleUnit.DEGREES.normalize(angles.firstAngle) > heading) && opModeIsActive()) {
            telemetry.addData("current heading", formatAngle(angles.angleUnit, angles.firstAngle));
            telemetry.update();
            angles = robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
        }
        stopDriving();
    }

    void rotateRight(double speed) {
        // Set power on each wheel
        robot.frontLeftMotor.setPower(speed);
        robot.frontRightMotor.setPower(speed);
        robot.backLeftMotor.setPower(speed);
        robot.backRightMotor.setPower(speed);
    }

    void rotateL(double heading, double speed) {
        Orientation angles;
        angles = robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
        telemetry.addData("current heading", formatAngle(angles.angleUnit, angles.firstAngle));
        telemetry.update();

        rotateLeft(-speed);
        while ((angles.angleUnit.DEGREES.normalize(angles.firstAngle) < heading) && opModeIsActive()) {
            telemetry.addData("current heading", formatAngle(angles.angleUnit, angles.firstAngle));
            telemetry.update();
            angles = robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
        }
        stopDriving();
    }

    void rotateLeft(double speed) {
        // Set power on each wheel
        robot.frontLeftMotor.setPower(speed);
        robot.frontRightMotor.setPower(speed);
        robot.backLeftMotor.setPower(speed);
        robot.backRightMotor.setPower(speed);
    }
    void parkMatBlue(){
        double lastTime = runtime.milliseconds();
        robot.pattern = RevBlinkinLedDriver.BlinkinPattern.WHITE;
        robot.blinkinLedDriver.setPattern(robot.pattern);
        Orientation targOrient;
        targOrient = robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
        lastTime = runtime.milliseconds();
        strafeRight(mat,.3, targOrient);
        while(runtime.milliseconds() < lastTime + 1000){
        }
        stopDriving(); //We may be able to remove this
        lowerClaw();
        while (robot.colorSensorDown.blue() < BLUETAPE && opModeIsActive()) {
            strafeRight(mat,.3, targOrient);
        }
        stopDriving();
    }

    void parkMatRed(){
        double lastTime = runtime.milliseconds();
        robot.pattern = RevBlinkinLedDriver.BlinkinPattern.WHITE;
        robot.blinkinLedDriver.setPattern(robot.pattern);
        Orientation targOrient;
        targOrient = robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
        lastTime = runtime.milliseconds();
        strafeLeft(mat,.3, targOrient);
        while(runtime.milliseconds() < lastTime + 1000){
        }
        stopDriving(); //We may be able to remove this
        lowerClaw();
        while (robot.colorSensorDown.red() < REDTAPE && opModeIsActive()) {
            strafeLeft(mat,.3, targOrient);
            telemetry.addData("Red  ", robot.colorSensorDown.red());
            telemetry.update();
        }
        stopDriving();
    }

    String formatAngle(AngleUnit angleUnit, double angle) {
        return formatDegrees(AngleUnit.DEGREES.fromUnit(angleUnit, angle));
    }

    String formatDegrees(double degrees) {
        return String.format(Locale.getDefault(), "%.1f", AngleUnit.DEGREES.normalize(degrees));
    }
}
/*
Going to 1000 lines



=================================
=================================
=================================



 ____          _____ ____  _   _
|  _ \   /\   / ____/ __ \| \ | |
| |_) | /  \ | |   | |  | |  \| |
|  _ < / /\ \| |   | |  | | . ` |
| |_) / ____ \ |___| |__| | |\  |
|____/_/    \_\_____\____/|_| \_|




=================================
=================================
=================================




                                  _______
                           _,,ad8888888888bba,_
                        ,ad88888I888888888888888ba,
                      ,88888888I88888888888888888888a,
                    ,d888888888I8888888888888888888888b,
                   d88888PP"""" ""YY88888888888888888888b,
                 ,d88"'__,,--------,,,,.;ZZZY8888888888888,
                ,8IIl'"                ;;l"ZZZIII8888888888,
               ,I88l;'                  ;lZZZZZ888III8888888,
             ,II88Zl;.                  ;llZZZZZ888888I888888,
            ,II888Zl;.                .;;;;;lllZZZ888888I8888b
           ,II8888Z;;                 `;;;;;''llZZ8888888I8888,
           II88888Z;'                        .;lZZZ8888888I888b
           II88888Z; _,aaa,      .,aaaaa,__.l;llZZZ88888888I888
           II88888IZZZZZZZZZ,  .ZZZZZZZZZZZZZZ;llZZ88888888I888,
           II88888IZZ<'(@@>Z|  |ZZZ<'(@@>ZZZZ;;llZZ888888888I88I
          ,II88888;   `""" ;|  |ZZ; `"""     ;;llZ8888888888I888
          II888888l            `;;          .;llZZ8888888888I888,
         ,II888888Z;           ;;;        .;;llZZZ8888888888I888I
         III888888Zl;    ..,   `;;       ,;;lllZZZ88888888888I888
         II88888888Z;;...;(_    _)      ,;;;llZZZZ88888888888I888,
         II88888888Zl;;;;;' `--'Z;.   .,;;;;llZZZZ88888888888I888b
         ]I888888888Z;;;;'   ";llllll;..;;;lllZZZZ88888888888I8888,
         II888888888Zl.;;"Y88bd888P";;,..;lllZZZZZ88888888888I8888I
         II8888888888Zl;.; `"PPP";;;,..;lllZZZZZZZ88888888888I88888
         II888888888888Zl;;. `;;;l;;;;lllZZZZZZZZW88888888888I88888
         `II8888888888888Zl;.    ,;;lllZZZZZZZZWMZ88888888888I88888
          II8888888888888888ZbaalllZZZZZZZZZWWMZZZ8888888888I888888,
          `II88888888888888888b"WWZZZZZWWWMMZZZZZZI888888888I888888b
           `II88888888888888888;ZZMMMMMMZZZZZZZZllI888888888I8888888
            `II8888888888888888 `;lZZZZZZZZZZZlllll888888888I8888888,
             II8888888888888888, `;lllZZZZllllll;;.Y88888888I8888888b,
            ,II8888888888888888b   .;;lllllll;;;.;..88888888I88888888b,
            II888888888888888PZI;.  .`;;;.;;;..; ...88888888I8888888888,
            II888888888888PZ;;';;.   ;. .;.  .;. .. Y8888888I88888888888b,
           ,II888888888PZ;;'                        `8888888I8888888888888b,
           II888888888'                              888888I8888888888888888b
          ,II888888888                              ,888888I88888888888888888
         ,d88888888888                              d888888I8888888888ZZZZZZZ
      ,ad888888888888I                              8888888I8888ZZZZZZZZZZZZZ
    ,d888888888888888'                              888888IZZZZZZZZZZZZZZZZZZ
  ,d888888888888P'8P'                               Y888ZZZZZZZZZZZZZZZZZZZZZ
 ,8888888888888,  "                                 ,ZZZZZZZZZZZZZZZZZZZZZZZZ
d888888888888888,                                ,ZZZZZZZZZZZZZZZZZZZZZZZZZZZ
888888888888888888a,      _                    ,ZZZZZZZZZZZZZZZZZZZZ888888888
888888888888888888888ba,_d'                  ,ZZZZZZZZZZZZZZZZZ88888888888888
8888888888888888888888888888bbbaaa,,,______,ZZZZZZZZZZZZZZZ888888888888888888
88888888888888888888888888888888888888888ZZZZZZZZZZZZZZZ888888888888888888888
8888888888888888888888888888888888888888ZZZZZZZZZZZZZZ88888888888888888888888
888888888888888888888888888888888888888ZZZZZZZZZZZZZZ888888888888888888888888
8888888888888888888888888888888888888ZZZZZZZZZZZZZZ88888888888888888888888888
88888888888888888888888888888888888ZZZZZZZZZZZZZZ8888888888888888888888888888
8888888888888888888888888888888888ZZZZZZZZZZZZZZ88888888888888888  BACON!  88
88888888888888888888888888888888ZZZZZZZZZZZZZZ8888888888888888888   7080   88
8888888888888888888888888888888ZZZZZZZZZZZZZZ88888888888888888888888888888888


made it
 */









//okay team, today is going to be a great day. We are going to work so hard and we are going to enjoy the moment.
//Tony Bennett, the UVA Men's Basketball Coach, once said that team 7080 is the best team because
//we have a lot of snacks with us at all times.