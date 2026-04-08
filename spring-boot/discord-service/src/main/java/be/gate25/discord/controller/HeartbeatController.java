package be.gate25.discord.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.gate25.discord.service.DiscordService;

@RestController
@RequestMapping("/v1")
public class HeartbeatController {

    private final String         adminUser;
    private final DiscordService discordService;

    public HeartbeatController(@Value("${discord.admin}") String adminUser, DiscordService discordService) {
        this.adminUser = adminUser;
        this.discordService = discordService;
    }

    /**
     * Endpoint to check the availability and responsiveness of the Discord bot.
     * Sends a direct message (DM) to the admin user indicating that the bot is active.
     *
     * @return a ResponseEntity with an HTTP status of 200 (OK) indicating the successful execution of the ping request
     */
    @GetMapping("/discord/ping")
    public ResponseEntity<Void> ping() {
        discordService.sendPrivateMessage(adminUser, "[ping] Discord's bot is alive ! \uD83E\uDD16");
        return ResponseEntity.ok().build();
    }

}