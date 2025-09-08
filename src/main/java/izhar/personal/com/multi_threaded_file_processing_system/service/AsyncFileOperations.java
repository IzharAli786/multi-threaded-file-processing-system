package izhar.personal.com.multi_threaded_file_processing_system.service;


import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import izhar.personal.com.multi_threaded_file_processing_system.concurrency.ProcessingContext;
import izhar.personal.com.multi_threaded_file_processing_system.exception.ConversionTimeoutException;
import izhar.personal.com.multi_threaded_file_processing_system.exception.FileProcessingException;
import izhar.personal.com.multi_threaded_file_processing_system.exception.FileValidationException;
import izhar.personal.com.multi_threaded_file_processing_system.exception.GlobalExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class AsyncFileOperations {
    @Autowired
    private GlobalExceptionHandler exceptionHandler;
    @Value("${file.processing.timeout.validation:10}")
    private long validationTimeoutSeconds;

 private static final Logger logger= LoggerFactory.getLogger(AsyncFileOperations.class);
    @Async("FileValidator")
    @Retryable(value = {IOException.class, RuntimeException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public CompletableFuture<File> validateFileWithRetry(File file) {
        String threadName = Thread.currentThread().getName();
        logger.info("Attempting file validation for: {} on thread: {}", file.getName(), threadName);

        try {
            // Directly use PerformValidationFile and apply timeout and exception handling
            return performValidationFile(file)
                    .orTimeout(validationTimeoutSeconds, TimeUnit.SECONDS)
                    .exceptionally(ex -> {
                        logger.error("Validation failed after retries for: {} - {}", file.getName(), ex.getMessage());
                        throw new FileValidationException(file.getName(), "validation", ex.getMessage());
                    });
        } catch (Exception e) {
            logger.error("Validation attempt failed for: {} - {}", file.getName(), e.getMessage());
            CompletableFuture<File> completableFuture = new CompletableFuture<>();
            completableFuture.completeExceptionally(new FileValidationException(file.getName(), "Validate", e.getMessage()));
            return completableFuture;
        }
    }

    public CompletableFuture<File> performValidationFile(File file) {
        try {
            System.out.println("the thread name in validate File is " + Thread.currentThread().getName());
            LocalDateTime startTime = LocalDateTime.now();

            if (!file.exists() || file.length() == 0) {
                throw new RuntimeException("file must exist");
            }
            if (!file.getName().endsWith(".pdf")) {
                throw new RuntimeException("the file must be a pdf ");
            }

            ProcessingContext.setStartTime(startTime);
            ProcessingContext.setOriginalFile(file);
            return CompletableFuture.completedFuture(file);
        } catch (RuntimeException e) {
            exceptionHandler.handleGenericException(e);
//            CompletableFuture<Void> completableFuture = new CompletableFuture();
            throw new RuntimeException(e);
        }
    }



}
