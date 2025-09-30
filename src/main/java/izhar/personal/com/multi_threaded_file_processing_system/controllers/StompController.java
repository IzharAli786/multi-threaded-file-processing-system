package izhar.personal.com.multi_threaded_file_processing_system.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
// ... other imports


@Controller
public class StompController {
  // ... logger and JobRepository

  // Add SimpMessagingTemplate
  @Autowired
  private SimpMessagingTemplate messagingTemplate;

  private static final Logger logger = LoggerFactory.getLogger(StompController.class);
  // ... existing getJobStatus method ...

  /**
   * This method is an example of how your internal logic (e.g., a Service)
   * would send an update to the clients. It could be in a separate service class.
   *
   * @param jobID     The ID of the job whose status has changed.
   * @param newStatus The new status of the job.
   */
  public void notifyJobStatusChange(Long jobID, String newStatus) {
    // The destination is the same one the @SendTo uses: /topic/jobStatus
    // You might want a more granular topic like /topic/jobStatus/{jobID}
    // for efficiency, but using the one you have:
    logger.info("==> Programmatically sending job status update for jobID: {} with status: {}", jobID, newStatus);

    // This sends the message payload to all subscribers of the topic.
    messagingTemplate.convertAndSend("/topic/jobStatus", newStatus);

    // Or, to send a more detailed JSON object:
    // messagingTemplate.convertAndSend("/topic/jobStatus", new JobStatusUpdate(jobID, newStatus));
  }
}