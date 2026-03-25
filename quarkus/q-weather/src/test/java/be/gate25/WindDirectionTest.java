package be.gate25.weather;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.gate25.weather.domain.WindDirection;

public class WindDirectionTest {

    private static final Logger log = LoggerFactory.getLogger(WindDirectionTest.class);

    @Test
    public void testCardinalDirections() {
        assertEquals(WindDirection.N, WindDirection.fromDegrees(0));
        assertEquals(WindDirection.N, WindDirection.fromDegrees(360));
        assertEquals(WindDirection.E, WindDirection.fromDegrees(90));
        assertEquals(WindDirection.S, WindDirection.fromDegrees(180));
        assertEquals(WindDirection.W, WindDirection.fromDegrees(270));
        assertEquals(WindDirection.NE, WindDirection.fromDegrees(45));
        assertEquals(WindDirection.SE, WindDirection.fromDegrees(135));
        assertEquals(WindDirection.SW, WindDirection.fromDegrees(225));
        assertEquals(WindDirection.NW, WindDirection.fromDegrees(315));
    }

    @Test
    public void testBoundaryValues() {
        assertEquals(WindDirection.N, WindDirection.fromDegrees(11));
        assertEquals(WindDirection.NNE, WindDirection.fromDegrees(12));
        assertEquals(WindDirection.NNW, WindDirection.fromDegrees(348));
        assertEquals(WindDirection.N, WindDirection.fromDegrees(349));
    }

}