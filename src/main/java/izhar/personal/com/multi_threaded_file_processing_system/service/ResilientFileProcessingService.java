package izhar.personal.com.multi_threaded_file_processing_system.service;
import izhar.personal.com.multi_threaded_file_processing_system.dto.ProcessingResult;
import izhar.personal.com.multi_threaded_file_processing_system.exception.ConversionTimeoutException;
import izhar.personal.com.multi_threaded_file_processing_system.exception.FileProcessingException;
import izhar.personal.com.multi_threaded_file_processing_system.exception.FileValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
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
public class ResilientFileProcessingService {
    private static final Logger logger = LoggerFactory.getLogger(ResilientFileProcessingService.class);

    private PdfToWordConverter pdfToWordConverter = new PdfToWordConverter();

    @Value("${file.processing.timeout.validation:10}")
    private long validationTimeoutSeconds;

    @Value("${file.processing.timeout.conversion:30}")
    private long conversionTimeoutSeconds;

    @Async("FileValidator")
    @Retryable(value = {IOException.class, RuntimeException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public CompletableFuture<File> validateFileWithRetry(File file) {
        String threadName = Thread.currentThread().getName();
        logger.info("Attempting file validation for: {} on thread: {}", file.getName(), threadName);

        try {
            // Directly use PerformValidationFile and apply timeout and exception handling
            return PerformValidationFile(file)
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
    public CompletableFuture<File> PerformValidationFile(File file) {
        return CompletableFuture.completedFuture(file);
    }

    @Recover
    public CompletableFuture<File> recoverFromValidationException(File file){
        String threadName = Thread.currentThread().getName();
        logger.error("retried after failure: {} on thread: {}", file.getName(), threadName);
        CompletableFuture<File> future = new CompletableFuture<>();
        future.completeExceptionally(new FileValidationException(file.getName(), "Validation", file.getName()));
        return future;
    }
    @Async("TaskExecutor")
    @CircuitBreaker(name="fileProcessing",fallbackMethod = "fallBackConversion")
    @Retryable(value={IOException.class,RuntimeException.class},maxAttempts = 2,backoff = @Backoff(delay = 2000,multiplier = 1.5))
    public CompletableFuture<File> convertFileWithCircuitBreaker(File validatedFile ){

        logger.info("converting file {} with circuitBreaker and thread name {}", validatedFile.getName(), Thread.currentThread().getName());

         try {
                    long startTime=System.currentTimeMillis();
                    File result= pdfToWordConverter.convertToWord(validatedFile);
                    return  CompletableFuture.completedFuture(result);
                }catch (Exception e) {
                    if(e instanceof TimeoutException){
                        throw new ConversionTimeoutException(validatedFile.getName(),conversionTimeoutSeconds);
                    }
                    else{
                        throw new FileProcessingException(validatedFile.getName(), "convertFileWithCircuitBreaker", e.getMessage());
                    }
                }
        }
        public CompletableFuture<File> fallBackConversion(File validatedFile, Exception ex ){
        String threadName= Thread.currentThread().getName();
            logger.error("Circuit breaker activated for file conversion: {} on thread: {}",
                    validatedFile.getName(), threadName, ex);
            CompletableFuture<File> future = new CompletableFuture<>();
            future.completeExceptionally(
                    new FileProcessingException(validatedFile.getName(), "convert",
                            "Service temporarily unavailable due to circuit breaker. Please try again later.")
            );
            return future;

        }
        @Recover
        public CompletableFuture<File> recoverFromConversionFailure(File validatedFile, Exception ex){
            String threadName= Thread.currentThread().getName();
            logger.error("All conversion retry attempts exhausted for: {} on thread: {}",
                    validatedFile.getName(), threadName, ex);
            CompletableFuture<File >  future = new CompletableFuture<>();
            future.completeExceptionally(
                    new FileProcessingException(validatedFile.getName(),threadName,ex.getMessage())
            );
            return future;
        }
        public CompletableFuture<ProcessingResult> processFileResilient(File inputFile) {
        String threadName = Thread.currentThread().getName();
        LocalDateTime startTime = LocalDateTime.now();
        long initialTime=System.currentTimeMillis();
        logger.info("Starting resilient file processing for: {} on thread: {}",
                inputFile.getName(), threadName);

        return validateFileWithRetry(inputFile)
                .thenCompose(this::convertFileWithCircuitBreaker)
                .thenApply(convertedFile -> {
                    long processingTime = System.currentTimeMillis() -initialTime;

                    logger.info("Resilient processing completed for: {} -> {} in {}ms",
                            inputFile.getName(), convertedFile.getName(), processingTime);

                    return new ProcessingResult(
                            convertedFile.getName(),
                            processingTime,threadName, convertedFile.length()
                    );
                })
                .exceptionally(throwable -> {
                    logger.error("Resilient processing failed for: {} on thread: {}",
                            inputFile.getName(), threadName, throwable);

                    return new ProcessingResult(
                            inputFile.getName(),
                            extractMeaningfulErrorMessage(throwable),
                            threadName,
                            startTime
                    );
                });
    }
        private String extractMeaningfulErrorMessage(Throwable throwable) {
        if (throwable instanceof FileProcessingException) {
            return throwable.getMessage();
        } else if (throwable instanceof FileValidationException) {
            return throwable.getMessage();
        } else if (throwable instanceof ConversionTimeoutException) {
            return throwable.getMessage();
        } else if (throwable.getCause() instanceof FileProcessingException) {
            return throwable.getCause().getMessage();
        } else {
            return "An unexpected error occurred: " + throwable.getMessage();
        }
    }
}
