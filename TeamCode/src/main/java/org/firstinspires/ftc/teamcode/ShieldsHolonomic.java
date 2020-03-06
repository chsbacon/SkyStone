/*
Copyright (c) 2016 Robert Atkinson

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of Robert Atkinson nor the names of his contributors may be used to
endorse or promote products derived from this software without specific prior
written permission.

NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESSFOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package org.firstinspires.ftc.teamcode;

import android.graphics.Color;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.hardware.rev.RevBlinkinLedDriver;

// This OpMode is just Shields messing around with holonomic drive and functions
// The OpMode utilizes the BACONbot hardware definition defined in HardwareBACONbot

@TeleOp(name = "Shields: Holonomic", group = "Shields")

public class ShieldsHolonomic extends LinearOpMode {

    /* Declare OpMode members. */
    Hardware_Shields_Holonomic robot = new Hardware_Shields_Holonomic();   // Use a BACONbot's hardware
    ElapsedTime stateTime = new ElapsedTime();

    @Override
    //@Disable
    public void runOpMode() throws InterruptedException {

        double x;
        double y;
        double r;
        double rot = 45;
        double frontLeft;
        double frontRight;
        double backLeft;
        double backRight;
        double max;
        int botState = 0;
        boolean shooting = false;

        /* Initialize the hardware variables.
         * The init() method of the hardware class does all the work here
         */
        robot.init(hardwareMap);

        // Send telemetry message to signify robot waiting;
        telemetry.addData("Say", "Greetings, human.");    //
        telemetry.update();

        // Wait for the game to start (driver presses PLAY)
        waitForStart();

        // this is a linear op mode, but everything happens in this while loop
        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {

            // Get x and y values from left joystick. (With the Logitech 310 the joystick y goes negative when pushed forwards, so negate it)
            // Left joystick sets heading
            // Right joystick sets rotation
            y = -gamepad1.left_stick_x*Math.cos(Math.toRadians(rot))-gamepad1.left_stick_y*Math.sin(Math.toRadians(rot));
            x = gamepad1.left_stick_x*Math.cos(Math.toRadians(rot))-gamepad1.left_stick_y*Math.sin(Math.toRadians(rot));
            r = gamepad1.right_stick_x;
            // do not let rotation dominate movement
            r = r / 4;

            // calculate the power for each wheel (math from here: https://www.vexforum.com/index.php/12370-holonomic-drives-2-0-a-video-tutorial-by-cody/0)
            frontLeft = -y - x + r;
            frontRight = +y - x + r;
            backLeft = -y + x + r;
            backRight = +y + x + r;

            // Normalize the values so none exceeds +/- 1.0
            max = Math.max(Math.max(Math.abs(frontLeft), Math.abs(frontRight)), Math.max(Math.abs(frontRight), Math.abs(frontRight)));
            if (max > 1.0) {
                frontLeft = frontLeft / max;
                frontRight = frontRight / max;
                backLeft = backLeft / max;
                backRight = backRight / max;
            }

            // Set power on each wheel
            robot.frontLeftMotor.setPower(frontLeft);
            robot.frontRightMotor.setPower(frontRight);
            robot.backLeftMotor.setPower(backLeft);
            robot.backRightMotor.setPower(backRight);

            telemetry.update();

            while (!gamepad1.x && !gamepad1.b && !gamepad1.y && !gamepad1.a) {
            }

            if(gamepad1.x){
                robot.pattern = RevBlinkinLedDriver.BlinkinPattern.BLUE;
                robot.blinkinLedDriver.setPattern(robot.pattern);
                telemetry.addData("Blue", " ");
                telemetry.update();
            }
            if(gamepad1.y){
                robot.pattern = RevBlinkinLedDriver.BlinkinPattern.YELLOW;
                robot.blinkinLedDriver.setPattern(robot.pattern);
            }
            if(gamepad1.a){
                robot.pattern = RevBlinkinLedDriver.BlinkinPattern.GREEN;
                robot.blinkinLedDriver.setPattern(robot.pattern);
            }
            if(gamepad1.b){
                robot.pattern = RevBlinkinLedDriver.BlinkinPattern.RED;
                robot.blinkinLedDriver.setPattern(robot.pattern);
            }
            if(gamepad1.b && gamepad1.x){
                robot.pattern = RevBlinkinLedDriver.BlinkinPattern.VIOLET;
                robot.blinkinLedDriver.setPattern(robot.pattern);
            }
            if(gamepad1.b && gamepad1.y){
                robot.pattern = RevBlinkinLedDriver.BlinkinPattern.ORANGE;
                robot.blinkinLedDriver.setPattern(robot.pattern);
            }
            if(gamepad1.x && gamepad1.y){
                robot.pattern = RevBlinkinLedDriver.BlinkinPattern.GREEN;
                robot.blinkinLedDriver.setPattern(robot.pattern);
            }
            if(gamepad1.a && gamepad1.b && gamepad1.x && gamepad1.y){
                robot.pattern = RevBlinkinLedDriver.BlinkinPattern.RAINBOW_RAINBOW_PALETTE;
                robot.blinkinLedDriver.setPattern(robot.pattern);
            }


            /*
            if (gamepad1.b)
                botState = 0;

            switch (botState) {
                case 0:
                    stateTime.reset();
                    botState++;
                    break;
                case 1:
                    goForward();
                    if (stateTime.time() >= 2.0)
                    {
                        botState++;
                    }
                    break;
                case 2:
                    wheelsOff();
                    break;
            }

            switch (botState) {
                case 0:
                    if (gamepad1.y)
                        botState++;
                    break;
                case 1:
                    robot.shooterMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                    idle();
                    robot.shooterMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                    telemetry.addData("Path0",  "my position %7d",robot.shooterMotor.getCurrentPosition());
                    telemetry.update();
                    robot.shooterMotor.setTargetPosition(1440);
                    robot.shooterMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                    robot.shooterMotor.setPower(.5);
                    botState++;
                    break;
                case 2:
                    if (!robot.shooterMotor.isBusy()) {
                        botState = 0;
                        robot.shooterMotor.setPower(0);
                    }
                    else {
                        telemetry.addData("Path1",  "my position %7d",robot.shooterMotor.getCurrentPosition());
                        telemetry.update();
                    }
                    break;
            }
            */
            // Show wheel power to driver
            // telemetry.addData("front left", "%.2f", frontLeft);
            // telemetry.addData("front right", "%.2f", frontRight);
            // telemetry.addData("back left", "%.2f", backLeft);
            // telemetry.addData("back right", "%.2f", backRight);
            // telemetry.addData("back right", "%.2f", backRight);
            // telemetry.update();

            idle();
        }
    }

    public void goForward() {
        robot.frontLeftMotor.setPower(-.5);
        robot.frontRightMotor.setPower(.5);
        robot.backLeftMotor.setPower(-.5);
        robot.backRightMotor.setPower(.5);
    }
    public void wheelsOff() {
        robot.frontLeftMotor.setPower(0);
        robot.frontRightMotor.setPower(0);
        robot.backLeftMotor.setPower(0);
        robot.backRightMotor.setPower(0);
    }
}
