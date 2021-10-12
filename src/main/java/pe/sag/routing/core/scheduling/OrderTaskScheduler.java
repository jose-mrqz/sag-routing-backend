package pe.sag.routing.core.scheduling;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;

public class OrderTaskScheduler {
    private class ModifyStatusTask extends TimerTask {
        public ModifyStatusTask() {
        }
        public void run() {
            System.out.println("Executing @: " + LocalDateTime.now() + " ");
        }
    }

    public void scheduleMessage() {
        Timer timer = new Timer();
        TimerTask task = new ModifyStatusTask();
        System.out.println("Scheduling @: " + LocalDateTime.now() + " to run after 5 seconds.");
        long wait = Duration.between(LocalDateTime.now(), LocalDateTime.now().plusSeconds(5)).toMillis();
        timer.schedule(task, wait, Long.MAX_VALUE);
    }
}
