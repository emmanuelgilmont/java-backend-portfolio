package be.gate25.discord.controller;

import static org.mockito.Mockito.doNothing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import be.gate25.discord.service.DiscordService;

@ContextConfiguration(classes = { HeartbeatController.class })
@DisabledInAotMode
@ExtendWith(SpringExtension.class)
class HeartbeatControllerTest {
    @MockitoBean
    private DiscordService      discordService;

    @Autowired
    private HeartbeatController heartbeatController;

    /**
     * Test {@link HeartbeatController#ping()}.
     * <p>
     * Method under test: {@link HeartbeatController#ping()}
     */
    @Test
    @DisplayName("Test ping()")
    void testPing() throws Exception {
        // Arrange
        doNothing().when(discordService).sendPrivateMessage(Mockito.any(), Mockito.any());
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/discord/ping");

        // Act and Assert
        MockMvcBuilders
            .standaloneSetup(heartbeatController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}