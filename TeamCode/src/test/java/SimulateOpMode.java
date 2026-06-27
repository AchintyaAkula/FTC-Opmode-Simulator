import org.firstinspires.ftc.teamcode.opmode.FieldCentricTeleop;
import org.jjophoven.simulator.OpModeSimulator;
import org.jjophoven.simulator.OpModeRegister;
import org.junit.Test;
import java.io.IOException;

public class SimulateOpMode {
    @Test
    public void test() throws IOException, InterruptedException {
         OpModeRegister register = new OpModeRegister();
         for (Class<?> opMode : register.getTeleOpModes()) {
             System.out.println(opMode.getName());
         }

//         Optionally simulate a single opmode directly
         OpModeSimulator.simulate(new FieldCentricTeleop());
    }
}

