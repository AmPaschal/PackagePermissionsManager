package com.ampaschal.google.apps;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ThreadStartApp {

    public static void main(String[] args) {

        performThreadStart();
    }

    public static void performThreadStart() {
        try {
            CompletableFuture.runAsync(() -> readAndPrintFile("src/main/java/com/ampaschal/google/test-2.txt"));

            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(() -> printStackTrace("ExecutorService"));
            executorService.shutdown();

            // readAndPrintFile("src/main/java/com/ampaschal/google/test-2.txt");
            readAndPrintFileAsync("src/main/java/com/ampaschal/google/test.txt");
        } catch (IllegalThreadStateException e) {
            e.printStackTrace();
        }
    }

    public static void printStackTrace(String name) {
        System.out.println("Starting thread with " + name);
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            String methodName = element.getMethodName();
            System.out.println(className + "." + methodName + "()");
        }
    }

    public static void readAndPrintFile(String fileName) {

        String line;
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            line = reader.readLine();
            System.out.println(line);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static void readAndPrintFileAsync(String filePath) {


        // Open the file channel in asynchronous mode
        try (AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(Paths.get(filePath), StandardOpenOption.READ)) {

            // Set the position where you want to start reading from
            long position = 0;

            // Set the buffer size
            int bufferSize = 1024;
            ByteBuffer buffer = ByteBuffer.allocate(bufferSize);

            // Read asynchronously using the read() method
            Future<Integer> readFuture = fileChannel.read(buffer, position);

            // Do other work while waiting for the read operation to complete

            // Wait for the read operation to complete and get the number of bytes read
            int bytesRead = readFuture.get();

            // Display the read data
            buffer.flip(); // Prepare the buffer for reading
            byte[] data = new byte[bufferSize];
            buffer.get(data, 0, bytesRead);
            System.out.println(new String(data, "UTF-8"));

        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

    }

}
