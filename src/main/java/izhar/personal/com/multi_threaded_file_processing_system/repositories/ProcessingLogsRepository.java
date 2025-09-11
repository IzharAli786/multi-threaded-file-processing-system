package izhar.personal.com.multi_threaded_file_processing_system.repositories;


import izhar.personal.com.multi_threaded_file_processing_system.entity.Job;
import izhar.personal.com.multi_threaded_file_processing_system.entity.ProcessingLogs;
import izhar.personal.com.multi_threaded_file_processing_system.enums.Status;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.util.List;
@Repository
public interface ProcessingLogsRepository extends CrudRepository<ProcessingLogs,Long> {
    List<ProcessingLogs> findByJob(Job job);

    List<ProcessingLogs> findByFileName(String fileName);
    List<ProcessingLogs> findByStatus(Status status);
}
