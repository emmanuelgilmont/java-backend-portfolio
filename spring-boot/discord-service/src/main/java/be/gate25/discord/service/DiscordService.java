package be.gate25.discord.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DiscordService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${discord.token}")
    private String             TOKEN;
    @Value("${discord.channel-id}")
    private String             CHANNEL_ID; // usually #general

    /**
     * Sends a public message to a pre-configured Discord channel.
     *
     * @param message the content of the message to be sent to the public Discord channel
     */
    public void sendPublicMessage(String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bot " + TOKEN);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = Map.of("content", message);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        restTemplate.postForEntity("https://discord.com/api/v10/channels/" + CHANNEL_ID + "/messages", request, String.class);
    }

    /**
     * Sends a private message to a specific user on Discord via direct messaging (DM).
     * This method first opens a direct message channel with the specified user and
     * then sends the provided message through that channel.
     *
     * @param userId  the unique identifier of the user to whom the message will be sent or a list of comma-separated user ids
     * @param message the content of the private message to be sent
     */
    public void sendPrivateMessage(String userId, String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bot " + TOKEN);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String[] users = userId.split(",");
        for (String user : users) {
            // Step 1: open the direct message channel
            Map<String, String> dmBody = Map.of("recipient_id", user.trim());
            HttpEntity<Map<String, String>> dmRequest = new HttpEntity<>(dmBody, headers);

            ResponseEntity<Map> dmResponse = restTemplate
                .postForEntity("https://discord.com/api/v10/users/@me/channels", dmRequest, Map.class);

            String dmChannelId = (String) dmResponse.getBody().get("id");

            // Step 2: send the message
            Map<String, String> msgBody = Map.of("content", message);
            HttpEntity<Map<String, String>> msgRequest = new HttpEntity<>(msgBody, headers);

            restTemplate
                .postForEntity("https://discord.com/api/v10/channels/" + dmChannelId + "/messages", msgRequest, String.class);
        }
    }
}