package net.jasper.mod.util.data;

import java.util.*;

public class TaskQueue {

    private final List<Runnable> tasks;
    private final List<String> taskNames;
    public TaskQueue() {
        this.tasks = new ArrayList<>();
        this.taskNames = new ArrayList<>();
    }

    public boolean isEmpty() {
        return this.tasks.isEmpty();
    }

    public void add(String name, Runnable r) {
        this.taskNames.add(name);
        this.tasks.add(r);
    }


    public void clear() {
        this.tasks.clear();
        this.taskNames.clear();
    }

    public Runnable poll() {
        Runnable result = this.tasks.get(0);
        this.tasks.remove(0);
        this.taskNames.remove(0);
        return result;
    }
}
