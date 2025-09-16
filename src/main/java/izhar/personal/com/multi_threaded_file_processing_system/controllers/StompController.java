package izhar.personal.com.multi_threaded_file_processing_system.controllers;


import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller

public class StompController {

  @MessageMapping("/log")
  @SendTo("/topic/log")
  public String processFile(String payLoad) {
    return "the process started for job" + payLoad;
  }


}
