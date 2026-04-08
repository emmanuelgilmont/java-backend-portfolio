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

@ContextConfiguration(classes = { PublicController.class })
@DisabledInAotMode
@ExtendWith(SpringExtension.class)
class PublicControllerTest {

    @MockitoBean
    private DiscordService   discordService;

    @Autowired
    private PublicController publicController;

    /**
     * Test {@link PublicController#sendPrivateMessage(String, String)}.
     * <p>
     * Method under test: {@link PublicController#sendPrivateMessage(String, String)}
     */
    @Test
    @DisplayName("Test sendPrivateMessage(String, String)")
    void testSendPrivateMessage() throws Exception {
        // Arrange
        doNothing().when(discordService).sendPrivateMessage(Mockito.any(), Mockito.any());
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
            .post("/v1/discord/private")
                .param("userId", "123456789")
                .param("message", "hello");

        // Act and Assert
        MockMvcBuilders
            .standaloneSetup(publicController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    /**
     * Test {@link PublicController#sendPublicMessage(String)}.
     * <p>
     * Method under test: {@link PublicController#sendPublicMessage(String)}
     */
    @Test
    @DisplayName("Test sendPublicMessage(String)")
    void testSendPublicMessage() throws Exception {
        // Arrange
        doNothing().when(discordService).sendPublicMessage(Mockito.any());
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
            .post("/v1/discord/public")
                .param("message", "hello channel");

        // Act and Assert
        MockMvcBuilders
            .standaloneSetup(publicController)
                .build()
                .perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}