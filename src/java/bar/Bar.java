package bar;

import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class Bar {
    private Instant datetime;
    private double open;
    private double high;
    private double low;
    private double close;
    private double volume;
    private double adjustedClose;

    public Bar(Instant datetime, double open, double high, double low, double close, double volume, double adjustedClose) {
        this.datetime = datetime;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.adjustedClose = adjustedClose;
    }

    public Instant getDatetime() {
        return datetime;
    }

    public void setDatetime(Instant datetime) {
        this.datetime = datetime;
    }

    public double getOpen() {
        return open;
    }

    public void setOpen(double open) {
        this.open = open;
    }

    public double getHigh() {
        return high;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public double getLow() {
        return low;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public double getClose() {
        return close;
    }

    public void setClose(double close) {
        this.close = close;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public double getAdjustedClose() {
        return adjustedClose;
    }

    public void setAdjustedClose(double adjustedClose) {
        this.adjustedClose = adjustedClose;
    }

    public PersistentArrayMap toPersistentArrayMap() {
        Map<Keyword, Object> tmp = new HashMap<>();
        tmp.put(Keyword.intern("datetime"), this.getDatetime());
        tmp.put(Keyword.intern("open"), this.getOpen());
        tmp.put(Keyword.intern("high"), this.getHigh());
        tmp.put(Keyword.intern("low"), this.getLow());
        tmp.put(Keyword.intern("close"), this.getClose());
        tmp.put(Keyword.intern("volume"), this.getVolume());
        tmp.put(Keyword.intern("adj-close"), this.getAdjustedClose());
        return (PersistentArrayMap) PersistentArrayMap.create(tmp);
    }
}
