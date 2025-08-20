package izhar.personal.com.multi_threaded_file_processing_system.concurrency;
import izhar.personal.com.multi_threaded_file_processing_system.dto.ProcessingResult;
import izhar.personal.com.multi_threaded_file_processing_system.service.PdfToWordConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
@Service
public class ThreadedFileProcessor {
    @Autowired
    private  ExecutorService executorService;
    private final PdfToWordConverter pdfToWordConverter= new  PdfToWordConverter();
    public Future<ProcessingResult> processFileAsync (File file){
        return executorService.submit(new FileProcessingTask(file));
    }
    private class FileProcessingTask implements Callable<ProcessingResult> {
        private File file;
        public FileProcessingTask(File file){
            this.file = file;
        }
        @Override
        public ProcessingResult call() throws Exception {
            long startTime=System.currentTimeMillis();
            String threadName = Thread.currentThread().getName();
            try{
                System.out.println("Starting Process of "+file.getName()+" on Thread:"+threadName);
                File Result= pdfToWordConverter.convertToWord(file);
                long processingTime=System.currentTimeMillis()-startTime;
                System.out.println("Completed processing of " + file.getName() +
                        " in " + processingTime + "ms on thread: " + threadName);
                return new ProcessingResult(file.getName(), processingTime, threadName);
            }catch(Exception e){
                System.err.println("Error processing " + file.getName() +
                        " on thread " + threadName + ": " + e.getMessage());
                return new ProcessingResult(file.getName(), e.getMessage(), threadName);
            }
        }
    }
}

