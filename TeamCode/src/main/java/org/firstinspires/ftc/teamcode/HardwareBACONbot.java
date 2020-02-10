/* Copyright (c) 2017 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.rev.RevBlinkinLedDriver;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.hardware.rev.Rev2mDistanceSensor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

/**
 * This is NOT an opmode.
 *
 * This class defines all the specific hardware for a the BACONbot robot.
 *
 * This hardware class assumes the following device names have been configured on the robot:
 *
 * Motor channel:   Front Left drive motor:       "FL"
 * Motor channel:  Front Right  drive motor:      "FR"
 * Motor channel:   Back Left drive motor:        "BL"
 * Motor channel:  Back Right  drive motor:       "BR"
 *
 */
public class HardwareBACONbot

{
    /* Public OpMode members. */
    public DcMotor  frontLeftMotor   = null;
    public DcMotor  frontRightMotor  = null;
    public DcMotor  backLeftMotor    = null;
    public DcMotor  backRightMotor   = null;

    public DcMotor liftMotor = null;

    public DistanceSensor backDistance = null;
    public DistanceSensor frontDistance = null;

    public ColorSensor colorSensorL = null; //Adding in a color sensor for the blocks
    public DistanceSensor sensorDistanceL = null;
    public DistanceSensor sensorDistanceR = null;
    public ColorSensor colorSensorR = null; //Adding in a color sensor for the blocks
    public ColorSensor colorSensorDown = null;

    public Servo    clawServo = null;
    public Servo    matServoL = null;
    public Servo    matServoR = null;
    public Servo    capstoneServo = null;
    public Servo  wheelServoL = null;
    public Servo  wheelServoR = null;
    public RevBlinkinLedDriver blinkinLedDriver;
    public RevBlinkinLedDriver.BlinkinPattern pattern;



    public BNO055IMU imu;



    //public Servo matServo = null;

    /* local OpMode members. */
    private HardwareMap hwMap =  null;
    private ElapsedTime period  = new ElapsedTime();

    /* Constructor */
    public HardwareBACONbot(){
    }

    /* Initialize standard Hardware interfaces */
    public void init(HardwareMap ahwMap) {
        // Save reference to Hardware map
        hwMap = ahwMap;

        // Define and Initialize Motors
        // Define and Initialize Motors
        frontLeftMotor  = hwMap.dcMotor.get("FL"); // H1 0 - motor port
        frontRightMotor = hwMap.dcMotor.get("FR"); // H1 1
        backLeftMotor   = hwMap.dcMotor.get("BL"); // H1 2
        backRightMotor  = hwMap.dcMotor.get("BR"); // H1 3

        liftMotor = hwMap.dcMotor.get("LM"); // Hub 2 Port 0

        backDistance = hwMap.get(DistanceSensor.class, "bsr"); //hub2 port 1
        frontDistance = hwMap.get(DistanceSensor.class, "fsr"); //hub2 port 2


        colorSensorL = hwMap.get(ColorSensor.class, "colL");  //hub1 port 1
        sensorDistanceL = hwMap.get(DistanceSensor.class, "colL");
        sensorDistanceR = hwMap.get(DistanceSensor.class, "colR");
        colorSensorR = hwMap.get(ColorSensor.class, "colR"); //hub1 port 2
        colorSensorDown = hwMap.get(ColorSensor.class, "colD"); //hub1 port 3
        clawServo = hwMap.servo.get("claw"); //H1P5
        capstoneServo = hwMap.servo.get("capstone"); //H1P3
        wheelServoL = hwMap.servo.get("wheelL");
        wheelServoR = hwMap.servo.get("wheelR");

        matServoL = hwMap.servo.get("matL");
        matServoR = hwMap.servo.get("matR");

        blinkinLedDriver = hwMap.get(RevBlinkinLedDriver.class, "blinkin");



       // matServo = hwMap.servo.get("MS");

        // BACONbot uses AndyMark NeverRest Motors
        // This code assumes that the motors turns counterclockwise,
        //     looking from the back of the motor down the shaft,
        //     when positive power is applied

        //  *****if the above assumption is incorrect uncomment these lines
        frontLeftMotor.setDirection(DcMotor.Direction.REVERSE);
        frontRightMotor.setDirection(DcMotor.Direction.REVERSE);
        backLeftMotor.setDirection(DcMotor.Direction.REVERSE);
        backRightMotor.setDirection(DcMotor.Direction.REVERSE);

        // Set all motors to zero power
        frontLeftMotor.setPower(0);
        frontRightMotor.setPower(0);
        backLeftMotor.setPower(0);
        backRightMotor.setPower(0);
        //clawServo.setPosition(0);

        liftMotor.setPower(0);
      //  matServo.setPosition(0);

        // Set all motors to run without encoders.
        // May want to use RUN_USING_ENCODERS if encoders are installed.
        //
        // Holonomic drive will make using encoders challenging as straffing is
        //    a design expectation
        frontLeftMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        frontRightMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        backLeftMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        backRightMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);


        liftMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        liftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        frontLeftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        liftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Set up the parameters with which we will use our IMU. Note that integration
        // algorithm here just reports accelerations to the logcat log; it doesn't actually
        // provide positional information.
        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit           = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit           = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.calibrationDataFile = "BNO055IMUCalibration.json"; // see the calibration sample opmode
        parameters.loggingEnabled      = true;
        parameters.loggingTag          = "IMU";


        // Retrieve and initialize the IMU. We expect the IMU to be attached to an I2C port
        // on a Core Device Interface Module, configured to be a sensor of type "AdaFruit IMU",
        // and named "imu".
        imu = hwMap.get(BNO055IMU.class, "imu");
        imu.initialize(parameters);



    }










 }

