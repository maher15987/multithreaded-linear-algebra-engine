package spl.lae;
import java.io.IOException;

import parser.*;
import java.text.ParseException;
public class Main {
    public static void main(String[] args) throws IOException {
        // TODO: main
        if (args.length != 3) {
            System.err.println("Usage: <numThreads> <input.json> <output.json>");
            return;
        }
        int numThreads;
        try {
            numThreads = Integer.parseInt(args[0]);
            if (numThreads <= 0) {
                System.err.println("numThreads must be a positive integer");
                return;
            }
        } catch (NumberFormatException e) {
            System.err.println("numThreads must be a positive integer");
            return;
        }

        String inputPath = args[1];
        String outputPath = args[2];

        InputParser parser = new InputParser();
        LinearAlgebraEngine engine = new LinearAlgebraEngine(numThreads);

        try {
            ComputationNode root = parser.parse(inputPath);

            ComputationNode resultNode = engine.run(root);

            double[][] result = resultNode.getMatrix();
            OutputWriter.write(result, outputPath);

        }  catch (ParseException | IllegalArgumentException e) {
        OutputWriter.write(e.getMessage(), outputPath);

        } catch (RuntimeException e) {
            String msg = (e.getMessage() == null)
                    ? "Unexpected error"
                    : e.getMessage();
            OutputWriter.write(msg, outputPath);
        }
    }
}