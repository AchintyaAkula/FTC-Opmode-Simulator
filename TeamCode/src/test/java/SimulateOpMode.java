import org.firstinspires.ftc.teamcode.opmode.base.RobotCentricOpMode;
import org.firstinspires.ftc.teamcode.opmode.base.TeleOpMode;
import org.junit.Test;

import java.io.IOException;

public class SimulateOpMode {
    @Test
    public void fieldCentric() throws InterruptedException, IOException {
        OpModeSimulator.simulate(new TeleOpMode() {
            @Override
            protected void onFirstDriverInput() {}
        });
    }
    @Test
    public void robotCentric() throws InterruptedException, IOException {
        OpModeSimulator.simulate(new RobotCentricOpMode() {
            @Override
            protected void onFirstDriverInput() {}
        });
    }
}
