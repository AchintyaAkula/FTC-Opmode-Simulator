import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.IntStream;

public class CSVSmoother2 {
    private static class DataSet {
        double[] vels;
        double[] loopTimes;
        double[] voltages;
        double[] duties;
        double[] times;
    }

    public static void main(String[] args) throws FileNotFoundException {

        List<DataSet> datasets = new ArrayList<>();

        datasets.add(loadAndSmooth("model/+1to-0.2.csv"));
        datasets.add(loadAndSmooth("model/+1to.0001.csv"));
        datasets.add(loadAndSmooth("model/+1to-.0001.csv"));

        int totalSamples = datasets.stream()
                .mapToInt(d -> d.vels.length)
                .sum();

        double[] vels = new double[totalSamples];
        double[] loopTimes = new double[totalSamples];
        double[] voltages = new double[totalSamples];
        double[] duties = new double[totalSamples];
        double[] times = new double[totalSamples];

        int index = 0;

        for (DataSet d : datasets) {
            System.arraycopy(d.vels, 0, vels, index, d.vels.length);
            System.arraycopy(d.loopTimes, 0, loopTimes, index, d.loopTimes.length);
            System.arraycopy(d.voltages, 0, voltages, index, d.voltages.length);
            System.arraycopy(d.duties, 0, duties, index, d.duties.length);
            System.arraycopy(d.times, 0, times, index, d.times.length);

            index += d.vels.length;
        }

        double[] accels = new double[vels.length];

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

    private static DataSet loadAndSmooth(String path)
            throws FileNotFoundException {

        File file = new File(path);

        if (!file.exists()) {
            throw new FileNotFoundException(
                    "Could not find file: " + file.getAbsolutePath()
            );
        }

        Scanner scanner = new Scanner(file);

        List<String> lines = new ArrayList<>();

        while (scanner.hasNextLine()) {
            lines.add(scanner.nextLine());
        }

        if (lines.size() < 2) {
            throw new IllegalArgumentException(
                    "CSV contains no data rows: " + path
            );
        }

        int samples = lines.size() - 1;

        DataSet data = new DataSet();

        data.vels = new double[samples];
        data.loopTimes = new double[samples];
        data.voltages = new double[samples];
        data.duties = new double[samples];
        data.times = new double[samples];

        double firstTimestampMs = Double.NaN;

        for (int i = 1; i < lines.size(); i++) {

            String[] row = lines.get(i)
                    .split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

            int idx = i - 1;

            double batteryVoltage = parse(row, 10);
            double dutyCycle = parse(row, 12);
            double loopTimeMs = parse(row, 13);
            double timestampMs = parse(row, 0);
            double velocity = parse(row, 18);

            if (Double.isNaN(firstTimestampMs)
                    && !Double.isNaN(timestampMs)) {
                firstTimestampMs = timestampMs;
            }

            data.vels[idx] =
                    Double.isNaN(velocity) ? 0.0 : velocity;

            data.voltages[idx] =
                    Double.isNaN(batteryVoltage) ? 13.0 : batteryVoltage;

            data.duties[idx] =
                    Double.isNaN(dutyCycle) ? 0.0 : dutyCycle;

            data.loopTimes[idx] =
                    Double.isNaN(loopTimeMs) ? 10.0 : loopTimeMs;

            data.times[idx] =
                    Double.isNaN(timestampMs)
                            ? 0.0
                            : (timestampMs - firstTimestampMs) / 1000.0;
        }

        // Smooth only this file's velocity data
        SavitzkyGolayFilter filter = new SavitzkyGolayFilter(41, 2);
        data.vels = filter.filter(data.vels);

        return data;
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