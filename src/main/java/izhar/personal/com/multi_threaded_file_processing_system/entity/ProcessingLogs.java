package izhar.personal.com.multi_threaded_file_processing_system.entity;
import izhar.personal.com.multi_threaded_file_processing_system.enums.Status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.codehaus.plexus.classworlds.strategy.Strategy;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
@Getter @Setter @Entity
public class ProcessingLogs {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long Id;

    private String fileName;

    private LocalDateTime timeStamp;
    @CreationTimestamp
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    @Enumerated(EnumType.STRING)
    private Status status;


    @ManyToOne
    @JoinColumn(name = "job_id",nullable = false)
    private Job job;


}



