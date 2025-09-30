package izhar.personal.com.multi_threaded_file_processing_system.service;


import izhar.personal.com.multi_threaded_file_processing_system.entity.Job;
import izhar.personal.com.multi_threaded_file_processing_system.repositories.JobRepository;
import kotlin._Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

@Service
public class StompService {
  private static final Logger logger = LoggerFactory.getLogger(StompService.class);
  private Job job;

  @Autowired
  private JobService jobService;

  @Autowired
  private JobRepository jobRepository;

  @MessageMapping("customers/job/{jobID}")
  @SendTo("/topic/jobStatus")
  public String JobStatus(@DestinationVariable("jobID") long jobID) {

    job = jobRepository.findById(jobID).orElse(null);
    if (job == null) throw new IllegalArgumentException("Job Not Found");
    logger.info("the job status is {}", job.getStatus().toString());
    return job.getStatus().toString();
  }
}
