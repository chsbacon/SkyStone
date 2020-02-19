package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.HardwareBACONbot;


/**
 * This file contains an minimal example of a Linear "OpMode". An OpMode is a 'program' that runs in either
 * the autonomous or the teleop period of an FTC match. The names of OpModes appear on the menu
 * of the FTC Driver Station. When an selection is made from the menu, the corresponding OpMode
 * class is instantiated on the Robot Controller and executed.
 *
 * This particular OpMode just executes a basic Tank Drive Teleop for a two wheeled robot
 * It includes all the skeletal structure that all linear OpModes contain.
 *
 * Use Android Studios to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this opmode to the Driver Station OpMode list
 */

@TeleOp(name="Claw Servo Calibration", group="Linear Opmode")
//@Disabled
public class ClawServoCalibration extends LinearOpMode {

    HardwareBACONbot robot = new HardwareBACONbot();   // Use BACONbot's hardware


    public void runOpMode(){


        robot.init(hardwareMap);

        while (opModeIsActive()) {

            double servoPos = robot.clawServo.getPosition();
            while (!gamepad1.right_bumper && !gamepad1.left_bumper) {
                robot.clawServo.setPosition(servoPos);
                telemetry.addData("Servo Position", servoPos);
                telemetry.update();
            }

            while (gamepad1.right_bumper && !gamepad1.left_bumper) {
                //Servo Spins +1
                // Telemetry Servo Number Angle Thing
                //sleep for like maybe 250

                robot.clawServo.setPosition(servoPos);
                telemetry.addData("Servo Position", servoPos);
                telemetry.update();
                sleep(250);
                servoPos = servoPos + .05;


            }
            while (gamepad1.left_bumper && !gamepad1.right_bumper) {

                //Servo Spins -1
                // Telemetry Servo Number Angle Thing
                //sleep for like maybe 250
                robot.clawServo.setPosition(servoPos);
                telemetry.addData("Servo Position", servoPos);
                telemetry.update();
                sleep(250);
                servoPos = servoPos - .05;
            }
        }
    }

}
