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

import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

import java.util.Locale;
//import org.firstinspires.ftc.teamcode.Teleops.HardwareMap;


@Autonomous(name = "Shields Mecanum Auto", group = "Opmode")
@Disabled

public class ShieldsMecanumAuto extends LinearOpMode {
    private ElapsedTime runtime = new ElapsedTime();
    HardwareShieldsMecanum robot = new HardwareShieldsMecanum();
    // === DEFINE CONSTANTS HERE! ===

    // ==============================
    public void runOpMode() {
        // double lastTime = runtime.milliseconds();

        // State used for updating telemetry
        // Orientation angles;
        // Acceleration gravity;

        robot.init(hardwareMap);
        telemetry.addData("Greetings, Human", "");
        telemetry.update();

        waitForStart();
        runtime.reset();
        // we don't want to change orientation, just heading
        Orientation targOrient;
        targOrient = robot.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
        headingPowerTime(0,0.5,2, targOrient);
        headingPowerTime(45,0.5,2, targOrient);
        headingPowerTime(90,0.5,2, targOrient);
        headingPowerTime(135,0.5,2, targOrient);
        headingPowerTime(180,0.5,2, targOrient);
        headingPowerTime(225,0.5,2, targOrient);
        headingPowerTime(270,0.5,2, targOrient);
        headingPowerTime(315,0.5,2, targOrient);
        stopDriving();
    }
    // Functions ----------------------------------------------------------------------------------------------------------------

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

        double x = pwr*Math.cos(Math.toRadians(heading));
        double y = pwr*Math.sin(Math.toRadians(heading));

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
    //Stop Driving - Kill power to all the motors
    void stopDriving() {
        robot.frontLeftMotor.setPower(0);
        robot.frontRightMotor.setPower(0);
        robot.backLeftMotor.setPower(0);
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