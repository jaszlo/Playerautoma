package net.jasper.mod.util.data;

import java.util.*;

/**
 * Simple Queue for Runnable objects
 */
public class TaskQueue {

    private final List<Runnable> tasks;
    public TaskQueue() {
        this.tasks = new ArrayList<>();
    }

    public boolean isEmpty() {
        return this.tasks.isEmpty();
    }

    public void add(Runnable r) {
        this.tasks.add(r);
    }


    public void clear() {
        this.tasks.clear();
    }

    public Runnable poll() {
        Runnable result = this.tasks.get(0);
        this.tasks.remove(0);
        return result;
    }
}
