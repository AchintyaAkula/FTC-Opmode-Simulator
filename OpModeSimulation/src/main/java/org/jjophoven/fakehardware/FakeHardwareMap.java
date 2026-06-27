package org.jjophoven.fakehardware;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;

import java.util.ArrayList;
import java.util.List;

public class FakeHardwareMap extends HardwareMap {
    public FakeHardwareMap() {
        super(null, null);
        voltageSensor.put("voltageSensor", new FakeVoltageSensor());
    }

    List<FakeMotor> motors = new ArrayList<>();
    FakeMecanum drivetrain;
    FakePinpoint pinpoint;

    @Override
    public <T> T get(Class<? extends T> classOrInterface, String deviceName) {
        synchronized (lock) {
            if (classOrInterface.equals(DcMotor.class) || classOrInterface.equals(DcMotorEx.class)) {
                FakeMotor motor = new FakeMotor();
                if (deviceName.equals("frontLeft")) {
                    motors.add(motor);
                }
                if (deviceName.equals("frontRight")) {
                    motors.add(motor);
                }
                if (deviceName.equals("backLeft")) {
                    motors.add(motor);
                }
                if (deviceName.equals("backRight")) {
                    motors.add(motor);
                    drivetrain = new FakeMecanum(motors.toArray(new FakeMotor[4]));
                }

                return (T) motor;
            } else if (classOrInterface.equals(VoltageSensor.class)) {
                return (T) new FakeVoltageSensor();
            } else if (classOrInterface.equals(GoBildaPinpointDriver.class)) {
                pinpoint = new FakePinpoint();
                return (T) pinpoint;
            }
            System.out.println("Unable to find a hardware device with name \"" + deviceName + "\" and type " + classOrInterface.getSimpleName());
            return null;
        }
    }

    public void updateHardware() {
        drivetrain.update(0.02);
        pinpoint.pose2D =
                new Pose2D(DistanceUnit.MM,
                0,
                0,
                AngleUnit.RADIANS,
                AngleUnit.normalizeRadians(drivetrain.heading));
    }
}
