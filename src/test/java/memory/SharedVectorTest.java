package memory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SharedVectorTest {

        @Test
        void constructor_nullVector_throws() {
            assertThrows(IllegalArgumentException.class,
                    () -> new SharedVector(null, VectorOrientation.ROW_MAJOR));
        }

        @Test
        void length_returnsSize() {
            SharedVector v = new SharedVector(new double[]{1, 2, 3, 4}, VectorOrientation.ROW_MAJOR);
            assertEquals(4, v.length());
        }

        @Test
        void get_returnsCorrectElement() {
            SharedVector v = new SharedVector(new double[]{10, 20, 30}, VectorOrientation.ROW_MAJOR);
            assertEquals(10, v.get(0));
            assertEquals(20, v.get(1));
            assertEquals(30, v.get(2));
        }

        @Test
        void getOrientation_returnsGivenOrientation() {
            SharedVector v = new SharedVector(new double[]{1, 2}, VectorOrientation.ROW_MAJOR);
            assertEquals(VectorOrientation.ROW_MAJOR, v.getOrientation());
        }

        @Test
        void get_multipleReads_consistent() {
            double[] data = {5, 6, 7, 8};
            SharedVector v = new SharedVector(data, VectorOrientation.ROW_MAJOR);
            for (int i = 0; i < v.length(); i++) {
                double a = v.get(i);
                double b = v.get(i);
                assertEquals(a, b);
                assertEquals(data[i], a);
            }
        }

        @Test
        void transpose_togglesOrientation() {
            SharedVector v = new SharedVector(new double[]{1, 2, 3}, VectorOrientation.ROW_MAJOR);

            v.transpose();
            assertEquals(VectorOrientation.COLUMN_MAJOR, v.getOrientation());

            v.transpose();
            assertEquals(VectorOrientation.ROW_MAJOR, v.getOrientation());
        }


        @Test
        void add_nullOther_throws() {
            SharedVector v = new SharedVector(new double[]{1, 2}, VectorOrientation.ROW_MAJOR);
            assertThrows(IllegalArgumentException.class, () -> v.add(null));
        }

        @Test
        void add_lengthMismatch_throws() {
            SharedVector v1 = new SharedVector(new double[]{1, 2}, VectorOrientation.ROW_MAJOR);
            SharedVector v2 = new SharedVector(new double[]{1}, VectorOrientation.ROW_MAJOR);
            assertThrows(IllegalArgumentException.class, () -> v1.add(v2));
        }

        @Test
        void add_sameLength_addsComponentWise() {
            SharedVector v1 = new SharedVector(new double[]{1, 2, 3}, VectorOrientation.ROW_MAJOR);
            SharedVector v2 = new SharedVector(new double[]{10, -1, 0.5}, VectorOrientation.COLUMN_MAJOR);

            v1.add(v2);

            assertEquals(11.0, v1.get(0), 1e-9);
            assertEquals(1.0,  v1.get(1), 1e-9);
            assertEquals(3.5,  v1.get(2), 1e-9);
        }


        @Test
        void negate_flipsSigns() {
            SharedVector v = new SharedVector(new double[]{1, -2, 0}, VectorOrientation.ROW_MAJOR);
            v.negate();
            assertEquals(-1.0, v.get(0), 1e-9);
            assertEquals( 2.0, v.get(1), 1e-9);
            assertEquals(-0.0, v.get(2), 1e-9);
        }


        @Test
        void dot_nullOther_throws() {
            SharedVector v = new SharedVector(new double[]{1, 2}, VectorOrientation.ROW_MAJOR);
            assertThrows(IllegalArgumentException.class, () -> v.dot(null));
        }

        @Test
        void dot_lengthMismatch_throws() {
            SharedVector v1 = new SharedVector(new double[]{1, 2}, VectorOrientation.ROW_MAJOR);
            SharedVector v2 = new SharedVector(new double[]{1}, VectorOrientation.COLUMN_MAJOR);
            assertThrows(IllegalArgumentException.class, () -> v1.dot(v2));
        }

        @Test
        void dot_sameOrientation_throws() {
            SharedVector v1 = new SharedVector(new double[]{1, 2, 3}, VectorOrientation.ROW_MAJOR);
            SharedVector v2 = new SharedVector(new double[]{4, 5, 6}, VectorOrientation.ROW_MAJOR);
            assertThrows(IllegalArgumentException.class, () -> v1.dot(v2));
        }

        @Test
        void dot_rowTimesColumn_returnsCorrectValue() {
            SharedVector row = new SharedVector(new double[]{1, 2, 3}, VectorOrientation.ROW_MAJOR);
            SharedVector col = new SharedVector(new double[]{4, 5, 6}, VectorOrientation.COLUMN_MAJOR);
            double res = row.dot(col);
            // 1*4 + 2*5 + 3*6 = 32
            assertEquals(32.0, res, 1e-9);
        }



        @Test
        void vecMatMul_nullMatrix_throws() {
            SharedVector row = new SharedVector(new double[]{1, 2, 3}, VectorOrientation.ROW_MAJOR);
            assertThrows(IllegalArgumentException.class, () -> row.vecMatMul(null));
        }

        @Test
        void vecMatMul_notRowVector_throws() {
            SharedVector col = new SharedVector(new double[]{1, 2}, VectorOrientation.COLUMN_MAJOR);
            SharedMatrix m = new SharedMatrix(new double[][]{{1}, {2}});
            assertThrows(IllegalArgumentException.class, () -> col.vecMatMul(m));
        }

        @Test
        void vecMatMul_dimensionMismatch_throws() {
            SharedVector row = new SharedVector(new double[]{1, 2, 3}, VectorOrientation.ROW_MAJOR);
            SharedMatrix m = new SharedMatrix();
            double[][] data = {
                    {1, 2},
                    {3, 4}
            };
            m.loadColumnMajor(data); // أعمدة طولها 2، والفكتور طوله 3
            assertThrows(IllegalArgumentException.class, () -> row.vecMatMul(m));
        }

        @Test
        void vecMatMul_rowTimesMatrix_updatesVector() {
            SharedVector row = new SharedVector(new double[]{1, 2, 3}, VectorOrientation.ROW_MAJOR);


            SharedMatrix m = new SharedMatrix();
            double[][] data = {
                    {1, 4},
                    {2, 5},
                    {3, 6}
            };
            m.loadColumnMajor(data);

            row.vecMatMul(m);

            assertEquals(2, row.length());
            assertEquals(VectorOrientation.ROW_MAJOR, row.getOrientation());
            assertEquals(14.0, row.get(0), 1e-9);
            assertEquals(32.0, row.get(1), 1e-9);
        }
}
