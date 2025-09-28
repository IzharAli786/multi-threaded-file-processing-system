package izhar.personal.com.multi_threaded_file_processing_system.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class StompController {

  // 1. Add this logger
  private static final Logger logger = LoggerFactory.getLogger(StompController.class);

  @MessageMapping("/log")
  @SendTo("/topic/log")
  public String processFile(String payLoad) {
    // 2. Add this log statement
    logger.info("--- CONTROLLER HIT: Received payload: '{}'", payLoad);

    String response = "the process started for job" + payLoad;

    // 3. Add this log statement
    logger.info("--- CONTROLLER RESPONDING: Sending response: '{}'", response);

    return response;
  }
}
