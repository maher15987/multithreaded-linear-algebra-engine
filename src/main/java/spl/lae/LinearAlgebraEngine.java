package spl.lae;

import parser.*;
import memory.*;
import scheduling.*;

import java.util.ArrayList;
import java.util.List;

public class LinearAlgebraEngine {

    private SharedMatrix leftMatrix = new SharedMatrix();
    private SharedMatrix rightMatrix = new SharedMatrix();
    private TiredExecutor executor;

    public LinearAlgebraEngine(int numThreads) {
        this.executor = new TiredExecutor(numThreads);
    }

    public ComputationNode run(ComputationNode computationRoot) {

        try {
            if (computationRoot.getNodeType() == ComputationNodeType.MATRIX) {
                return computationRoot;
            }

            while (computationRoot.getNodeType() != ComputationNodeType.MATRIX) {

                ComputationNode resolvableNode = computationRoot.findResolvable();

                if (resolvableNode == null) {
                    throw new IllegalStateException("No resolvable node found");
                }

                loadAndCompute(resolvableNode);
            }

            return computationRoot;

        } finally {
            try {
                executor.shutdown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void loadAndCompute(ComputationNode node) {

        ComputationNodeType type = node.getNodeType();
        List<ComputationNode> children = node.getChildren();

        if (type == ComputationNodeType.ADD) {

            if (children.size() < 2) {
                throw new IllegalArgumentException("ADD must have at least 2 operands");
            }

            double[][] acc = deepCopy(children.get(0).getMatrix());

            for (int k = 1; k < children.size(); k++) {
                double[][] b = children.get(k).getMatrix();

                if (acc.length != b.length || acc[0].length != b[0].length) {
                    throw new IllegalArgumentException("Illegal operation: dimensions mismatch");
                }

                leftMatrix.loadRowMajor(acc);
                rightMatrix.loadRowMajor(b);

                executor.submitAll(createAddTasks());
                acc = leftMatrix.readRowMajor();
            }

            node.resolve(acc);
        }

        else if (type == ComputationNodeType.MULTIPLY) {

            if (children.size() < 2) {
                throw new IllegalArgumentException("MULTIPLY must have at least 2 operands");
            }

            double[][] acc = children.get(0).getMatrix();

            for (int k = 1; k < children.size(); k++) {
                double[][] b = children.get(k).getMatrix();

                if (acc.length == 0 || b.length == 0 || acc[0].length != b.length) {
                    throw new IllegalArgumentException("Illegal operation: dimensions mismatch");
                }

                leftMatrix.loadRowMajor(acc);
                rightMatrix.loadColumnMajor(b);

                executor.submitAll(createMultiplyTasks());
                acc = leftMatrix.readRowMajor();
            }

            node.resolve(acc);
        }

        else if (type == ComputationNodeType.NEGATE) {

            if (children.size() != 1) {
                throw new IllegalArgumentException("NEGATE requires exactly 1 operand");
            }

            leftMatrix.loadRowMajor(children.get(0).getMatrix());

            executor.submitAll(createNegateTasks());
            node.resolve(leftMatrix.readRowMajor());
        }

        else if (type == ComputationNodeType.TRANSPOSE) {

            if (children.size() != 1) {
                throw new IllegalArgumentException("TRANSPOSE requires exactly 1 operand");
            }

            double[][] a = children.get(0).getMatrix();
            int rows = a.length;
            int cols = a[0].length;
            double[][] t = new double[cols][rows];

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    t[j][i] = a[i][j];
                }
            }

            node.resolve(t);
        }

        else {
            throw new IllegalArgumentException("Unsupported operation: " + type);
        }
    }

    public List<Runnable> createAddTasks() {

        int numRows = Math.min(leftMatrix.length(), rightMatrix.length());
        List<Runnable> tasks = new ArrayList<>();

        for (int i = 0; i < numRows; i++) {
            final int rowIndex = i;
            tasks.add(() -> {
                SharedVector left = leftMatrix.get(rowIndex);
                SharedVector right = rightMatrix.get(rowIndex);
                left.add(right);
            });
        }

        return tasks;
    }

    public List<Runnable> createMultiplyTasks() {

        int numRows = leftMatrix.length();
        List<Runnable> tasks = new ArrayList<>();

        for (int i = 0; i < numRows; i++) {
            final int rowIndex = i;
            tasks.add(() -> {
                SharedVector row = leftMatrix.get(rowIndex);
                row.vecMatMul(rightMatrix);
            });
        }

        return tasks;
    }

    public List<Runnable> createNegateTasks() {

        int numRows = leftMatrix.length();
        List<Runnable> tasks = new ArrayList<>();

        for (int i = 0; i < numRows; i++) {
            final int rowIndex = i;
            tasks.add(() -> {
                SharedVector v = leftMatrix.get(rowIndex);
                v.negate();
            });
        }

        return tasks;
    }

    public List<Runnable> createTransposeTasks() {

        int n = leftMatrix.length();
        List<Runnable> tasks = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            final int idx = i;
            tasks.add(() -> {
                SharedVector v = leftMatrix.get(idx);
                v.transpose();
            });
        }

        return tasks;
    }

    public String getWorkerReport() {
        return executor.getWorkerReport();
    }

    private static double[][] deepCopy(double[][] m) {
        double[][] copy = new double[m.length][m[0].length];
        for (int i = 0; i < m.length; i++) {
            System.arraycopy(m[i], 0, copy[i], 0, m[i].length);
        }
        return copy;
    }
}
