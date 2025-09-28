package izhar.personal.com.multi_threaded_file_processing_system.service;

import izhar.personal.com.multi_threaded_file_processing_system.dto.ProcessingResult;
import izhar.personal.com.multi_threaded_file_processing_system.entity.Job;
import izhar.personal.com.multi_threaded_file_processing_system.entity.ProcessingLogs;
import izhar.personal.com.multi_threaded_file_processing_system.enums.Status;
import izhar.personal.com.multi_threaded_file_processing_system.exception.ConversionTimeoutException;
import izhar.personal.com.multi_threaded_file_processing_system.exception.FileProcessingException;
import izhar.personal.com.multi_threaded_file_processing_system.exception.FileValidationException;
import izhar.personal.com.multi_threaded_file_processing_system.repositories.JobRepository;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.userdetails.UserDetailsService;
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
  @Autowired
  private ConvertFileAsynchronous convertFileAsynchronous;


  @Autowired
  private AsyncFileOperations asyncFileOperations;


  @Value("${file.processing.timeout.validation:10}")
  private long validationTimeoutSeconds;
  @Autowired
  private LogService.AsyncServices asyncServices;


  @Autowired
  private SessionFactory sessionFactory;
  @Autowired
  private UserDetailsService userDetailsService;


  @Recover
  public CompletableFuture<File> recoverFromValidationException(File file) {
    String threadName = Thread.currentThread().getName();
    logger.error("retried after failure: {} on thread: {}", file.getName(), threadName);
    CompletableFuture<File> future = new CompletableFuture<>();
    future.completeExceptionally(new FileValidationException(file.getName(), "Validation", file.getName()));
    return future;
  }

  public CompletableFuture<ProcessingResult> processFileResilient(File inputFile, Job job, ProcessingLogs processingLogs) {

    job.setStatus(Status.PROCESSING);
    processingLogs.setStatus(Status.PROCESSING);
    logger.info("the job is {}", job);
    logger.info("the processingLog name  in the resilient file {}  ", processingLogs.getFileName());
    String threadName = Thread.currentThread().getName();
    LocalDateTime startTime = LocalDateTime.now();
    long initialTime = System.currentTimeMillis();
    logger.info("Starting resilient file processing for: {} on thread: {}",
          inputFile.getName(), threadName);

    return asyncFileOperations.validateFileWithRetry(inputFile)
          .thenCompose(validatedFile -> convertFileAsynchronous.convertFileWithCircuitBreaker(validatedFile))
          .thenApply(convertedFile -> {
            long processingTime = System.currentTimeMillis() - initialTime;

            logger.info("Resilient processing completed for: {} -> {} in {}ms",
                  inputFile.getName(), convertedFile.getName(), processingTime);

            return new ProcessingResult(
                  convertedFile.getName(),
                  processingTime, threadName, convertedFile.length()
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
