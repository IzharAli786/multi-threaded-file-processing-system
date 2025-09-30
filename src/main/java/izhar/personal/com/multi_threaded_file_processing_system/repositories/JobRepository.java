package izhar.personal.com.multi_threaded_file_processing_system.repositories;


import izhar.personal.com.multi_threaded_file_processing_system.entity.Job;
import izhar.personal.com.multi_threaded_file_processing_system.enums.Status;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends CrudRepository<Job, Long> {
  List<Job> findByStatus(Status status);

}
