package org.firstinspires.ftc.teamcode.utils;

public class OpModeTimer {

    private long lastLoopTime;

    private long initStart;
    private long startTime;

    private boolean started = false;

    private double smoothedDt = 0.0;
    private double dt = 0.0;

    private final double alpha;
    private final double maxDt;

    public OpModeTimer() {
        this(0.85, 0.05);
    }

    public OpModeTimer(double alpha, double maxDt) {
        this.alpha = alpha;
        this.maxDt = maxDt;

        long now = System.nanoTime();
        this.initStart = now;
        this.lastLoopTime = now;
    }

    public void start() {
        startTime = System.nanoTime();
        started = true;
    }

    public void reset() {
        long now = System.nanoTime();
        initStart = now;
        startTime = now;
        lastLoopTime = now;
        dt = 0.0;
        smoothedDt = 0.0;
        started = true;
    }

    public void updateDt() {
        long now = System.nanoTime();

        dt = (now - lastLoopTime) / 1e9;
        lastLoopTime = now;

        if (dt > maxDt) dt = maxDt;

        if (smoothedDt == 0.0) {
            smoothedDt = dt;
        } else {
            smoothedDt = alpha * smoothedDt + (1.0 - alpha) * dt;
        }
    }

    public double getDt() {
        return dt;
    }

    public double getSmoothedDt() {
        return smoothedDt;
    }

    public double getInitTime() {
        return (System.nanoTime() - initStart) / 1e9;
    }

    public double getRuntime() {
        if (!started) return 0.0;
        return (System.nanoTime() - startTime) / 1e9;
    }

    public boolean isStarted() {
        return started;
    }
}