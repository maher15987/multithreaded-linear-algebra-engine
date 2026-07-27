package memory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SharedMatrixTest {

        @Test
        void constructor_empty_createsEmptyMatrix() {
            SharedMatrix m = new SharedMatrix();

            assertEquals(0, m.length());
            assertEquals(VectorOrientation.ROW_MAJOR, m.getOrientation());

            double[][] rows = m.readRowMajor();
            assertNotNull(rows);
            assertEquals(0, rows.length);
        }

        @Test
        void constructor_rowMajor_storesData() {
            double[][] data = {
                    {1, 2, 3},
                    {4, 5, 6}
            };

            SharedMatrix m = new SharedMatrix(data);

            assertEquals(2, m.length());
            assertEquals(VectorOrientation.ROW_MAJOR, m.getOrientation());

            double[][] res = m.readRowMajor();
            assertEquals(data.length, res.length);
            for (int r = 0; r < data.length; r++) {
                assertArrayEquals(data[r], res[r], 1e-9);
            }
        }

        @Test
        void loadRowMajor_null_throws() {
            SharedMatrix m = new SharedMatrix();
            assertThrows(IllegalArgumentException.class, () -> m.loadRowMajor(null));
        }

        @Test
        void loadRowMajor_rowNull_throws() {
            SharedMatrix m = new SharedMatrix();
            double[][] data = {
                    {1, 2},
                    null
            };

            assertThrows(IllegalArgumentException.class, () -> m.loadRowMajor(data));
        }

        @Test
        void loadColumnMajor_null_throws() {
            SharedMatrix m = new SharedMatrix();
            assertThrows(IllegalArgumentException.class, () -> m.loadColumnMajor(null));
        }

        @Test
        void loadColumnMajor_badRowLengths_throws() {
            SharedMatrix m = new SharedMatrix();
            double[][] data = {
                    {1, 2},
                    {3}
            };

            assertThrows(IllegalArgumentException.class, () -> m.loadColumnMajor(data));
        }

        @Test
        void loadColumnMajor_empty_createsEmptyMatrix() {
            SharedMatrix m = new SharedMatrix();
            double[][] data = new double[0][0];

            m.loadColumnMajor(data);

            assertEquals(0, m.length());
            double[][] res = m.readRowMajor();
            assertNotNull(res);
            assertEquals(0, res.length);
        }

        @Test
        void readRowMajor_afterColumnMajor_returnsOriginal() {
            SharedMatrix m = new SharedMatrix();
            double[][] data = {
                    {1, 2, 3},
                    {4, 5, 6},
                    {7, 8, 9}
            };

            m.loadColumnMajor(data);

            assertEquals(VectorOrientation.COLUMN_MAJOR, m.getOrientation());

            double[][] res = m.readRowMajor();
            assertEquals(data.length, res.length);
            for (int r = 0; r < data.length; r++) {
                assertArrayEquals(data[r], res[r], 1e-9);
            }
        }

        @Test
        void get_returnsVectorAtIndex() {
            double[][] data = {
                    {10, 20},
                    {30, 40}
            };
            SharedMatrix m = new SharedMatrix(data);

            SharedVector v0 = m.get(0);
            SharedVector v1 = m.get(1);

            assertNotNull(v0);
            assertNotNull(v1);
            assertEquals(2, v0.length());
            assertEquals(2, v1.length());
            assertEquals(10, v0.get(0));
            assertEquals(20, v0.get(1));
            assertEquals(30, v1.get(0));
            assertEquals(40, v1.get(1));
        }

        @Test
        void length_returnsNumberOfVectors() {
            double[][] data = {
                    {1, 2},
                    {3, 4},
                    {5, 6}
            };
            SharedMatrix m = new SharedMatrix(data);

            assertEquals(3, m.length());
        }

        @Test
        void getOrientation_matchesVectorsAfterLoads() {
            double[][] data = {
                    {1, 2},
                    {3, 4}
            };
            SharedMatrix m = new SharedMatrix();

            m.loadRowMajor(data);
            assertEquals(VectorOrientation.ROW_MAJOR, m.getOrientation());
            assertEquals(VectorOrientation.ROW_MAJOR, m.get(0).getOrientation());

            m.loadColumnMajor(data);
            assertEquals(VectorOrientation.COLUMN_MAJOR, m.getOrientation());
            assertEquals(VectorOrientation.COLUMN_MAJOR, m.get(0).getOrientation());
        }
    }


