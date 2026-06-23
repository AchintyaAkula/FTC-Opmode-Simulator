public class SavitzkyGolayFilter {
    private final int windowSize;
    private final int polyDegree;
    private final double[] coefficients;

    /**
     * @param windowSize Must be an odd integer (e.g., 5, 7, 11).
     * @param polyDegree Must be less than the windowSize.
     */
    public SavitzkyGolayFilter(int windowSize, int polyDegree) {
        if (windowSize % 2 == 0) {
            throw new IllegalArgumentException("Window size must be an odd number.");
        }
        if (polyDegree >= windowSize) {
            throw new IllegalArgumentException("Polynomial degree must be less than the window size.");
        }
        this.windowSize = windowSize;
        this.polyDegree = polyDegree;
        this.coefficients = computeCoefficients();
    }

    /**
     * Computes SG coefficients.
     * Formula: C = (J^T * J)^-1 * J^T
     */
    private double[] computeCoefficients() {
        Matrix J = getDesignMatrix();

        // 2. Linear Least Squares Math: C = (J^T * J)^-1 * J^T
        Matrix JT = J.transposed();
        Matrix invJTJ = JT.multiply(J).inverse();
        Matrix C = invJTJ.multiply(JT);

        // 3. Extract the 0-th row of matrix C
        // The 0-th row yields the coefficients required for central point smoothing
        double[] coeffs = new double[windowSize];
        for (int j = 0; j < windowSize; j++) {
            coeffs[j] = C.get(0, j);
        }

        return coeffs;
    }

    private Matrix getDesignMatrix() {
        int rows = windowSize;
        int cols = polyDegree + 1;
        int m = windowSize / 2;

        // 1. Build the Design Matrix J (windowSize x polyDegree + 1)
        Matrix J = new Matrix(rows, cols);
        for (int i = 0; i < rows; i++) {
            double x = i - m; // Coordinate relative to center
            for (int j = 0; j < cols; j++) {
                J.set(i, j, Math.pow(x, j));
            }
        }
        return J;
    }

    /**
     * Filters a custom array of noisy data and returns a new smoothed Vector.
     */
    public double[] filter(double[] data) {
        int length = data.length;
        if (length < windowSize) {
            throw new IllegalArgumentException("Data length must be at least equal to the window size.");
        }

        double[] smoothed = new double[length];
        int m = windowSize / 2;

        // Apply convolution matrix style across the vector
        for (int i = 0; i < length; i++) {
            double sum = 0;
            for (int j = 0; j < windowSize; j++) {
                int dataIdx = i + j - m;
                sum += coefficients[j] * getExtendedValue(data, dataIdx);
            }
            smoothed[i] = sum;
        }

        return smoothed;
    }

    /**
     * Helper to handle vector edge padding using symmetric reflection.
     */
    private double getExtendedValue(double[] data, int index) {
        int length = data.length;
        if (index < 0) {
            return data[-index]; // Mirror at the beginning
        } else if (index >= length) {
            return data[2 * length - 2 - index]; // Mirror at the end
        }
        return data[index];
    }
}