/*******************************************************************************
 * Copyright (C) 2022-2023 WaveMaker, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of WaveMaker, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the source code license agreement you entered into with WaveMaker, Inc.
 ******************************************************************************/

package com.wavemaker.app.memory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Will interrupt all the running threads in the given class loader except current thread.
 * Post interrupt after a specific timeout if any threads are still alive it logs a message
 */
public class StopRunningThreadsListener implements MemoryLeakPreventionListener {

    private static final Logger logger = LoggerFactory.getLogger(StopRunningThreadsListener.class);
    private int maxWaitTimeOfRunningThreadsInMillis = Integer.getInteger("wm.app.maxWaitTimeRunningThreads", 5000);

    public StopRunningThreadsListener() {
    }

    public StopRunningThreadsListener(int maxWaitTimeOfRunningThreadsInMillis) {
        this.maxWaitTimeOfRunningThreadsInMillis = maxWaitTimeOfRunningThreadsInMillis;
    }

    @Override
    public void listen(ClassLoader classLoader) {
        stopRunningThreads(classLoader);
    }

    private void stopRunningThreads(ClassLoader classLoader) {
        try {
            List<Thread> threads = getThreads(classLoader);
            List<Thread> runningThreads = new ArrayList<>();
            for (Thread thread : threads) {
                if (isAliveAndNotCurrentThread(thread)) {
                    logger.info("Interrupting thread {}", thread);
                    thread.interrupt();
                    runningThreads.add(thread);
                }
                stopTimerThread(thread);
            }
            if (!runningThreads.isEmpty()) {
                logger.info("Waiting for interrupted threads to be finished in max of {} ms", maxWaitTimeOfRunningThreadsInMillis);
                join(runningThreads, maxWaitTimeOfRunningThreadsInMillis);
                for (Thread thread : runningThreads) {
                    if (thread.isAlive()) {
                        StackTraceElement[] stackTrace = thread.getStackTrace();
                        Throwable throwable = new IllegalThreadStateException(
                            "Thread [" + thread.getName() + "] is Still running");
                        throwable.setStackTrace(stackTrace);
                        logger.warn(
                            "Thread {} is still alive after waiting for {} and will mostly probably create a memory leak",
                            thread.getName(),
                            maxWaitTimeOfRunningThreadsInMillis, throwable);
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Failed in stopRunningThreads", e);
        }
    }

    private void stopTimerThread(Thread thread) {
        if (!thread.getClass().getName().startsWith("java.util.Timer")) {
            return;
        }
        logger.info("Stopping Timer thread {}", thread);
        Field newTasksMayBeScheduled = ClassUtils.findField(thread.getClass(), "newTasksMayBeScheduled");
        Field queueField = ClassUtils.findField(thread.getClass(), "queue");
        if (queueField != null && newTasksMayBeScheduled != null) {
            try {
                Object queue = queueField.get(thread);
                newTasksMayBeScheduled.set(thread, false);
                Method clearMethod = ClassUtils.findMethod(queue.getClass(), "clear");
                synchronized (queue) {
                    if (clearMethod != null) {
                        clearMethod.invoke(queue);
                    }
                    newTasksMayBeScheduled.set(thread, false);
                    queue.notify();
                }
            } catch (Exception e) {
                logger.warn("Failed to stop timer thread {}", thread, e);
            }
        } else {
            logger.warn(
                "Couldn't stop timer thread {} as one of newTasksMayBeScheduled/queue fields are not present in the class {}",
                thread, thread
                    .getClass().getName());
        }
    }

    @Override
    public int getOrder() {
        return 400;
    }

    /**
     * returns threads running in the given in class loader context or whose class is loaded from given class loader
     */
    private List<Thread> getThreads(ClassLoader classLoader) {
        return Thread.getAllStackTraces().keySet().stream().filter(thread -> {
            return thread.getContextClassLoader() == classLoader || thread.getClass().getClassLoader() == classLoader;
        }).collect(Collectors.toList());
    }

    private boolean isAliveAndNotCurrentThread(Thread thread) {
        return thread.isAlive() && thread != Thread.currentThread();
    }

    private synchronized void join(List<Thread> threads, long millis) throws InterruptedException {
        if (millis < 0) {
            throw new IllegalArgumentException("timeout value is negative");
        } else if (millis == 0) {
            for (Thread thread : threads) {
                thread.join();
            }
        } else {

            long base = System.currentTimeMillis();
            long now = 0;

            for (Thread thread : threads) {
                while (thread.isAlive()) {
                    long delay = millis - now;
                    if (delay <= 0) {
                        break;
                    }
                    thread.join(delay);
                    now = System.currentTimeMillis() - base;
                }
            }
        }
    }
}
