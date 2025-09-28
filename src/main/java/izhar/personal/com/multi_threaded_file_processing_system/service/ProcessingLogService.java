package izhar.personal.com.multi_threaded_file_processing_system.service;


import izhar.personal.com.multi_threaded_file_processing_system.entity.ProcessingLogs;
import izhar.personal.com.multi_threaded_file_processing_system.repositories.ProcessingLogsRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProcessingLogService {

  @Autowired
  private ProcessingLogsRepository processingLogsRepository;

}
