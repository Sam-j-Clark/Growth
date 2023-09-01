package nz.ac.canterbury.seng302.portfolio.notifications;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Websocket config.
     * This designates the /notifications/sending prefix for messages
     * that are bound for our message-handling methods
     *
     * It also sets up a broker to carry messages back to the client
     * on destinations prefixed with /notifications/receiving
     *
     * @param config A configuration.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("notifications/sending");
        config.setApplicationDestinationPrefixes("notifications/");
    }


    /**
     * This enables a fallback option in case websockets aren't available.
     *
     * @param registry A registry
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("websocket")
                .setAllowedOriginPatterns("https://*.canterbury.ac.nz")
                .withSockJS()
                .setClientLibraryUrl("../webjars/sockjs-client/dist/sockjs.min.js");
    }
}
