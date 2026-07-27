package memory;

import java.util.concurrent.locks.ReadWriteLock;

public class SharedVector {

    private double[] vector;
    private VectorOrientation orientation;
    private ReadWriteLock lock = new java.util.concurrent.locks.ReentrantReadWriteLock();

    public SharedVector(double[] vector, VectorOrientation orientation) {
        // TODO: store vector data and its orientation
        if (vector == null)
            throw new IllegalArgumentException("vector cannot be null");
        this.vector = vector;
        this.orientation = orientation;
    }

    public double get(int index) {
        // TODO: return element at index (read-locked)
        readLock();
        try {
            return vector[index];
        } finally {
            readUnlock();
        }
    }

    public int length() {
        // TODO: return vector length
        return vector.length;
    }

    public VectorOrientation getOrientation() {
        // TODO: return vector orientation
        return orientation;
    }

    public void writeLock() {
        // TODO: acquire write lock
        lock.writeLock().lock();
    }

    public void writeUnlock() {
        // TODO: release write lock
        lock.writeLock().unlock();
    }

    public void readLock() {
        // TODO: acquire read lock
        lock.readLock().lock();
    }

    public void readUnlock() {
        // TODO: release read lock
        lock.readLock().unlock();
    }

    public void transpose() {
        // TODO: transpose vector
        writeLock();
        try {
            if (orientation == VectorOrientation.ROW_MAJOR) {
                orientation = VectorOrientation.COLUMN_MAJOR;
            } else {
                orientation = VectorOrientation.ROW_MAJOR;
            }
        } finally {
            writeUnlock();
        }
    }

    public void add(SharedVector other) {
        // TODO: add two vectors
        if (other == null) {
            throw new IllegalArgumentException("other vector cannot be null");
        }
        if (this.length() != other.length()) {
            throw new IllegalArgumentException("vectors must have the same length");
        }
        writeLock();
        other.readLock();
        try {
            for (int i = 0; i < vector.length; i++)
                vector[i] = vector[i] + other.vector[i];
        } finally {
            other.readUnlock();
            writeUnlock();
        }
    }

    public void negate() {
        // TODO: negate vector
        writeLock();
        try {
            for (int i = 0; i < vector.length; i++) {
                vector[i] = -vector[i];
            }
        } finally {
            writeUnlock();
        }
    }

    public double dot(SharedVector other) {
        // TODO: compute dot product (row · column)
        if (other == null) {
            throw new IllegalArgumentException("other vector cannot be null");
        }
        if (this.length() != other.length()) {
            throw new IllegalArgumentException("vectors must have the same length");
        }
        if (this.getOrientation() == other.getOrientation()) {
            throw new IllegalArgumentException("Dot product requires one row vector and one column vector");
        }

        double ans = 0;
        readLock();
        other.readLock();
        try {
            for (int i = 0; i < length(); i++) {
                ans = ans + this.vector[i] * other.vector[i];
            }

        } finally {
            other.readUnlock();
            readUnlock();
        }
        return ans;
    }




    public void vecMatMul(SharedMatrix matrix) {
        // TODO: compute row-vector × matrix
            if (matrix == null) {
                throw new IllegalArgumentException("matrix cannot be null");
            }
            if (this.getOrientation() != VectorOrientation.ROW_MAJOR) {
                throw new IllegalArgumentException("Vector must be row-major for vec-mat multiplication.");
            }

            double[] result = new double[matrix.length()];

            this.readLock();
            try {
                for (int j = 0; j < matrix.length(); j++) {
                    SharedVector colVec = matrix.get(j);
                    colVec.readLock();
                    try {
                        if (colVec.vector.length != this.vector.length) {
                            throw new IllegalArgumentException("dimension mismatch in vecMatMul");
                        }

                        double sum = 0;
                        for (int i = 0; i < this.vector.length; i++) {
                            sum =  sum + this.vector[i] * colVec.vector[i];
                        }
                        result[j] = sum;
                    } finally {
                        colVec.readUnlock();
                    }
                }
            } finally {
                this.readUnlock();
            }

            this.writeLock();
            try {
                this.vector = result;
                this.orientation = VectorOrientation.ROW_MAJOR;
            } finally {
                this.writeUnlock();
            }
        }

}
