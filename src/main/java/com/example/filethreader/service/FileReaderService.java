package com.example.filethreader.service;

import com.example.filethreader.entity.DBStatus;
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
import java.util.stream.Collectors;

@Service
public class FileReaderService {

    @Autowired
    private ResourceLoader resourceLoader;

    // Autowire the taskExecutor bean
    @Autowired
    private Executor taskExecutor;

    @Autowired
    private UserService userService;


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

        /*
         * The line below converts the `futures` list into an array of `CompletableFuture` objects:
         *
         * CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
         *
         * Explanation:
         * - `futures.toArray(new CompletableFuture[0])` is a common pattern in Java used to convert a list to an array.
         * - The `0` inside `new CompletableFuture[0]` is not specifying the size of the array but is used to tell Java the **type** of the array you want (i.e., `CompletableFuture[]`).
         * - Even though the array is empty (`0`), Java will automatically determine the correct size based on the size of the `futures` list.
         * - This ensures the right type (`CompletableFuture[]`) is passed to `CompletableFuture.allOf()`, which expects an array of `CompletableFuture` objects.
         */

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        return allOf.thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream) //flatMap will flatten these lists into a single stream of User objects
                .collect(Collectors.toList()));
    }

    /*
     * Comparison of `join()` vs `allOf()` in `CompletableFuture`:
     *
     * 1. `CompletableFuture.allOf(...)`:
     *    - `allOf` is a method that accepts an array of `CompletableFuture` objects and returns a single `CompletableFuture<Void>`.
     *    - It completes when **all the futures in the array are complete**. In other words, it waits for all the given futures to finish, regardless of whether they succeed or fail.
     *    - This method is useful when you want to **wait for multiple asynchronous tasks to complete** without worrying about their individual results.
     *    - `allOf()` does not return the individual results of the futures, only a signal that all are done.
     *    - Example:
     *      CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
     *
     * 2. `CompletableFuture.join()`:
     *    - `join()` is a method that is called on a specific `CompletableFuture` to block and **wait for the future to complete**.
     *    - Once the future completes, `join()` retrieves its result. If the future completes exceptionally, `join()` will throw an unchecked exception (`CompletionException`).
     *    -  If you use join() in a loop or on individual futures, you're waiting for each result sequentially.
 *          This means you block and collect the result from one future, then move on to the next, which can slow down your overall execution when there are many tasks.
     *    - It is used to **get the result of a specific future** after it completes, and it blocks the current thread until the result is available.
     *    - Example:
     *      List<User> users = future.join();  // Waits for the future and retrieves the result.
     *
     * How they work together:
     * 1. When using `CompletableFuture.allOf()`, it is common to wait for all futures to complete, but `allOf()` does not give you their results.
     * 2. After `allOf()` completes (indicating that all futures are done), you can safely call `join()` on each future to retrieve their results without worrying about blocking because all futures are guaranteed to be done.
     * 3. This combination allows you to wait for all futures to complete and then retrieve each result using `join()`, ensuring the entire process happens efficiently.
     *
     * Summary:
     * - **`allOf`** is used to wait for all futures to complete (without getting individual results).
     * - **`join()`** is used to block the current thread until the future completes and retrieves the result.
     * - You can use both together when you want to wait for multiple asynchronous tasks to complete and then retrieve their results after the tasks are done.
     */


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

    public CompletableFuture<List<DBStatus>> readFileAndUpdateDB(String[] paths) {

        // Collect all futures into a list
        List<CompletableFuture<DBStatus>> futures = new ArrayList<>();

        for(String path : paths) {
            CompletableFuture<DBStatus> future = CompletableFuture.supplyAsync(() -> {
                try {
                   List<User> users = readMultipleFilesHelper(path);
                   boolean status = userService.saveAll(users);

                   return new DBStatus(path, status);
                } catch (Exception e) {
                    System.err.println("Exception occured for path = "+ path +" exception e = " + e);

                    return new DBStatus(path, false);
                }
            }, taskExecutor);

            futures.add(future);
        }

        CompletableFuture<Void> allOff = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        return allOff.thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList()));
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
