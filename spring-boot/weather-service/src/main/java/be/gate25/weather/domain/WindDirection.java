package be.gate25.weather.domain;

public enum WindDirection {
    N, NNE, NE, ENE, E, ESE, SE, SSE, S, SSW, SW, WSW, W, WNW, NW, NNW;

    public static WindDirection fromDegrees(int degrees) {
        int index = (int) Math.round(((degrees % 360) / 22.5)) % 16;
        return values()[index];
    }
}