// FTC Team 7080 BACON
// Autonomous code for red & blue side, Stone & Mat side

package org.firstinspires.ftc.teamcode;

import android.app.Activity;
import android.graphics.Color;
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

import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.teamcode.HardwareBACONbot;

import java.util.Locale;

import static org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer.CameraDirection.BACK;
//import org.firstinspires.ftc.teamcode.Teleops.HardwareMap;

//TODO: Important things to do in the mat position auto code
//   - Check where driveForward() and strafeLeft/Right() are in relation to while loop
//   - Compare to MatPositionTesting branch (that branch is done now)
//   - Check correct version of all strafes (strafe functions are now correct)
//   - Figure out the claw raise/lower problem with timing in the park function
//    (Mr. Dierolf said we can just do a sleep)(Wait until motor/encoder situation is figured out)
//   - figure out what in the world is going on with the down button on the controller
//     (Why it's not in the mechanum code anymore

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
    int mat = 1;
    int stones = 2;
    int parallel = 1;
    int perpendicular = 2;
    int matPosition;
    int FRONTDIST = 160;


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
        telemetry.addData("task ", task);
        telemetry.update();

        //Choosing the mat orientation
        telemetry.addData("Press left bumper for parallel mat position, right bumper for perpendicular mat position", "");
        telemetry.update();
        while (!gamepad1.left_bumper && !gamepad1.right_bumper) {
        }
        if (gamepad1.left_bumper) {
            matPosition = parallel;
        }
        if (gamepad1.right_bumper) {
            matPosition=perpendicular;
        }
        telemetry.addData("Mat Position ", matPosition);
        telemetry.update();

        Rev2mDistanceSensor sensorTimeOfFlight = (Rev2mDistanceSensor) robot.backDistance;

        //mat servos up
        robot.matServoL.setPosition(freePos);
        robot.matServoR.setPosition(grabPos);

        waitForStart();
        runtime.reset();

        // run until the end of the match (driver presses STOP)

        //Stones --------------------------------------------------------------------------++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        //First troubleshooting steps for this section would be to check the direction of the strafes in scan and grab
        if ((task == stones) && (teamcolor == red)) {
            robot.pattern = RevBlinkinLedDriver.BlinkinPattern.WHITE;
            robot.blinkinLedDriver.setPattern(robot.pattern);
            //This lifts the claw one level so that it won't be in the way of the blocks while scanning
            //raiseClaw();
            //This gets the robot in the proper place to sense the Skystones
            //positionRobot();
            Orientation targOrient = robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
            headingPowerTime(1, .3, 4, targOrient);
            //This performs the scanning operation until we find a Skystone
            //scan(red); //2 means red
            //Setting it up to go up and grab the Skystone
            grabPrep();
            //Pick up the Skystone
            grabStone();
            //rotate to face mat side
            //rotateR(-80.0, 0.3); //heading was at 85
            //Park on the tape
            //parkStonesRed();
            //go drop the stone in the build zone and return to the parking line
           // outAndBackRed();
            stopDriving();

        }
        if ((task == stones) && (teamcolor == blue)) {
            robot.pattern = RevBlinkinLedDriver.BlinkinPattern.WHITE;
            robot.blinkinLedDriver.setPattern(robot.pattern);
            //This lifts the claw one level so that it won't be in the way of the blocks while scanning
            raiseClaw();
            //This gets the robot in the proper place to sense the Skystones
            positionRobot();
            //This performs the scanning operation until we find a Skystone
            scan(blue); //1 means blue
            //Setting it up to go up and grab the Skystone
            grabPrep();
            //Pick up the Skystone
            grabStone();
            //rotate to face mat side
            rotateL(80.0, 0.3); //heading was at 85
            //Park on the tape
            parkStonesBlue();
            //go drop the stone in the build zone and return to the parking line
            outAndBackBlue();
            stopDriving();

        }

        //----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        //TODO: BLUE TEAM MAT
        //----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        //BLUE TEAM MAT
        if ((task == mat) && (teamcolor == blue) && (matPosition==parallel)) {
            //Orientation targOrient;
            //targOrient = robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

            initializeWhiteLight();
            positionRobotMatBlue();
            grabMat();

            driveBackwardsSlow();

            while ((robot.backDistance.getDistance(DistanceUnit.MM) > 100) && opModeIsActive())
            {
                telemetry.addData("backing up", "Back Distance: " + robot.backDistance.getDistance(DistanceUnit.MM));
                telemetry.update();
            }
            stopDriving();

            releaseMat();

            //targOrient = robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

            parkMatBlue();

            driveBackwardsSlow();
            sleep(500);
            stopDriving();
        }
        if ((task == mat) && (teamcolor == blue)&&(matPosition==perpendicular)) {
            //Orientation targOrient;
            //targOrient = robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

            initializeWhiteLight();
            positionRobotMatBlue();
            grabMat();
            driveBackwardsSlow();

            while ((robot.backDistance.getDistance(DistanceUnit.MM) > 100) && opModeIsActive())
            {
                telemetry.addData("backing up", "Back Distance: " + robot.backDistance.getDistance(DistanceUnit.MM));
                telemetry.update();
            }
            stopDriving();
            matRotateR();
            stopDriving();
            releaseMat();

            //targOrient = robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
            //rotateL(90, .3);
            //strafeRight();
            parkMatBlue();

            driveBackwardsSlow();
            sleep(500);
            stopDriving();
        }
        // -----------------------------------------------------------------------------------------------------------------------


        if ((task == mat) && (teamcolor == red)&&(matPosition==perpendicular)) {
            initializeWhiteLight();
            positionRobotMatRed();
            grabMat();


            driveBackwardsSlow();

            while ((robot.backDistance.getDistance(DistanceUnit.MM) > 600) && opModeIsActive()) //drivetomat
            {
                telemetry.addData("backing up", "Back Distance: " + robot.backDistance.getDistance(DistanceUnit.MM));
                telemetry.update();
            }
            stopDriving();
            matRotateR();
            releaseMat();


            //Actually left towards the skybridge
            //Senses the BLUE tape under the skybridge and tells the robot to stop
            //targOrient = robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);


            parkMatRed();

            driveBackwardsSlow();
            sleep(500);
            stopDriving();
        }
        if ((task == mat) && (teamcolor == red)&&(matPosition==parallel)) {
            initializeWhiteLight();
            positionRobotMatRed();
            grabMat();


            driveBackwardsSlow();

            while ((robot.backDistance.getDistance(DistanceUnit.MM) > 100) && opModeIsActive()) //drivetomat
            {
                telemetry.addData("backing up", "Back Distance: " + robot.backDistance.getDistance(DistanceUnit.MM));
                telemetry.update();
            }
            stopDriving();

            releaseMat();

            //rotateL(-10.0, 0.3);

            //Actually left towards the skybridge
            //Senses the BLUE tape under the skybridge and tells the robot to stop
            //targOrient = robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);


            parkMatRed();

            driveBackwardsSlow();
            sleep(500);
            stopDriving();
        }

        //TODO What does this part do? It isn't in the same spot as the other things with sensing skystones
        while (opModeIsActive()) {
            //telemetry.addData("Alpha", robot.colorSensorL.alpha());
            //telemetry.addData("Red  ", robot.colorSensorDown.red());
            if ((robot.colorSensorL.alpha() < 40) && (robot.colorSensorR.alpha() < 40))
                telemetry.addData("Skystone", 1);
            else
                telemetry.addData("Yellow", 0);
            telemetry.update();
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

        /*
        //if the absolute value of r is less than
        //.07, the motors won't do anything, so if
        //it is less than .07, make it .07
        if ((r < .07) && (r > 0)) {
            r = .07;
        } else if ((r > -.07) && (r < 0)) {
            r = -.07;
        }
        */

/*
        telemetry.addData("pwr:>", pwr);
        telemetry.addData("error:>", r);
        telemetry.addData("r:>", r);
        telemetry.update();
*/
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

        //if the absolute value of r is less than
        //.07, the motors won't do anything, so if
        //it is less than .07, make it .07
        /*
        if ((r < .07) && (r > 0)) {
            r = .07;
        } else if ((r > -.07) && (r < 0)) {
            r = -.07;
        }
        */
/*
        telemetry.addData("pwr:>", pwr);
        telemetry.addData("error:>", r);
        telemetry.addData("r:>", r);
        telemetry.update();
*/
        double d; // Front distance correction
        d = -(FRONTDIST - 45 - robot.frontDistance.getDistance(DistanceUnit.MM)) / 200;
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

    void headingPowerTime(double heading, double pwr, double t, Orientation targ) {
        // this is a new branch :)

        /* pwr(sin(heading+45))+r [M4]------[M1] pwr(cos(heading+45))-r
                                    |        |
                                    |        |
           pwr(cos(heading+45))+r [M3]------[M2] pwr(sin(heading+45))-r */

        /*
        double r = 0;
        double M1 = pwr*Math.cos(Math.toRadians(heading)+Math.PI/4)-r;
        double M2 = pwr*Math.sin(Math.toRadians(heading)+Math.PI/4)-r;
        double M3 = pwr*Math.cos(Math.toRadians(heading)+Math.PI/4)+r;
        double M4 = pwr*Math.sin(Math.toRadians(heading)+Math.PI/4)+r;
        */

        /*
        double x = pwr*Math.cos(Math.toRadians(heading));
        double y = pwr*Math.sin(Math.toRadians(heading));
         */

        int STONEX = 20;
        int STONEY = 48;

        double x = STONEX/Math.sqrt(STONEX^2 +  STONEY^2);
        double y = STONEY/Math.sqrt(STONEX^2 +  STONEY^2);

        // trueHead is the current orientation.  It shouldn't change!
        Orientation trueHead;
        trueHead = robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
        double trueAng = trueHead.angleUnit.DEGREES.normalize(trueHead.firstAngle);

        double startTime = runtime.milliseconds();
        double lastTime =  startTime;
        double now;
        while (runtime.milliseconds() < (startTime + t*1000) && opModeIsActive()) {
            now = runtime.milliseconds();
            if(now > lastTime + 50) {
                // fix any heading drift
                Orientation currOrient;
                currOrient = robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
                double currAng = currOrient.angleUnit.DEGREES.normalize(currOrient.firstAngle);
                double error = trueAng - currAng;
                telemetry.addData("error:", error);
                double r = error / 180 * pwr * 0.5;

                if ((r < .07) && (r > 0)) {
                    r = .07;
                } else if ((r > -.07) && (r < 0)) {
                    r = -.07;
                }

                // mecanum math
                double M2 = +y - x + r;
                double M1 = +y + x + r;
                double M3 = -y - x + r;
                double M4 = -y + x + r;

                double max;
                max = Math.max(Math.max(Math.abs(M2), Math.abs(M1)), Math.max(Math.abs(M3), Math.abs(M4)));
                if (max > 1.0) {
                    M2 = M2 / max;
                    M1 = M1 / max;
                    M3 = M3 / max;
                    M4 = M4 / max;
                }

                robot.frontLeftMotor.setPower(M4);
                robot.backLeftMotor.setPower(M3);
                robot.backRightMotor.setPower(M2);
                robot.frontRightMotor.setPower(M1);

                telemetry.addData("M1:", M1);
                telemetry.addData("M2:", M2);
                telemetry.addData("M3:", M3);
                telemetry.addData("M4:", M4);
                telemetry.update();
                lastTime = now;
            }
        }
    }


    void outAndBackRed() {
        driveForwardSlow(); //Out from the parking tape under the skybridge
        sleep(1000);
        stopDriving();
        raiseClaw();

        driveForwardSlow();
       sleep(2000);

       /*
        while ((robot.frontDistance.getDistance(DistanceUnit.MM) > 150) && opModeIsActive()){
            driveForwardSlow();
        }
        */

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
       /* sleep(500);
        stopDriving();
        lowerClaw();
        sleep(1000);
        driveBackwardsSlow();*/

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

        sleep(2000);
        /*
        while ((robot.frontDistance.getDistance(DistanceUnit.MM) > 150) && opModeIsActive()){
            driveForwardSlow();
        }
        */

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
        telemetry.addData("Start", "scan");
        stopDriving();
        sleep(500);
        telemetry.update();
        int lalpha;
        int ralpha;
        boolean bothYellow = true;
        int relativeLayoutId = hardwareMap.appContext.getResources().getIdentifier("RelativeLayout", "id", hardwareMap.appContext.getPackageName());
        final View relativeLayout = ((Activity) hardwareMap.appContext).findViewById(relativeLayoutId);

        robot.pattern = RevBlinkinLedDriver.BlinkinPattern.WHITE;
        robot.blinkinLedDriver.setPattern(robot.pattern);

        Orientation targOrient;
        targOrient = robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
        telemetry.addData("heading", "heading: " + targOrient);
        telemetry.update();
        ///ALL STRAFES ARE INVERTED IN AUTONOMOUS
        //STRAFE RIGHT IN THE AUTONOMOUS CODE IS STRAFE LEFT IN REAL LIFE
        //sorry for the all caps, it's just important
        //-Love, Graham
        //sleep(100);
        //We may need to change the alpha values to get consistent readings
        while ((bothYellow == true) && opModeIsActive()) {

            if (color == red) {
                strafeRight(stones,STRAFE_SPEED, targOrient);
            } else if (color == blue) {
                strafeLeft(stones,STRAFE_SPEED, targOrient);
            }

            int skyStoneThresholdRed = 90;
            if(runtime.milliseconds() > 12000){
                skyStoneThresholdRed = 200;
            }
            int skyStoneThresholdBlue = skyStoneThresholdRed;
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
            /* begin correct heading part 1
            Orientation heading;
            heading = robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
            double cHeading;
            cHeading = heading.angleUnit.DEGREES.normalize(heading.firstAngle);
            end correct heading part 1  */
            // If it's black then bothYellow is false
            if ((lalpha < skyStoneThreshold) && (ralpha < skyStoneThreshold)) {
                sleep(100); //keep strafing
                /*  part 2 -- tried to correct heading
                if (cHeading > 0.0) {//line up
                    rotateR(0.0, .3);
                } else if (cHeading < 0.0) {
                    rotateL(0.0, .3);
                }
                end part 2 tried to correct heading */
                bothYellow = false;
            }
            /*
            telemetry.addData("leftVal = ", "leftVal = " + robot.colorSensorL.alpha());
            telemetry.addData("rightVal = ", "rightVal = " + robot.colorSensorR.alpha());
            //telemetry.addData("bothYellowVal: ", "Yellow State: " + bothYellow);
            telemetry.update();
            */
        }


        //  telemetry.addData("SICK", "I SEE A SKYSTONE");
        telemetry.update();
        relativeLayout.post(new Runnable() {
            public void run() {
                robot.pattern = RevBlinkinLedDriver.BlinkinPattern.WHITE;  //used to be green but want white for blue tape
                robot.blinkinLedDriver.setPattern(robot.pattern);
            }
        });

    }


    void raiseClaw() {
        robot.liftMotor.setPower(-1);
        while ((robot.liftMotor.getCurrentPosition() > -1500) && opModeIsActive()) {
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
        }

    }

    void grabPrep() {
        driveBackwardsSlow();
        while ((robot.frontDistance.getDistance(DistanceUnit.MM) < 200) && opModeIsActive()) {
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
        driveForwardSlow();
        /*
        while ((robot.backDistance.getDistance(DistanceUnit.MM) < 700) && opModeIsActive()) {
            sleep(10);
        }
        */
        sleep(1500);
        telemetry.addData("I stop", "I have entered the Grab Phase");
        telemetry.update();
        lowerClaw();
        sleep(500);
        closeClaw();
        sleep(500);

        driveBackwardsSlow();
        /*
        while ((robot.backDistance.getDistance(DistanceUnit.MM) > 700) && opModeIsActive()) {
            sleep(10);
        }
        */
        sleep(1500);
        stopDriving();

    }

    void openClaw() {
        robot.clawServo.setPosition(1);
    }

    void closeClaw() {
        robot.clawServo.setPosition(0);
    }


    void parkStonesRed() {
        driveForwardSlow(); //Back to the parking tape under the skybridge
        //Stop at the red tape
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
    void initializeWhiteLight(){
        robot.pattern = RevBlinkinLedDriver.BlinkinPattern.WHITE;
        robot.blinkinLedDriver.setPattern(robot.pattern);
    }

    void positionRobotMatBlue(){
        double meetDistance = 860; //Distance from wall to the Blocks/Mat (CM From Wall (BackSensor))
        double lastTime = runtime.milliseconds();
        //raiseClaw();
        while ((robot.backDistance.getDistance(DistanceUnit.MM) < meetDistance) && opModeIsActive()) //drive to mat
        {
            driveForwardSlow();
        }
        stopDriving();
        lastTime = runtime.milliseconds();
        //this actually makes it go left toward the center of the mat
        Orientation targOrient;
        targOrient = robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
        while (runtime.milliseconds() < lastTime + 1000) {
            strafeLeft(mat, .3, targOrient);
        }
        stopDriving();
        driveForwardSlow();
        sleep(250);
        stopDriving();
    }

    void positionRobotMatRed(){
        double meetDistance = 860; //Distance from wall to the Blocks/Mat (CM From Wall (BackSensor))
        double lastTime = runtime.milliseconds();
        //raiseClaw();

        while ((robot.backDistance.getDistance(DistanceUnit.MM) < meetDistance) && opModeIsActive()) //drive to mat
        {
            driveForwardSlow();
        }
        stopDriving();
        lastTime = runtime.milliseconds();
        //this actually makes it go right toward the center of the mat
        Orientation targOrient;
        targOrient = robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

        while (runtime.milliseconds() < lastTime + 1000) {
            strafeRight(mat, .3, targOrient);
        }
        stopDriving();
        driveForwardSlow();
        sleep(250);
        stopDriving();
    }

    void grabMat(){
        float grabPos = 0;  //change these later (written 12-3-19)
        float freePos = 1;  //change these later  (written 12-3-19)
        //servos down
        robot.matServoL.setPosition(grabPos);
        robot.matServoR.setPosition(freePos);
        sleep(1000); //We can edit this delay based on it we need more time or not
    }

    void releaseMat(){
        float grabPos = 0;  //change these later (written 12-3-19)
        float freePos = 1;  //change these later  (written 12-3-19)
        //mat servos up
        robot.matServoL.setPosition(freePos);
        robot.matServoR.setPosition(grabPos);
        sleep(1000); //this makes sure we don't knock the mat when we begin to go towards parking
    }

    void parkMatBlue(){
        double lastTime = runtime.milliseconds();
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
    void matRotateR(){
        robot.frontLeftMotor.setPower(0);
        robot.frontRightMotor.setPower(-0.3);
        robot.backLeftMotor.setPower(0);
        robot.backRightMotor.setPower(-0.3);
    }
    void matRotateL(){
        robot.frontLeftMotor.setPower(-0.3);
        robot.frontRightMotor.setPower(0);
        robot.backLeftMotor.setPower(-0.3);
        robot.backRightMotor.setPower(0);
    }

    //----------------------------------------------------------------------------------------------
    // Formatting
    //----------------------------------------------------------------------------------------------

    String formatAngle(AngleUnit angleUnit, double angle) {
        return formatDegrees(AngleUnit.DEGREES.fromUnit(angleUnit, angle));
    }

    String formatDegrees(double degrees) {
        return String.format(Locale.getDefault(), "%.1f", AngleUnit.DEGREES.normalize(degrees));
    }

}


    //Lets Go Team! Hi Mom! - Graham