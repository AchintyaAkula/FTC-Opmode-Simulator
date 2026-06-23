import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.IntStream;

public class CSVSmoother {
    public static void main(String[] args) throws FileNotFoundException {
        File logFile = new File("model/+1to-0.2.csv");

        if (!logFile.exists()) {
            throw new FileNotFoundException(
                    "Could not find file. Working directory is: "
                            + new File(".").getAbsolutePath()
            );
        }

        Scanner scanner = new Scanner(logFile);

        List<String> lines = new ArrayList<>();
        while (scanner.hasNextLine()) {
            lines.add(scanner.nextLine());
        }

        Scanner scanner2 = new Scanner(new File("model/FullPowerTo.0001.csv"));
        while (scanner2.hasNextLine()) {
            lines.add(scanner2.nextLine());
        }

        Scanner scanner3 = new Scanner(new File("model/+1to-0.0001.csv"));
        while (scanner3.hasNextLine()) {
            lines.add(scanner3.nextLine());
        }

        if (lines.size() < 2) {
            throw new IllegalArgumentException("CSV contains no data rows.");
        }

        int samples = lines.size() - 1; // skip header

        double[] vels = new double[samples];
        double[] loopTimes = new double[samples];
        double[] accels = new double[samples];
        double[] voltages = new double[samples];
        double[] duties = new double[samples];
        double[] times = new double[samples];

        double firstTimestampMs = Double.NaN;

        for (int i = 1; i < lines.size(); i++) {

            String[] row = lines.get(i)
                    .split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

            int idx = i - 1;

            // Column indices from your CSV
            double batteryVoltage = parse(row, 10);
            double dutyCycle = parse(row, 12);
            double loopTimeMs = parse(row, 13);
            double timestampMs = parse(row, 0);
            double velocity = parse(row, 18);

            if (Double.isNaN(firstTimestampMs) && !Double.isNaN(timestampMs)) {
                firstTimestampMs = timestampMs;
            }

            vels[idx] = Double.isNaN(velocity) ? 0.0 : velocity;

            voltages[idx] =
                    Double.isNaN(batteryVoltage) ? 13.0 : batteryVoltage;

            duties[idx] =
                    Double.isNaN(dutyCycle) ? 0.0 : dutyCycle;

            loopTimes[idx] =
                    Double.isNaN(loopTimeMs) ? 10.0 : loopTimeMs;

            times[idx] =
                    Double.isNaN(timestampMs)
                            ? 0.0
                            : (timestampMs - firstTimestampMs) / 1000.0;
        }

        // Smooth velocity
        SavitzkyGolayFilter filter = new SavitzkyGolayFilter(41, 2);
        vels = filter.filter(vels);

        for (int i = 0; i < accels.length; i++) {

            if (i == 0) {
                accels[i] =
                        (vels[i + 1] - vels[i])
                                / loopTimes[i + 1]
                                * 1000.0;

            } else if (i == accels.length - 1) {
                accels[i] =
                        (vels[i] - vels[i - 1])
                                / loopTimes[i]
                                * 1000.0;

            } else {
                accels[i] =
                        (vels[i + 1] - vels[i - 1])
                                / (loopTimes[i] + loopTimes[i + 1])
                                * 1000.0;
            }
        }

        printArray("a", accels, vels);
        printArray("v", vels, vels);

        double[] u = IntStream.range(0, voltages.length)
                .mapToDouble(i -> duties[i] * voltages[i])
                .toArray();

        printArray("u", u, vels);
        printArray("t", times, vels);
        printArray("d", duties, vels);
        printArray("b", voltages, vels);
    }

    private static double parse(String[] row, int index) {

        if (index >= row.length) {
            return Double.NaN;
        }

        String s = row[index].trim();

        if (s.isEmpty() || s.equals("null")) {
            return Double.NaN;
        }

        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    private static void printArray(
            String name,
            double[] values,
            double[] vels
    ) {

        System.out.print(name + "=[");

        boolean first = true;

        for (int i = 0; i < values.length; i++) {

            if (Math.abs(vels[i]) < 0.2) {
                continue;
            }

            if (!first) {
                System.out.print(",");
            }

            System.out.printf("%.4f", values[i]);
            first = false;
        }

        System.out.println("]");
    }
}