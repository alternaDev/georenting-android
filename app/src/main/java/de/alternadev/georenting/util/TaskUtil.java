package de.alternadev.georenting.util;


import com.google.android.gms.tasks.Task;

public final class TaskUtil {
    public static final <T> T waitForTask(Task<T> t) {
        while(!t.isComplete()) {
            Thread.yield();
        }
        return t.getResult();
    }
}
