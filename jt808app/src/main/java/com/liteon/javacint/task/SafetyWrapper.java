package com.liteon.javacint.task;

import java.util.TimerTask;
import com.liteon.javacint.logging.Logger;

/**
 * Timer task safety wrapper.
 *
 * This wrapper only guarantees a runnable task won't break the Timer.
 *
 */
public class SafetyWrapper extends TimerTask {

    private final Runnable runnable;

    public SafetyWrapper(Runnable runnable) {
        this.runnable = runnable;
    }

    public void run() {
        try {
            runnable.run();
        } catch (Throwable ex) {
            if (Logger.BUILD_CRITICAL) {
                Logger.log("SafetyWrapper", ex, true);
            }
        }
    }
}
