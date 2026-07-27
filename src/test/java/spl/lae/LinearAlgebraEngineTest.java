package spl.lae;
import memory.SharedMatrix;
import memory.SharedVector;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class LinearAlgebraEngineTest {

        @Test
        void constructor_zeroThreads_throws() {
            assertThrows(IllegalArgumentException.class,
                    () -> new LinearAlgebraEngine(0));
        }

        @Test
        void constructor_positiveThreads_createsEngine() {
            LinearAlgebraEngine eng = new LinearAlgebraEngine(2);
            assertNotNull(eng);
        }


        @Test
        void createAddTasks_rowWiseAddition() throws Exception {
            LinearAlgebraEngine eng = new LinearAlgebraEngine(1);

            double[][] a = {
                    {1, 2},
                    {3, 4}
            };
            double[][] b = {
                    {10, 20},
                    {30, 40}
            };
            double[][] expected = {
                    {11, 22},
                    {33, 44}
            };

            SharedMatrix left = new SharedMatrix(a);
            SharedMatrix right = new SharedMatrix(b);
            setField(eng, "leftMatrix", left);
            setField(eng, "rightMatrix", right);

            List<Runnable> tasks = eng.createAddTasks();
            assertEquals(2, tasks.size(), "one task per row expected");

            for (Runnable r : tasks) {
                r.run();
            }

            SharedMatrix resultMatrix = (SharedMatrix) getField(eng, "leftMatrix");
            double[][] result = resultMatrix.readRowMajor();

            assertEquals(expected.length, result.length);
            for (int i = 0; i < expected.length; i++) {
                assertArrayEquals(expected[i], result[i], 1e-9);
            }
        }


        @Test
        void createNegateTasks_negatesRows() throws Exception {
            LinearAlgebraEngine eng = new LinearAlgebraEngine(1);

            double[][] a = {
                    {1, -2},
                    {0, 5}
            };
            double[][] expected = {
                    {-1, 2},
                    {0, -5}
            };

            SharedMatrix left = new SharedMatrix(a);
            setField(eng, "leftMatrix", left);

            List<Runnable> tasks = eng.createNegateTasks();
            assertEquals(2, tasks.size());

            for (Runnable r : tasks) {
                r.run();
            }

            SharedMatrix resultMatrix = (SharedMatrix) getField(eng, "leftMatrix");
            double[][] result = resultMatrix.readRowMajor();

            assertEquals(expected.length, result.length);
            for (int i = 0; i < expected.length; i++) {
                assertArrayEquals(expected[i], result[i], 1e-9);
            }
        }


        @Test
        void createTransposeTasks_togglesOrientationOfRows() throws Exception {
            LinearAlgebraEngine eng = new LinearAlgebraEngine(1);

            double[][] a = {
                    {1, 2, 3},
                    {4, 5, 6}
            };

            SharedMatrix left = new SharedMatrix(a);
            setField(eng, "leftMatrix", left);

            List<Runnable> tasks = eng.createTransposeTasks();
            assertEquals(2, tasks.size());

            for (Runnable r : tasks) {
                r.run();
            }

            SharedMatrix resultMatrix = (SharedMatrix) getField(eng, "leftMatrix");
            // كل صف أصبح COLUMN_MAJOR حسب منطق SharedVector.transpose
            for (int i = 0; i < resultMatrix.length(); i++) {
                SharedVector v = resultMatrix.get(i);
                assertEquals(memory.VectorOrientation.COLUMN_MAJOR, v.getOrientation());
            }
        }


        @Test
        void createMultiplyTasks_rowTimesMatrix() throws Exception {
            LinearAlgebraEngine eng = new LinearAlgebraEngine(1);

            // A: 2x3
            double[][] a = {
                    {1, 2, 3},
                    {4, 5, 6}
            };
            // B: 3x2
            double[][] b = {
                    {1, 4},
                    {2, 5},
                    {3, 6}
            };

            double[][] expected = {
                    {14, 32},
                    {32, 77}
            };

            SharedMatrix left = new SharedMatrix(a);
            SharedMatrix right = new SharedMatrix();
            right.loadColumnMajor(b);

            setField(eng, "leftMatrix", left);
            setField(eng, "rightMatrix", right);

            List<Runnable> tasks = eng.createMultiplyTasks();
            assertEquals(2, tasks.size(), "one task per row of left matrix");

            for (Runnable r : tasks) {
                r.run();
            }

            SharedMatrix resultMatrix = (SharedMatrix) getField(eng, "leftMatrix");
            double[][] result = resultMatrix.readRowMajor();

            assertEquals(expected.length, result.length);
            for (int i = 0; i < expected.length; i++) {
                assertArrayEquals(expected[i], result[i], 1e-9);
            }
        }


        private void setField(Object target, String fieldName, Object value) throws Exception {
            Field f = LinearAlgebraEngine.class.getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        }

        private Object getField(Object target, String fieldName) throws Exception {
            Field f = LinearAlgebraEngine.class.getDeclaredField(fieldName);
            f.setAccessible(true);
            return f.get(target);
        }
    }

