package izhar.personal.com.multi_threaded_file_processing_system;

import izhar.personal.com.multi_threaded_file_processing_system.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
// Import the PasswordEncoder interface and implementation
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.RestTemplateXhrTransport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CountDownLatch;

import org.springframework.boot.test.context.TestConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({SecurityConfig.class, StompTesting.TestUserConfiguration.class})
class StompTesting {

  @LocalServerPort
  private int port;

  /**
   * FIX: Defines a PasswordEncoder and a specific UserDetailsService
   * using the same encoder expected by the application context (BCrypt).
   */
  @TestConfiguration
  static class TestUserConfiguration {

    // 1. Define the PasswordEncoder
    @Bean
    public PasswordEncoder passwordEncoder() {
      // Use BCryptPasswordEncoder, the Spring Boot 3.x default
      return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
      // 2. Define the hardcoded user, encoding the test password
      UserDetails user = User.builder()
            .username("izhar")
            // Encode the password 'izhar' using the BCrypt encoder
            .password(passwordEncoder.encode("izhar"))
            .roles("USER")
            .build();
      return new InMemoryUserDetailsManager(user);
    }
  }


  // --- Custom Stomp Handler for Test Synchronization ---
  private class TestStompSessionHandler extends StompSessionHandlerAdapter {
    private final BlockingQueue<StompSession> sessionQueue = new LinkedBlockingQueue<>();
    private final String username;
    private final String password;

    public TestStompSessionHandler(String username, String password) {
      this.username = username;
      this.password = password;
    }

    // Capture the connected session
    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
      sessionQueue.add(session);
    }

    /**
     * Adds authentication credentials to the STOMP CONNECT frame.
     */

    public StompHeaders getConnectHeaders() {
      StompHeaders headers = new StompHeaders();
      String auth = username + ":" + password;
      // Manually Base64 encode the username:password string
      String encodedAuth = java.util.Base64.getEncoder().encodeToString(auth.getBytes());
      // Add the Authorization header to the STOMP CONNECT frame
      headers.add("Authorization", "Basic " + encodedAuth);
      return headers;
    }

    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
      System.err.println("STOMP Exception: " + exception.getMessage());
      exception.printStackTrace();
    }

    @Override
    public void handleTransportError(StompSession session, Throwable exception) {
      System.err.println("STOMP Transport Error: " + exception.getMessage());
      exception.printStackTrace();
    }

    // Helper to block until session is connected
    public StompSession waitForSession(long timeout, TimeUnit unit) throws InterruptedException {
      return sessionQueue.poll(timeout, unit);
    }
  }

  @Test
  void testStomp() throws Exception {
    String username = "izhar";
    String password = "izhar";
    String clientUrl = "ws://localhost:" + port + "/customers";

    // 1. Setup SockJS Transports with Basic Auth Interceptor for HTTP fallback
    List<Transport> transports = new ArrayList<>();
    transports.add(new WebSocketTransport(new StandardWebSocketClient()));

    // RestTemplate for SockJS XHR fallbacks must carry auth credentials
    RestTemplate restTemplate = new RestTemplate();
    // This interceptor sends the Base64 encoded Basic Auth header for the initial HTTP requests
    restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(username, password));
    transports.add(new RestTemplateXhrTransport(restTemplate));

    SockJsClient webSocketClient = new SockJsClient(transports);
    WebSocketStompClient webSocketStompClient = new WebSocketStompClient(webSocketClient);

    // FIX: Set proper message converters to handle String payloads
    List<org.springframework.messaging.converter.MessageConverter> converters = new ArrayList<>();
    converters.add(new StringMessageConverter());
    converters.add(new MappingJackson2MessageConverter());
    webSocketStompClient.setMessageConverter(new org.springframework.messaging.converter.CompositeMessageConverter(converters));

    BlockingQueue<String> blockingQueue = new LinkedBlockingQueue<>();
    CountDownLatch latch = new CountDownLatch(1);

    // 2. Custom Handler handles connection and STOMP CONNECT frame authentication
    TestStompSessionHandler sessionHandler = new TestStompSessionHandler(username, password);

    webSocketStompClient.connectAsync(clientUrl, sessionHandler);

    // 3. Wait for the session to be established before proceeding
    StompSession session = sessionHandler.waitForSession(5, TimeUnit.SECONDS);

    if (session == null || !session.isConnected()) {
      throw new RuntimeException("Failed to connect STOMP session within timeout. Check logs for authentication or connection errors.");
    }

    // 4. Subscribe and Send Message
    session.subscribe("/topic/log", new StompFrameHandler() {
      @Override
      public Type getPayloadType(StompHeaders headers) {
        return String.class;
      }

      @Override
      public void handleFrame(StompHeaders headers, Object payload) {
        System.out.println("Received message: " + payload);
        blockingQueue.add((String) payload);
        latch.countDown(); // Signal that the message was received
      }
    });

    String payloadToSend = "hey it's me ";

    // FIX: Send the String payload directly - the message converter will handle it properly
    session.send("/customers/log", payloadToSend);

    // 5. Poll the queue and wait for the Latch
    boolean messageReceived = latch.await(10, TimeUnit.SECONDS);

    if (!messageReceived) {
      throw new RuntimeException("No message received within timeout period");
    }

    String receivedMessage = blockingQueue.poll();

    // 6. Assertions
    String expectedMessage = "the process started for job" + payloadToSend;
    assertEquals(expectedMessage, receivedMessage);

    System.out.println("✅ STOMP Test Passed. Received: " + receivedMessage);

    // Clean up
    session.disconnect();
  }
}