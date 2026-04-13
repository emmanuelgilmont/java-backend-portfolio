package be.gate25.fxrate.provider;

public class UnsupportedPairException extends RuntimeException {

    public UnsupportedPairException(String pair) {
        super("FX pair not supported: " + pair);
    }
}
