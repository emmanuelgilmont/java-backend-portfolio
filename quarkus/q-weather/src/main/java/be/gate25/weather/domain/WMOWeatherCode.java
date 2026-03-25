package be.gate25.weather.domain;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum WMOWeatherCode {
    WMO_0(0,"Clear sky"),
    WMO_1(1,"Mainly clear"),
    WMO_2(2,"Partly cloudy"),
    WMO_3(3,"Overcast"),
    WMO_45(45,"Fog"),
    WMO_48(48,"Depositing rime fog"),
    WMO_51(51,"Drizzle (light)"),
    WMO_53(53,"Drizzle (moderate)"),
    WMO_55(55,"Drizzle (heavy)"),
    WMO_56(56,"Freezing Drizzle (light)"),
    WMO_57(57,"Freezing Drizzle (heavy)"),
    WMO_61(61,"Rain (light)"),
    WMO_63(63,"Rain (moderate)"),
    WMO_65(65,"Rain (heavy)"),
    WMO_66(66,"Freezing Rain (light)"),
    WMO_67(67,"Freezing Rain (heavy)"),
    WMO_71(71,"Snow fall (slight)"),
    WMO_73(73,"Snow fall (moderate)"),
    WMO_75(75,"Snow fall (heavy)"),
    WMO_77(77,"Snow grains"),
    WMO_80(80,"Rain showers (slight)"),
    WMO_81(81,"Rain showers (moderate)"),
    WMO_82(82,"Rain showers (violent)"),
    WMO_85(85,"Snow shower (slight)"),
    WMO_86(86,"Snow shower (heavy)"),
    WMO_95(95,"Thunderstorm"),
    WMO_96(96,"Thunderstorm (with slight hail)"),
    WMO_99(99,"Thunderstorm (with heavy hail)");
    /*
    0 Clear sky
1, 2, 3	Mainly clear, partly cloudy, and overcast
45, 48	Fog and depositing rime fog
51, 53, 55	Drizzle: Light, moderate, and dense intensity
56, 57	Freezing Drizzle: Light and dense intensity
61, 63, 65	Rain: Slight, moderate and heavy intensity
66, 67	Freezing Rain: Light and heavy intensity
71, 73, 75	Snow fall: Slight, moderate, and heavy intensity
77	Snow grains
80, 81, 82	Rain showers: Slight, moderate, and violent
85, 86	Snow showers slight and heavy
95 *	Thunderstorm: Slight or moderate
96, 99 *	Thunderstorm with slight and heavy hail
      */

    private final int code;
    private final String meaning;

    WMOWeatherCode(int code,String meaning){
        this.code = code;
        this.meaning = meaning;
    }

    public int getCode() {
        return code;
    }

    public String getMeaning() {
        return meaning;
    }

    // Lookup O(1)
    private static final Map<Integer, WMOWeatherCode> BY_CODE =
        Stream.of(values()).collect(Collectors.toUnmodifiableMap(
            WMOWeatherCode::getCode,
            Function.identity()
        ));

    public static String getMeaningFromCode(int code) {
        WMOWeatherCode s = BY_CODE.get(code);
        if (s == null) {
            return "Unknown code: " + code;
        }
        return s.meaning;
    }
}