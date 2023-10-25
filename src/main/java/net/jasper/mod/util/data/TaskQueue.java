package net.jasper.mod.util.data;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TaskQueue {

    private List<Runnable> tasks;
    private List<String> taskNames;
    public TaskQueue() {
        this.tasks = new ArrayList<>();
        this.taskNames = new ArrayList<>();
    }
    public int size() {
        return this.tasks.size();
    }

    public boolean isEmpty() {
        return this.tasks.isEmpty();
    }

    public boolean contains(Runnable o) {
        return this.tasks.contains(o);
    }

    public boolean contains(String name) {
        return this.taskNames.contains(name);
    }

    public void add(String name, Runnable r) {
        this.taskNames.add(name);
        this.tasks.add(r);
    }

    public void remove(String name) {
        int toRemove = this.taskNames.indexOf(name);
        this.tasks.remove(toRemove);
        this.taskNames.remove(toRemove);
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

    public Runnable peek() {
        return this.tasks.get(0);
    }

    public String peekName() {
        return this.taskNames.get(0);
    }
}
