package izhar.personal.com.multi_threaded_file_processing_system;

import izhar.personal.com.multi_threaded_file_processing_system.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.RestTemplateXhrTransport; // Import this
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(SecurityConfig.class) // Good, this ensures your security rules are loaded
public class StompTesting {

  @LocalServerPort
  private int port;

  @Test
  void testStomp() throws Exception {

    // --- START: Corrected Client Setup ---
    List<Transport> transports = new ArrayList<>();
    transports.add(new WebSocketTransport(new StandardWebSocketClient()));

    // Create RestTemplate with proper authentication
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor("izhar", "izhar"));
    transports.add(new RestTemplateXhrTransport(restTemplate));

    SockJsClient webSocketClient = new SockJsClient(transports);
    // --- END: Corrected Client Setup ---

    WebSocketStompClient webSocketStompClient = new WebSocketStompClient(webSocketClient);
    String URL = "ws://localhost:" + port + "/customers";

    BlockingQueue<String> blockingQueue = new LinkedBlockingQueue<>();

    // Set up proper headers with authentication
    WebSocketHttpHeaders handshakeHeaders = new WebSocketHttpHeaders();
    String auth = "izhar" + ":" + "izhar";
    String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
    handshakeHeaders.add("Authorization", "Basic " + encodedAuth);

    // Create STOMP headers
    StompHeaders connectHeaders = new StompHeaders();
    connectHeaders.add("Authorization", "Basic " + encodedAuth);

    CompletableFuture<StompSession> sessionFuture = webSocketStompClient.connectAsync(
          URL,
          handshakeHeaders,
          connectHeaders,
          new StompSessionHandlerAdapter() {
          }
    ).toCompletableFuture();

    sessionFuture.thenAccept(session -> {
      session.subscribe("/topic/log", new StompFrameHandler() {
        @Override
        public Type getPayloadType(StompHeaders headers) {
          return String.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
          blockingQueue.add((String) payload);
        }
      });
      session.send("/customers/log", "hey it's me ");
    });

    String receivedMessage = blockingQueue.poll(10, TimeUnit.SECONDS);

    assertEquals("the process started for jobhey it's me ", receivedMessage);
  }
}