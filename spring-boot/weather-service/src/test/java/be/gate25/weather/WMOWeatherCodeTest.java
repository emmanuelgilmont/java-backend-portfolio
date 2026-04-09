package be.gate25.weather;

import be.gate25.weather.domain.WMOWeatherCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class WMOWeatherCodeTest {


    @Test
    public void testSomeCode(){
        assertEquals("\u2614 Drizzle (heavy)", WMOWeatherCode.getMeaningFromCode(55));
        assertEquals("Unknown code: 128", WMOWeatherCode.getMeaningFromCode(128));
    }

    @Test
    public void testNonNullCode(){
        String meaning = WMOWeatherCode.getMeaningFromCode(55);
        assertNotNull(meaning, "Meaning for code 55 should not be null");
    }
}