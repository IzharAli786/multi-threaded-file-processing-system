package izhar.personal.com.multi_threaded_file_processing_system.service;


import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import izhar.personal.com.multi_threaded_file_processing_system.exception.ConversionTimeoutException;
import izhar.personal.com.multi_threaded_file_processing_system.exception.FileProcessingException;
import izhar.personal.com.multi_threaded_file_processing_system.exception.FileValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

@Service
public class ConvertFileAsynchronous {

    private static final  Logger logger= LoggerFactory.getLogger(ConvertFileAsynchronous.class);
    private PdfToWordConverter pdfToWordConverter = new PdfToWordConverter();
    @Value("${file.processing.timeout.conversion:30}")
    private long conversionTimeoutSeconds;

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

    @Recover
    public CompletableFuture<File> recoverFromValidationException(File file){
        String threadName = Thread.currentThread().getName();
        logger.error("retried after failure: {} on thread: {}", file.getName(), threadName);
        CompletableFuture<File> future = new CompletableFuture<>();
        future.completeExceptionally(new FileValidationException(file.getName(), "Validation", file.getName()));
        return future;
    }
}
