package net.jasper.mod.util.data;

import lombok.Getter;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.jasper.mod.util.PlayerAutomaExceptionHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple Queue for Runnable objects
 */
public class TaskQueue {

    public static final long LOW_PRIORITY = -1;
    public static final long MEDIUM_PRIORITY = 0;
    public static final long HIGH_PRIORITY = 1;
    public static final Map<String, TaskQueue> QUEUES = new HashMap<>();
    private static boolean done = false; // if work for this tick is done

    private final long priority;
    private final List<Runnable> tasks;
    @Getter
    private boolean paused;

    public TaskQueue(long priority) {
        this.priority = priority;
        this.tasks = new ArrayList<>();
        paused = false;
    }

    public boolean isEmpty() {
        return this.tasks.isEmpty();
    }

    public void pause() {
        this.paused = true;
    }

    public void resume() {
        this.paused = false;
    }

    public void add(Runnable r) {
        this.tasks.add(r);
    }

    public void clear() {
        this.tasks.clear();
    }

    public Runnable poll() {
        Runnable result = this.tasks.getFirst();
        this.tasks.removeFirst();
        return result;
    }

    private static boolean isHighestPriority(long priority) {
        // If other queues are empty or have lower priority return true
        return QUEUES.values().stream().allMatch(queue -> queue.priority <= priority || queue.isEmpty() || queue.isPaused());
    }

    public void register(String name) {
        QUEUES.put(name, this);

        // Clear done flag on client tick start
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                return;
            }
            done = false;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || done) {
                return;
            }

            // Only run the task if it has the highest priority or if no other queue is busy
            if (!isHighestPriority(this.priority) || this.isEmpty()) {
                return;
            }

            if (this.paused) {
                return;
            }
            try {
                this.poll().run();
            } catch (Exception exception) {
                PlayerAutomaExceptionHandler.handleException(exception);
            }
            done = true;
        });
    }
}