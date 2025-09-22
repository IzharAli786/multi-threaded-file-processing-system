package izhar.personal.com.multi_threaded_file_processing_system.service;

import izhar.personal.com.multi_threaded_file_processing_system.entity.Job;
import izhar.personal.com.multi_threaded_file_processing_system.enums.Status;
import izhar.personal.com.multi_threaded_file_processing_system.repositories.JobRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JobService {

  @Autowired
  private JobRepository jobRepository;


  @Transactional
  public Job createJob(String jobName, Status status) {
    Job job = new Job();
    job.setName(jobName);
    job.setStatus(status);
    jobRepository.save(job);
    return job;

  }

}
