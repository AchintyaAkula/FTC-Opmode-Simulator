package org.jjophoven.fakehardware;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

public class FakeHardware {

    FakeHardwareMap fakeHardwareMap;
    public FakeDriverStationServer driverStation;

    public FakeHardware(OpMode opMode, FakeDriverStationServer driverStation) {
        this.driverStation = driverStation;
        fakeHardwareMap = new FakeHardwareMap();
        opMode.hardwareMap = fakeHardwareMap;
        opMode.telemetry = new FakeTelemetry(driverStation);
        opMode.gamepad1 = driverStation.gamepad1;
        opMode.gamepad2 = driverStation.gamepad2;
    }

    public void update() {
        driverStation.poll();
        fakeHardwareMap.updateHardware();
    }
}
