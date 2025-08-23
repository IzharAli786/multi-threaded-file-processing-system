package izhar.personal.com.multi_threaded_file_processing_system.service;

import izhar.personal.com.multi_threaded_file_processing_system.concurrency.ProcessingContext;
import izhar.personal.com.multi_threaded_file_processing_system.exception.GlobalExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class AsyncService {

    @Autowired
    @Qualifier("TaskExecutor")
    private Executor taskExecutor;
    @Autowired
    private GlobalExceptionHandler exceptionHandler;
     PdfToWordConverter  pdfToWordConverter= new  PdfToWordConverter() ;
    @Async("FileValidator")
    public CompletableFuture<File> validateFile(File file) {
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

    @Async("TaskExecutor")
    public CompletableFuture<File> convertFile(File file) {
        return CompletableFuture.supplyAsync(() -> {
            LocalDateTime startTime = LocalDateTime.now();
            String threadName = Thread.currentThread().getName();
            System.out.println("Converting file: " + file.getName() + " on thread: " + threadName);

            ProcessingContext.setStartTime(startTime);
            ProcessingContext.setOriginalFile(file);

            try {
                return pdfToWordConverter.convertToWord(file);
            } catch (Exception e) {
                exceptionHandler.handleGenericException(e);
                throw new RuntimeException(e);
            }
        },taskExecutor);  // Using taskExecutor explicitly
    }
}
