package izhar.personal.com.multi_threaded_file_processing_system.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessingResult {
    private String fileName;
    private boolean success;
    private String errorMessage;
    private long processingTimeMs;
    private String threadName;
    private double fileSize;
    private LocalDateTime startTime;
    //constructor for Success
    public ProcessingResult(String fileName,long processingTimeMs,String ThreadName,double fileSize)
    {
        this.fileSize=fileSize;
        this.fileName = fileName;
        this.processingTimeMs = processingTimeMs;
        this.threadName = ThreadName;
        this.success=true;
    }
    //Constructor for Failure
    public ProcessingResult(String fileName, String errorMessage, String ThreadName, LocalDateTime localDateTime){
        this.fileName = fileName;
        this.errorMessage = errorMessage;
        this.threadName = ThreadName;
        this.startTime=localDateTime;
    }

}
