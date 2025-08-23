    package izhar.personal.com.multi_threaded_file_processing_system.concurrency;
    import izhar.personal.com.multi_threaded_file_processing_system.config.CustomAsyncExceptionHandler;
    import izhar.personal.com.multi_threaded_file_processing_system.dto.ProcessingResult;
    import izhar.personal.com.multi_threaded_file_processing_system.exception.GlobalExceptionHandler;
    import izhar.personal.com.multi_threaded_file_processing_system.service.AsyncService;
    import izhar.personal.com.multi_threaded_file_processing_system.service.PdfToWordConverter;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.beans.factory.annotation.Qualifier;
    import org.springframework.scheduling.annotation.Async;
    import org.springframework.stereotype.Service;
    import java.io.File;
    import java.time.LocalDateTime;
    import java.util.concurrent.CompletableFuture;
    import java.util.concurrent.Executor;

    @Slf4j
    @Service
    public class ThreadedFileProcessor {
        @Autowired
        private GlobalExceptionHandler exceptionHandler;
        @Autowired
        private AsyncService asyncService;
        @Autowired
        private CustomAsyncExceptionHandler customAsyncExceptionHandler;
        private final PdfToWordConverter pdfToWordConverter = new PdfToWordConverter();

        // Main entry point
        public CompletableFuture<ProcessingResult> processFileAsync(File file) {
            return asyncService.validateFile(file)  // Uses @Async("fileValidator")
                    .thenCompose(asyncService::convertFile)  // Renamed method to avoid confusion
                    .thenApply(this::returnResponse)
                    .exceptionally(this::handleProcessingError);
        }
        private ProcessingResult returnResponse(File convertedFile) {
            System.out.println("Came here ");
            String threadName = Thread.currentThread().getName();
            LocalDateTime startTime = ProcessingContext.getStartTime();
            System.out.println(startTime);
            long processingTime = System.currentTimeMillis() - java.sql.Timestamp.valueOf(startTime).getTime();
            return new ProcessingResult(convertedFile.getName(), processingTime, threadName, convertedFile.length());
        }

        private ProcessingResult handleProcessingError(Throwable throwable) {
            String threadName = Thread.currentThread().getName();
            LocalDateTime startTime = ProcessingContext.getStartTime();
            String originalFileName = ProcessingContext.getOriginalFile() != null ?
                    ProcessingContext.getOriginalFile().getName() : "unknown";
            return new ProcessingResult(originalFileName, throwable.getMessage(), threadName, startTime);
        }
    }
