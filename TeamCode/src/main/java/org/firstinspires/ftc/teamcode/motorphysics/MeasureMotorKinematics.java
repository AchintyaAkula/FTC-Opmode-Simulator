package org.firstinspires.ftc.teamcode.motorphysics;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;


@Config
@TeleOp(name = "Measure Motor Kinematics")
public class MeasureMotorKinematics extends MeasureKinematicsOpMode {
    private DcMotorEx motor;

    private double previousPosition = 0;

    @Override
    public void initHardware() {
        super.initHardware();
        motor = hardwareMap.get(DcMotorEx.class, "motor");
        motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    @Override
    public void loop() {
        super.loop();

        double position = motor.getCurrentPosition();
        double velocity = motor.getVelocity(AngleUnit.RADIANS);
        double batteryVoltage = voltageSensor.getVoltage();
        double current = motor.getCurrent(CurrentUnit.AMPS);
        double velocityFromPosition = (position - previousPosition) / deltaTime;

        previousPosition = position;

        if (velocity < 0.1) {
            return;
        }

        dashboardTelemetry.addData("velocity radians per second", velocity);
        dashboardTelemetry.addData("current amps", current);
        dashboardTelemetry.addData("position ticks", position);
        dashboardTelemetry.addData("velocity from position ticks", velocityFromPosition);
        dashboardTelemetry.addData("battery voltage", batteryVoltage);
        dashboardTelemetry.update();
    }

    @Override
    protected void setDutyCycle(double dutyCycle) {
        this.dutyCycle = dutyCycle;
        motor.setPower(dutyCycle);
    }

    @Override
    protected void setZeroPowerBehavior(DcMotor.ZeroPowerBehavior behavior) {
        this.zeroPowerBehavior = behavior;
        motor.setZeroPowerBehavior(behavior);
    }
}
