package izhar.personal.com.multi_threaded_file_processing_system.concurrency;
import izhar.personal.com.multi_threaded_file_processing_system.dto.ProcessingResult;
import izhar.personal.com.multi_threaded_file_processing_system.service.PdfToWordConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.File;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Service
public class ThreadedFileProcessor {
    @Autowired
    private  ExecutorService executorService;
    private final PdfToWordConverter pdfToWordConverter= new  PdfToWordConverter();
    public CompletableFuture<ProcessingResult> processFileAsync(File file){
        return
                CompletableFuture.supplyAsync(()->validateFile(file),executorService)
                                .thenCompose(validFile->(validateFileAsync(validFile))
                                        .thenApply(this::returnResponse)
                                        .exceptionally(this::handleProcessingError));
    }

    private ProcessingResult returnResponse(File convertedFile) {
        System.out.println("Came here ");
        String threadName = Thread.currentThread().getName();

        LocalDateTime startTime = ProcessingContext.getStartTime();
        System.out.println(startTime);

        long processingTime=System.currentTimeMillis()-java.sql.Timestamp.valueOf(startTime).getTime();


        return new ProcessingResult(convertedFile.getName(),processingTime,threadName,convertedFile.length());
    }

    private ProcessingResult handleProcessingError(Throwable throwable) {
//        assert (ProcessingContext.getStartTime()!=null);
        String threadName = Thread.currentThread().getName();
        LocalDateTime startTime = ProcessingContext.getStartTime();
        String originalFileName = ProcessingContext.getOriginalFile() != null ?
                ProcessingContext.getOriginalFile().getName() : "unknown";

        System.err.println("Error processing file: " + originalFileName +
                " on thread: " + threadName +
                " Error: " + throwable.getMessage());

        return new ProcessingResult(originalFileName, throwable.getMessage(),
                threadName, startTime);
    }



    private File validateFile(File file) {
        LocalDateTime startTime= LocalDateTime.now();
        String threadName= Thread.currentThread().getName();

        if(!file.exists() || file.length()==0){
            throw new RuntimeException("file must exist");
        }

        if(!file.getName().endsWith(".pdf")) {
            throw new RuntimeException("the file must be a pdf ");

        }
        System.out.println("Validating file: " + file.getName());
        ProcessingContext.setStartTime(startTime);
        ProcessingContext.setOriginalFile(file);
        return file;
    }

    private CompletableFuture<File> validateFileAsync(File file)

    {
        LocalDateTime startTime= LocalDateTime.now();
        String threadName= Thread.currentThread().getName();
        ProcessingContext.setStartTime(startTime);
        ProcessingContext.setOriginalFile(file);
        return  CompletableFuture.supplyAsync(()->{


            System.out.println("Converting file: " + file.getName() +
                    " on thread: " + threadName);
            try{
                return( pdfToWordConverter.convertToWord(file));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
                },executorService
        );
    }
}

