package scheduling;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TiredExecutor {

    private final TiredThread[] workers;
    private final PriorityBlockingQueue<TiredThread> idleMinHeap = new PriorityBlockingQueue<>();
    private final AtomicInteger inFlight = new AtomicInteger(0);

    public TiredExecutor(int numThreads) {
        // TODO
        if (numThreads <= 0) {
            throw new IllegalArgumentException("numThreads must be positive");
        }
        workers = new TiredThread[numThreads];

        for (int i = 0; i < numThreads; i++) {
            double fatigueFactor = 0.5 + Math.random();
            TiredThread worker = new TiredThread(i, fatigueFactor);
            workers[i] = worker;
            idleMinHeap.add(worker);
            worker.start();
        }
    }

    public void submit(Runnable task) {
        // TODO
        if (task == null) {
            throw new IllegalArgumentException("task cannot be null");
        }

        try {
            TiredThread worker = idleMinHeap.take();
            inFlight.incrementAndGet();
            Runnable wrapped = () -> {
                try {
                    task.run();
                } finally {
                    inFlight.decrementAndGet();
                    idleMinHeap.add(worker);
                }
            };
            worker.newTask(wrapped);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void submitAll(Iterable<Runnable> tasks) {
        // TODO: submit tasks one by one and wait until all finish
        if (tasks == null) {
            throw new IllegalArgumentException("tasks cannot be null");
        }

        for (Runnable task : tasks) {
            if (task == null) {
                throw new IllegalArgumentException("task in collection cannot be null");
            }
            submit(task);
        }

        while (inFlight.get() > 0) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

    }

    public void shutdown() throws InterruptedException {
        // TODO
        while (inFlight.get() > 0) {
            Thread.sleep(1);
        }
        for (TiredThread worker : workers)
            worker.shutdown();
        for (TiredThread worker : workers)
            worker.join();

    }

    public synchronized String getWorkerReport() {
        // TODO: return readable statistics for each worker
        StringBuilder sb = new StringBuilder();
        sb.append("TiredExecutor worker report:\n");
        for (TiredThread worker : workers) {
            sb.append(String.format("Worker %d (%s): busy=%s, fatigue=%.2f, timeUsed=%d, timeIdle=%d%n",
                    worker.getWorkerId(),
                    worker.getName(),
                    worker.isBusy(),
                    worker.getFatigue(),
                    worker.getTimeUsed(),
                    worker.getTimeIdle()
            ));
        }
        return sb.toString();
    }
}

