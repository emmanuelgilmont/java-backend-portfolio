package be.gate25.weather.domain;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum WMOWeatherCode {
    WMO_0(0,"\u2600\uFE0F Clear sky"),
    WMO_1(1,"\uD83C\uDF24\uFE0F Mainly clear"),
    WMO_2(2,"\u26C5 Partly cloudy"),
    WMO_3(3,"\u2601\uFE0F Overcast"),
    WMO_45(45,"\uD83C\uDF2B\uFE0F Fog"),
    WMO_48(48,"\uD83C\uDF01 Depositing rime fog"),
    WMO_51(51,"\uD83C\uDF26\uFE0F Drizzle (light)"),
    WMO_53(53,"\uD83C\uDF27\uFE0F Drizzle (moderate)"),
    WMO_55(55,"\u2614 Drizzle (heavy)"),
    WMO_56(56,"\uD83C\uDF28\uFE0F Freezing Drizzle (light)"),
    WMO_57(57,"\u2744\uFE0F Freezing Drizzle (heavy)"),
    WMO_61(61,"\uD83C\uDF26\uFE0F Rain (light)"),
    WMO_63(63,"\uD83C\uDF27\uFE0F Rain (moderate)"),
    WMO_65(65,"\u2614 Rain (heavy)"),
    WMO_66(66,"\uD83C\uDF28\uFE0F Freezing Rain (light)"),
    WMO_67(67,"\u2744\uFE0F Freezing Rain (heavy)"),
    WMO_71(71,"\uD83C\uDF28\uFE0F Snow fall (slight)"),
    WMO_73(73,"\u2744\uFE0F Snow fall (moderate)"),
    WMO_75(75,"\u2603\uFE0F Snow fall (heavy)"),
    WMO_77(77,"\uD83C\uDF28\uFE0F now grains"),
    WMO_80(80,"\uD83C\uDF26\uFE0F Rain showers (slight)"),
    WMO_81(81,"\uD83C\uDF27\uFE0F Rain showers (moderate)"),
    WMO_82(82,"\u26C8\uFE0F Rain showers (violent)"),
    WMO_85(85,"\uD83C\uDF28\uFE0F Snow shower (slight)"),
    WMO_86(86,"\u2744\uFE0F Snow shower (heavy)"),
    WMO_95(95,"\u26C8\uFE0F Thunderstorm"),
    WMO_96(96,"\uD83C\uDF29\uFE0F Thunderstorm (with slight hail)"),
    WMO_99(99,"\u26C8\uFE0F Thunderstorm (with heavy hail)");
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