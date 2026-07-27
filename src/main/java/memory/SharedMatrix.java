package memory;

public class SharedMatrix {

    private volatile SharedVector[] vectors = new SharedVector[0];


    public SharedMatrix() {
        // TODO: initialize empty matrix
        this.vectors = new SharedVector[0];
    }

    public SharedMatrix(double[][] matrix) {
        // TODO: construct matrix as row-major SharedVectors
        loadRowMajor(matrix);
    }

    public void loadRowMajor(double[][] matrix) {
        // TODO: replace internal data with new row-major
        if (matrix == null)
            throw new IllegalArgumentException("matrix cannot be null");

        SharedVector[] vectors = new SharedVector[matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            double[] row = matrix[i];
            if (row == null)
                throw new IllegalArgumentException("row " + i + " is null");
            vectors[i] = new SharedVector(row.clone(), VectorOrientation.ROW_MAJOR);
        }
        this.vectors = vectors;
    }

    public void loadColumnMajor(double[][] matrix) {
        // TODO: replace internal data with new column-major matrix
        if (matrix == null)
            throw new IllegalArgumentException("matrix cannot be null");

        int rows = matrix.length;
        int cols;
        if (rows == 0) {
            this.vectors = new SharedVector[0];
            return;
        }
        else
            cols = matrix[0].length;


        for (double[] row : matrix) {
            if (row == null || row.length != cols) {
                throw new IllegalArgumentException("all rows must be non-null and same length");
            }
        }

        SharedVector[] newVectors = new SharedVector[cols];
        for (int j = 0; j < cols; j++) {
            double[] col = new double[rows];
            for (int i = 0; i < rows; i++)
                col[i] = matrix[i][j];
            newVectors[j] = new SharedVector(col, VectorOrientation.COLUMN_MAJOR);
        }
        this.vectors = newVectors;
    }

    public double[][] readRowMajor() {
        // TODO: return matrix contents as a row-major double[][]
        SharedVector[] current = this.vectors;
        if (current.length == 0) {
            return new double[0][0];
        }

        VectorOrientation orientation = getOrientation();
        int rows;
        int cols;
        if (orientation == VectorOrientation.ROW_MAJOR) {
            rows = current.length;
            cols = current[0].length();
        } else { // COLUMN_MAJOR
            cols = current.length;
            rows = current[0].length();
        }

        double[][] result = new double[rows][cols];
        acquireAllVectorReadLocks(current);
        try {
            if (orientation == VectorOrientation.ROW_MAJOR) {
                for (int r = 0; r < rows; r++) {
                    SharedVector rowVec = current[r];
                    for (int c = 0; c < cols; c++) {
                        result[r][c] = rowVec.get(c);
                    }
                }
            } else {
                for (int c = 0; c < cols; c++) {
                    SharedVector colVec = current[c];
                    for (int r = 0; r < rows; r++) {
                        result[r][c] = colVec.get(r);
                    }
                }
            }
        } finally {
            releaseAllVectorReadLocks(current);
        }

        return result;


    }

    public SharedVector get(int index) {
        // TODO: return vector at index
        return vectors[index];

    }

    public int length() {
        // TODO: return number of stored vectors
        return vectors.length;
    }

    public VectorOrientation getOrientation() {
        // TODO: return orientation
        if (vectors.length == 0) {
            return VectorOrientation.ROW_MAJOR;
        }
        return vectors[0].getOrientation();
    }

    private void acquireAllVectorReadLocks(SharedVector[] vecs) {
        // TODO: acquire read lock for each vector
        if (vecs == null)
            return;
        for (SharedVector v : vecs)
            if (v != null)
                v.readLock();
    }

    private void releaseAllVectorReadLocks(SharedVector[] vecs) {
        // TODO: release read locks
        if (vecs == null)
            return;
        for (SharedVector v : vecs)
            if (v != null)
                v.readUnlock();
    }

    private void acquireAllVectorWriteLocks(SharedVector[] vecs) {
        // TODO: acquire write lock for each vector
        if (vecs == null)
                return;
        for (SharedVector v : vecs)
            if (v != null)
                v.writeLock();
    }

    private void releaseAllVectorWriteLocks(SharedVector[] vecs) {
        // TODO: release write locks
        if (vecs == null)
            return;
        for (SharedVector v : vecs)
            if (v != null)
                v.writeUnlock();
    }
}