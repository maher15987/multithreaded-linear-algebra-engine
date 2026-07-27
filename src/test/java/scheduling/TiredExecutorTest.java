package scheduling;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

public class TiredExecutorTest {

        @Test
        void constructor_zeroOrNegativeThreads_throws() {
            assertThrows(IllegalArgumentException.class, () -> new TiredExecutor(0));
            assertThrows(IllegalArgumentException.class, () -> new TiredExecutor(-1));
        }

        @Test
        void constructor_positiveThreads_createsExecutor() {
            TiredExecutor exec = new TiredExecutor(3);
            AtomicBoolean ran = new AtomicBoolean(false);
            exec.submit(() -> ran.set(true));
            waitUntilTrue(ran, 1000);

            assertTrue(ran.get(), "task should have been executed by one of the workers");
        }

        @Test
        void submit_nullTask_throws() {
            TiredExecutor exec = new TiredExecutor(1);
            assertThrows(IllegalArgumentException.class, () -> exec.submit(null));
        }

        @Test
        void submit_runsTask() {
            TiredExecutor exec = new TiredExecutor(1);
            AtomicBoolean ran = new AtomicBoolean(false);

            exec.submit(() -> ran.set(true));

            waitUntilTrue(ran, 1000);

            assertTrue(ran.get(), "submitted task should run");
        }

        @Test
        void submitAll_nullIterable_throws() {
            TiredExecutor exec = new TiredExecutor(1);
            assertThrows(IllegalArgumentException.class, () -> exec.submitAll(null));
        }

        @Test
        void submitAll_withNullTask_throws() {
            TiredExecutor exec = new TiredExecutor(1);
            List<Runnable> tasks = Arrays.asList(
                    () -> {},
                    null
            );

            assertThrows(IllegalArgumentException.class, () -> exec.submitAll(tasks));
        }

        @Test
        void submitAll_runsAllTasks() {
            TiredExecutor exec = new TiredExecutor(3);
            AtomicInteger counter = new AtomicInteger(0);

            List<Runnable> tasks = Arrays.asList(
                    counter::incrementAndGet,
                    counter::incrementAndGet,
                    counter::incrementAndGet
            );

            exec.submitAll(tasks);

            long deadline = System.currentTimeMillis() + 1000;
            while (counter.get() < 3 && System.currentTimeMillis() < deadline) {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException ignored) {
                }
            }

            assertEquals(3, counter.get(), "all tasks in submitAll should eventually run");
        }


        private void waitUntilTrue(AtomicBoolean flag, long timeoutMillis) {
            long deadline = System.currentTimeMillis() + timeoutMillis;
            while (!flag.get() && System.currentTimeMillis() < deadline) {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

