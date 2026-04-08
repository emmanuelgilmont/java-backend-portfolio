package be.gate25.discord.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import be.gate25.discord.service.DiscordService;

@RestController
@RequestMapping("/v1")
public class PublicController {

    private final DiscordService discordService;

    public PublicController(DiscordService discordService) {
        this.discordService = discordService;
    }

    /**
     * Sends a private message to a specified (or a list of users) user on Discord.
     *
     * @param userId the unique identifier of the Discord user to whom the message will be sent or a list of comma-separated user ids
     * @param message the content of the message to send
     * @return a ResponseEntity with an HTTP status indicating the result of the operation
     */
    @PostMapping("/discord/private")
    public ResponseEntity<Void> sendPrivateMessage(@RequestParam String userId, @RequestParam String message) {
        discordService.sendPrivateMessage(userId, message);
        return ResponseEntity.ok().build();
    }

    /**
     * Sends a public message to a pre-configured Discord channel (usually #general).
     *
     * @param message the content of the message to send
     * @return a ResponseEntity with an HTTP status indicating the result of the operation
     */
    @PostMapping("/discord/public")
    public ResponseEntity<Void> sendPublicMessage(@RequestParam String message) {
        discordService.sendPublicMessage(message);
        return ResponseEntity.ok().build();
    }
}