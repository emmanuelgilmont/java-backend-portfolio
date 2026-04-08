package be.gate25.discord;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import be.gate25.discord.service.DiscordService;

@SpringBootTest
class DiscordApplicationTests {

    @Value("${discord.admin}")
    private String         userAdmin;

    @Autowired
    private DiscordService discordService;

    @Test
    void testSendDM() {
        discordService.sendPrivateMessage(userAdmin, "Hello from Discord bot ! 🤖 (just a silly test)");
    }

}