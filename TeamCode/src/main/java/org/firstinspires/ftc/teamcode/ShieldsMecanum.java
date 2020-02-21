package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;

import java.util.concurrent.TimeUnit;

@TeleOp(name = "Shields Mechanum", group = "Opmode")
@Disabled

public class ShieldsMecanum extends LinearOpMode {
    // Declare OpMode members.
    private ElapsedTime runtime = new ElapsedTime();
    HardwareShieldsMecanum robot = new HardwareShieldsMecanum();

    @Override
    public void runOpMode() {
        telemetry.addData("Status", "Greetings, Human");
        telemetry.update();
        robot.init(hardwareMap);
        // Wait for the game to start (driver presses PLAY)
        waitForStart();
        runtime.reset();
        double x;
        double y;
        double r;
        double frontLeft;
        double frontRight;
        double backLeft;
        double backRight;
        while (opModeIsActive()) {
            // get joystick input
            y = gamepad1.left_stick_y;
            x = gamepad1.left_stick_x;
            r = gamepad1.right_stick_x;
            r = r / 3;

            // mecanum math
            backRight = +y - x + r;
            frontRight = +y + x + r;
            backLeft = -y - x + r;
            frontLeft = -y + x + r;

            // send power to motors
            robot.frontLeftMotor.setPower(frontLeft);
            robot.frontRightMotor.setPower(frontRight);
            robot.backLeftMotor.setPower(backLeft);
            robot.backRightMotor.setPower(backRight);

            // telemetry
            telemetry.addData("FrontLeft:", frontLeft);
            telemetry.addData("FrontRight", frontRight);
            telemetry.addData("BackLeft", backLeft);
            telemetry.addData("BackRight", backRight);
            telemetry.update();
        }
    }
}
    // Functions --------------------------------------

