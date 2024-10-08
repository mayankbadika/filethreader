package com.example.filethreader.service;

import com.example.filethreader.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class FileReaderService {

    @Autowired
    private ResourceLoader resourceLoader;

    // Autowire the taskExecutor bean
    @Autowired
    private Executor taskExecutor;


    /*
     * There are two scenarios when dealing with asynchronous file reading:
     *
     * 1. **Synchronous Method (`List<User>`)**:
     *    - If the `readAllUsersFromFile` method is synchronous (i.e., it returns a `List<User>` directly),
     *      this method will block the calling thread until the entire file is read.
     *    - In this case, to achieve parallel execution for multiple files, you can use
     *      `CompletableFuture.supplyAsync()` to run the file reading operation in a separate thread.
     *      This prevents the main thread from being blocked by the file I/O operation.
     *    - Example:
     *      CompletableFuture<List<User>> future = CompletableFuture.supplyAsync(() -> {
     *          return readAllUsersFromFile(path);
     *      }, executor);
     *
     * 2. **Asynchronous Method (`CompletableFuture<List<User>>`)**:
     *    - If `readAllUsersFromFile` is already asynchronous (i.e., it returns a `CompletableFuture<List<User>>`),
     *      then the file reading is already running asynchronously and does not block the main thread.
     *    - In this case, there's no need to wrap the call in `supplyAsync()`—you can just collect
     *      and work with the futures directly.
     *    - The method itself handles the asynchronous execution, so you just chain or manage the futures.
     *    - Example:
     *      CompletableFuture<List<User>> future = readAllUsersFromFile(path);
     *
     * Summary:
     * - Use `supplyAsync()` when you have a **synchronous method** to run it in a separate thread.
     * - If the method is already **asynchronous**, just handle the `CompletableFuture` directly without using `supplyAsync()`.
     */

    /*
     * This service class demonstrates the use of Spring's @Async annotation with a custom TaskExecutor.
     *
     * Key Points:
     *
     * 1. The `taskExecutor` bean is autowired to ensure that Spring injects the custom thread pool
     *    defined in the configuration.
     *
     * 2. We use `CompletableFuture.supplyAsync()` to execute the reading of each file asynchronously
     *    using the provided `taskExecutor`. This ensures that each file is processed by a different thread.
     *
     * 3. The `readMultipleFiles()` method creates multiple CompletableFuture objects, each handling
     *    the reading of a file. These are collected in a list called `futures`.
     *
     * 4. The `taskExecutor` is passed as a second argument to `supplyAsync()` so that the custom
     *    thread pool is used for file reading tasks.
     *
     * 5. Once all the files have been processed, `join()` is called on each CompletableFuture to ensure
     *    that the results are combined. `join()` blocks until the CompletableFuture completes,
     *    guaranteeing that all file reads finish before returning the combined result.
     *
     * 6. This method leverages asynchronous programming to process multiple files concurrently,
     *    improving performance when handling a large number of files.
     *
     * 7. If any exception occurs while reading a file, the method logs an error and continues with
     *    the other files, returning an empty list for the problematic file.
     */



    //@Async("taskExecutor")
    public CompletableFuture<List<User>> readMultipleFiles(String[] paths) {
        List<CompletableFuture<List<User>>> futures = new ArrayList<>();

        for (String path : paths) {
            // Submit each file read operation as a CompletableFuture
            CompletableFuture<List<User>> future =  CompletableFuture.supplyAsync(() -> {
                try {
                    return readMultipleFilesHelper(path);
                } catch (Exception e) {
                    System.err.println("NO file found for path ="+ path);
                    return new ArrayList<>();
                }
            }, taskExecutor);

            futures.add(future); // Collect the futures
        }

        // Wait for all futures to complete and combine results
        List<User> allUsers = new ArrayList<>();
        for (CompletableFuture<List<User>> future : futures) {
            allUsers.addAll(future.join()); // This will block until each CompletableFuture is complete
        }

        return CompletableFuture.completedFuture(allUsers); // Return the combined result
    }

    /*
     * In CompletableFuture, both join() and get() are used to wait for the completion
     * of the asynchronous task and retrieve the result. However, there are key differences:
     *
     * 1. Exception Handling:
     *    - get(): Throws checked exceptions, specifically InterruptedException and ExecutionException.
     *             You are forced to handle or declare these exceptions.
     *    - join(): Throws an unchecked CompletionException (which wraps the actual exception).
     *              No need to handle checked exceptions explicitly.
     *
     * 2. Blocking Behavior:
     *    - Both get() and join() block the current thread until the result is available.
     *      However, get() forces you to handle checked exceptions, while join() does not.
     *
     * 3. Use Case:
     *    - get(): Use when you want more control over exception handling with checked exceptions.
     *    - join(): Use when you prefer a simpler approach without checked exceptions.
     *
     * Example:
     * CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> 42);
     *
     * // Using get():
     * try {
     *     Integer result = future.get();  // Must handle InterruptedException, ExecutionException
     * } catch (InterruptedException | ExecutionException e) {
     *     e.printStackTrace();
     * }
     *
     * // Using join():
     * Integer result = future.join();  // No need to handle checked exceptions, might throw CompletionException
     */


    public List<User> readMultipleFilesHelper(String path) throws Exception {
        String threadName = Thread.currentThread().getName();

        // Output the thread name (you can log this or print it)
        System.out.println("Processing file: " + path + " on thread: " + threadName);
        List<User> users = new ArrayList<>();
        /*  Load the CSV file as a Resource
                The "file:" prefix specifies that we are accessing a file on the local file system.
                This tells the ResourceLoader to treat the provided path as a file system path,
                allowing it to locate and read the file correctly.
             */
        Resource resource = resourceLoader.getResource("file:" + path);

        // Create an InputStream to read raw byte data from the file
        // InputStream allows us to read data incrementally without loading the entire file into memory,
        // which is essential for handling large files efficiently.
        InputStream ir = resource.getInputStream();

        // BufferedReader reads the character data from the InputStream
        // It buffers input, making it more efficient to read text data line by line.
        // This way, we can easily handle large text files without performance issues.
        BufferedReader reader = new BufferedReader(new InputStreamReader(ir));

        String line;
        // Read the file line by line
        while ((line = reader.readLine()) != null) {
            String[] userData = line.split(",");
            if(userData[0].equals("id")) continue;
            User user = new User();

            user.setID(Integer.parseInt(userData[0]));
            user.setFirstName(userData[1]);
            user.setLastName(userData[2]);
            user.setEmail(userData[3]);
            user.setGender(userData[4]);
            user.setIpAddress(userData[5]);

            users.add(user);
        }

        reader.close(); // Close the reader to free resources

        return users;
    }


    /*
    @Async("taskExecutor")
    public CompletableFuture<List<User>> readAllUsersFromFile(String path) throws Exception {
        String threadName = Thread.currentThread().getName();

        // Output the thread name (you can log this or print it)
        System.out.println("Processing file: " + path + " on thread: " + threadName);
        List<User> users = new ArrayList<>();
        Resource resource = resourceLoader.getResource("file:" + path);

        // Create an InputStream to read raw byte data from the file
        // InputStream allows us to read data incrementally without loading the entire file into memory,
        // which is essential for handling large files efficiently.
        InputStream ir = resource.getInputStream();

        // BufferedReader reads the character data from the InputStream
        // It buffers input, making it more efficient to read text data line by line.
        // This way, we can easily handle large text files without performance issues.
        BufferedReader reader = new BufferedReader(new InputStreamReader(ir));

        String line;
        // Read the file line by line
        while ((line = reader.readLine()) != null) {
            String[] userData = line.split(",");
            if(userData[0].equals("id")) continue;
            User user = new User();

            user.setID(Integer.parseInt(userData[0]));
            user.setFirstName(userData[1]);
            user.setLastName(userData[2]);
            user.setEmail(userData[3]);
            user.setGender(userData[4]);
            user.setIpAddress(userData[5]);

            users.add(user);
        }

        reader.close(); // Close the reader to free resources

        return CompletableFuture.completedFuture(users);
    }
    */
}
