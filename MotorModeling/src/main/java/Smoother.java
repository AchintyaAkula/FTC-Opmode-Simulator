import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.IntStream;

public class Smoother {
    public static void main(String[] args) throws FileNotFoundException {
        File logFile = new File("model/brake.txt");

        if (!logFile.exists()) {
            throw new FileNotFoundException(
                    "Could not find .txt. Working directory is: "
                            + new File(".").getAbsolutePath()
            );
        }

        Scanner scanner = new Scanner(logFile);
        List<String> logs = new ArrayList<>();

        while (scanner.hasNextLine())
            logs.add(scanner.nextLine());

        double[] vels = new double[logs.size()];
        double[] loopTimes = new double[logs.size()];
        double[] accels = new double[logs.size()];
        double[] voltages = new double[logs.size()];
        double[] duties = new double[logs.size()];
        double[] times = new double[loopTimes.length];

        for (int i = 0; i < logs.size(); i++) {

            String[] curLogs = logs.get(i).split(",");

            String vel = curLogs[14];
            if (!vel.equals("null")) vels[i] = Double.parseDouble(vel);
            else vels[i] = 0.0;

            String volts = curLogs[15];
            if (!volts.equals("null")) voltages[i] = Double.parseDouble(volts);
            else voltages[i] = 13.5;

            String loop = curLogs[11];
            if (!loop.equals("null")) loopTimes[i] = Double.parseDouble(loop);
            else loopTimes[i] = 0.01;

            String mode = curLogs[12];
            if (mode.equals("\"IDLE\"")) duties[i] = 0;
            else if (mode.equals("\"DRIVING\"")) duties[i] = 1.0;
            else if (mode.split(" ")[0].equals("\"COAST")) duties[i] = 0.0001;
            else if (mode.split(" ")[0].equals("\"BRAKE")) duties[i] = -0.0001;
        }

        double t = 0.0;
        for (int i = 0; i < loopTimes.length; i++) {
            t += loopTimes[i]/1000;
            times[i] = t;
        }

        SavitzkyGolayFilter filter = new SavitzkyGolayFilter(21, 2);

        vels = filter.filter(vels);

        for (int i = 0; i < accels.length; i++) {
            if (i == 0) accels[i] = (vels[i + 1] - vels[i]) / loopTimes[i + 1] * 1000.0;
            else if (i == logs.size() - 1) accels[i] = (vels[i] - vels[i - 1]) / loopTimes[i] * 1000.0;
            else accels[i] = (vels[i + 1] - vels[i - 1]) / (loopTimes[i] + loopTimes[i + 1]) * 1000.0;
        }

        printArray("a", accels, vels);
        printArray("v", vels, vels);

        double[] u = IntStream.range(0, voltages.length)
                .mapToDouble(i -> duties[i] * voltages[i])
                .toArray();
        printArray("u", u, vels);
        printArray("t", times, times);
    }

    private static void printArray(String name, double[] values, double[] vels) {
        System.out.print(name + "=[");
        boolean first = true;

        for (int i = 0; i < values.length; i++) {
            if (Math.abs(vels[i]) < 0.01) continue;

            if (!first) System.out.print(",");
            System.out.printf("%.4f", values[i]);
            first = false;
        }

        System.out.println("]");
    }
}