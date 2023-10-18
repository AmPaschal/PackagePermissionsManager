package com.ampaschal.google.apps;

import com.ampaschal.google.PermissionsManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Paths;
import java.nio.file.ReadOnlyFileSystemException;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadStartApp {

    public static void main(String[] args) {

        PermissionsManager.setup();

        performThreadStart();
    }

    public static void performThreadStart() {
        try {
            CompletableFuture.runAsync(() -> printStackTrace("CompletableFutures"));

            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(() -> printStackTrace("ExecutorService"));
            executorService.shutdown();

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

    public static void readAndPrintFileAsync(String filePath) {
        try {

            System.out.println("Starting File Read");
            AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(Paths.get(filePath), StandardOpenOption.READ);

            int bufferSize = 1024;
            ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
            final long[] position = {0}; // Starting position

            CompletableFuture<Void> future = new CompletableFuture<>();

            // Start reading asynchronously
            fileChannel.read(buffer, position[0], null, new CompletionHandler<Integer, Void>() {
                @Override
                public void completed(Integer bytesRead, Void attachment) {
                    if (bytesRead == -1) {
                        // End of file
                        future.complete(null);
                    } else {
                        buffer.flip();
                        while (buffer.hasRemaining()) {
                            System.out.print((char) buffer.get());
                        }
                        buffer.clear();
                        position[0] += bytesRead;
                        // Continue reading
                        fileChannel.read(buffer, position[0], null, this);
                    }
                }

                @Override
                public void failed(Throwable exc, Void attachment) {
                    future.completeExceptionally(exc);
                }
            });

            future.get(); // Wait for the reading to complete

            fileChannel.close();
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

}
