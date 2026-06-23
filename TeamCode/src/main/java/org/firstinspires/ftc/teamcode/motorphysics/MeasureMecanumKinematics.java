package org.firstinspires.ftc.teamcode.motorphysics;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;

// TODO add strafing tests
@Config
@TeleOp(name = "Mecanum Decel Test")
public class MeasureMecanumKinematics extends MeasureKinematicsOpMode {
    private DcMotorEx fl, fr, bl, br;
    private GoBildaPinpointDriver pinpoint;

    @Override
    public void initHardware() {
        super.initHardware();

        fl = hardwareMap.get(DcMotorEx.class, "frontLeft");
        fr = hardwareMap.get(DcMotorEx.class, "frontRight");
        bl = hardwareMap.get(DcMotorEx.class, "backLeft");
        br = hardwareMap.get(DcMotorEx.class, "backRight");

        fl.setDirection(DcMotorSimple.Direction.REVERSE);
        bl.setDirection(DcMotorSimple.Direction.REVERSE);
        fr.setDirection(DcMotorSimple.Direction.FORWARD);
        br.setDirection(DcMotorSimple.Direction.FORWARD);

        fl.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        fr.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        bl.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        br.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        pinpoint = hardwareMap.get(GoBildaPinpointDriver.class, "pinpoint");
        pinpoint.setOffsets(0, 0, DistanceUnit.MM);
        pinpoint.setEncoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        pinpoint.setBulkReadScope(
                GoBildaPinpointDriver.Register.X_VELOCITY,
                GoBildaPinpointDriver.Register.Y_VELOCITY,
                GoBildaPinpointDriver.Register.X_POSITION,
                GoBildaPinpointDriver.Register.Y_POSITION,
                GoBildaPinpointDriver.Register.H_ORIENTATION
        );
        pinpoint.setEncoderDirections(
                GoBildaPinpointDriver.EncoderDirection.FORWARD,
                GoBildaPinpointDriver.EncoderDirection.FORWARD
        );
        pinpoint.resetPosAndIMU();
    }

    @Override
    public void loop() {
        super.loop();

        pinpoint.update();

        Pose2D pose = pinpoint.getPosition();
        double vx = pinpoint.getVelX(DistanceUnit.INCH);
        double vy = pinpoint.getVelY(DistanceUnit.INCH);
        double headingRad = pose.getHeading(AngleUnit.RADIANS);
        double velocityMagnitude = Math.sqrt(vx * vx + vy * vy);
        double forwardVelocity = vx * Math.cos(headingRad) + vy * Math.sin(headingRad);

        if (velocityMagnitude < 0.01) {
            return;
        }

        dashboardTelemetry.addData("velocityMag inches per second", velocityMagnitude);
        dashboardTelemetry.addData("forwardVelocity inches per second", forwardVelocity);
        dashboardTelemetry.addData("x inches", pose.getX(DistanceUnit.INCH));
        dashboardTelemetry.addData("y inches", pose.getY(DistanceUnit.INCH));
        dashboardTelemetry.addData("heading radians", headingRad);
        dashboardTelemetry.update();
    }

    @Override
    protected void setDutyCycle(double dutyCycle) {
        this.dutyCycle = dutyCycle;
        fl.setPower(dutyCycle);
        fr.setPower(dutyCycle);
        bl.setPower(dutyCycle);
        br.setPower(dutyCycle);
    }

    @Override
    protected void setZeroPowerBehavior(DcMotor.ZeroPowerBehavior zpb) {
        zeroPowerBehavior = zpb;
        fl.setZeroPowerBehavior(zpb);
        fr.setZeroPowerBehavior(zpb);
        bl.setZeroPowerBehavior(zpb);
        br.setZeroPowerBehavior(zpb);
    }
}
