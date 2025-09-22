package izhar.personal.com.multi_threaded_file_processing_system.entity;


import izhar.personal.com.multi_threaded_file_processing_system.dto.ProcessingResult;
import izhar.personal.com.multi_threaded_file_processing_system.enums.Status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
public class Job {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  @Enumerated(EnumType.STRING)
  private Status status;
  @CreationTimestamp

  private LocalDateTime createdAt;
  private LocalDateTime startedAt;
  private LocalDateTime completedAt;
  private String name;


  //mappedBy must be equal to the instance name of this class in the ProcessingLogs
  @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ProcessingLogs> processingLogs = new ArrayList<>();

  public void addLog(ProcessingLogs log) {
    processingLogs.add(log);
    log.setJob(this);
  }
}


