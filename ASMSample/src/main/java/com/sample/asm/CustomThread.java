package com.sample.asm;

import android.util.Log;

public class CustomThread extends Thread {
    private static final String TAG = "CustomThread";
    public CustomThread() {
        super();
    }


    public CustomThread(Runnable target) {
        super(target);
    }


    public CustomThread(ThreadGroup group, Runnable target) {
        super(group, target);
    }


    public CustomThread(String name) {
        super(name);
    }

    public CustomThread(ThreadGroup group, String name) {
        super(group, name);
    }

    public CustomThread(final Runnable runnable, final String name) {
        super(runnable, name);
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        super.run();
        Log.e(TAG,"thread name:" + getName() + ", run time:" + (System.currentTimeMillis() - start));
    }
}
