// =============================================================
// This is the bare minimum code you need for a BACON autonomous
// =============================================================

// Import hardware libraries and tell Android studio where our stuff it
package org.firstinspires.ftc.teamcode;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;

// Got to name it and specify Autonomous or TeleOp
// @Disabled will make this not show up on the phone
@Autonomous(name = "Greetings, Human.", group = "Opmode")
@Disabled

// In FTC, we write classes that extend OpMode classes that the FTC people wrote
public class ShieldsAutoShell extends LinearOpMode {
    // Life is easier if you use a hardware class.  This creates the hardware objects
    HardwareBlank robot = new HardwareBlank();

    // this will run once the opmode is fired up
    public void runOpMode() {
        // need to initialize the hardware class
        robot.init(hardwareMap);
        telemetry.addData("","Greetings, Human.");
        telemetry.update();
        // wait for the driver to hit play
        waitForStart();
        telemetry.addData("","You clicked play.");
        telemetry.update();
        while(opModeIsActive()) {

        }
    }
}