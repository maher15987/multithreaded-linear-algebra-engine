package spl.lae;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions .*;


public class MainTest {


        @Test
        void main_notEnoughArgs_printsUsage() {
            String[] args = {"2", "input.json"};

            ByteArrayOutputStream errOut = new ByteArrayOutputStream();
            PrintStream originalErr = System.err;
            System.setErr(new PrintStream(errOut));

            try {
                Main.main(args);
            } catch (Exception ignored) {
            } finally {
                System.setErr(originalErr);
            }

            String err = errOut.toString();
            assertTrue(err.contains("Usage: <numThreads> <input.json> <output.json>"),
                    "should print usage when args length != 3");
        }

        @Test
        void main_nonNumericNumThreads_printsError() {
            String[] args = {"abc", "input.json", "output.json"};

            ByteArrayOutputStream errOut = new ByteArrayOutputStream();
            PrintStream originalErr = System.err;
            System.setErr(new PrintStream(errOut));

            try {
                Main.main(args);
            } catch (Exception ignored) {
            } finally {
                System.setErr(originalErr);
            }

            String err = errOut.toString();
            assertTrue(err.contains("numThreads must be a positive integer"),
                    "should print error when numThreads is not an integer");
        }

        @Test
        void main_nonPositiveNumThreads_printsError() {
            String[] args = {"0", "input.json", "output.json"};

            ByteArrayOutputStream errOut = new ByteArrayOutputStream();
            PrintStream originalErr = System.err;
            System.setErr(new PrintStream(errOut));

            try {
                Main.main(args);
            } catch (Exception ignored) {
            } finally {
                System.setErr(originalErr);
            }

            String err = errOut.toString();
            assertTrue(err.contains("numThreads must be a positive integer"),
                    "should print error when numThreads <= 0");
        }
    }

