package scheduling;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class TiredThreadTest {


        @Test
        void constructor_setsIdAndName() {
            TiredThread t = new TiredThread(5, 1.5);

            assertEquals(5, t.getWorkerId());
            assertTrue(t.getName().startsWith("FF="));
            assertFalse(t.isBusy());
            assertEquals(0, t.getTimeUsed());
            assertTrue(t.getTimeIdle() >= 0);
        }

        @Test
        void run_executesTaskAndUpdatesTime() throws InterruptedException {
            AtomicBoolean ran = new AtomicBoolean(false);
            TiredThread t = new TiredThread(1, 1.0);

            t.start();

            t.newTask(() -> {
                ran.set(true);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {
                }
            });

            long deadline = System.currentTimeMillis() + 1000;
            while (!ran.get() && System.currentTimeMillis() < deadline) {
                Thread.sleep(5);
            }

            assertTrue(ran.get(), "task should have run");

            t.shutdown();
            t.join(1000);

            assertFalse(t.isAlive());
            assertTrue(t.getTimeUsed() > 0, "timeUsed should be > 0 after running a task");
            assertTrue(t.getTimeIdle() >= 0);
            assertFalse(t.isBusy());
        }

        @Test
        void isBusy_reflectsExecutionState() throws InterruptedException {
            AtomicBoolean insideTask = new AtomicBoolean(false);
            TiredThread t = new TiredThread(2, 1.0);
            t.start();

            t.newTask(() -> {
                insideTask.set(true);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ignored) {
                }
            });

            long deadline = System.currentTimeMillis() + 1000;
            while (!insideTask.get() && System.currentTimeMillis() < deadline) {
                Thread.sleep(5);
            }

            assertTrue(insideTask.get(), "task should have started");
            assertTrue(t.isBusy(), "thread should report busy while running task");

            t.shutdown();
            t.join(1000);

            assertFalse(t.isBusy(), "thread should not be busy after finishing");
        }

        @Test
        void shutdown_causesThreadToExit() throws InterruptedException {
            TiredThread t = new TiredThread(3, 1.0);
            t.start();

            t.shutdown();
            t.join(1000);

            assertFalse(t.isAlive(), "thread should be terminated after shutdown");
        }

        @Test
        void compareTo_comparesByFatigue() throws InterruptedException {
            TiredThread t1 = new TiredThread(1, 1.0);
            TiredThread t2 = new TiredThread(2, 1.0);

            t1.start();
            t2.start();
            t1.newTask(() -> {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {
                }
            });

            t2.newTask(() -> {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ignored) {
                }
            });

            t1.shutdown();
            t2.shutdown();
            t1.join(1000);
            t2.join(1000);

            assertTrue(t1.getFatigue() < t2.getFatigue(),
                    "t1 should be less fatigued than t2");
            assertTrue(t1.compareTo(t2) < 0);
            assertTrue(t2.compareTo(t1) > 0);
        }

        @Test
        void compareTo_sameFatigue_usesId() {
            TiredThread t1 = new TiredThread(1, 1.0);
            TiredThread t2 = new TiredThread(2, 1.0);

            assertEquals(0.0, t1.getFatigue());
            assertEquals(0.0, t2.getFatigue());

            assertTrue(t1.compareTo(t2) < 0, "smaller id should come first when fatigue is equal");
            assertTrue(t2.compareTo(t1) > 0);
        }
    }


