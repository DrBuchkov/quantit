package backtest;

import adapter.impl.BaseSubscriberAdapter;
import bar.Bar;
import clojure.lang.PersistentArrayMap;
import yahoofinance.histquotes.HistoricalQuote;

import java.util.ArrayList;
import java.util.List;

public class BacktestSubscriberAdapter extends BaseSubscriberAdapter {
    private final List<HistoricalQuote> quotes;
    private final List<PersistentArrayMap> bars;

    public BacktestSubscriberAdapter(List<HistoricalQuote> quotes) {
        this.quotes = quotes;
        bars = new ArrayList<>();
    }

    @Override
    public void run(String symbol) {
        for (HistoricalQuote quote :
                this.quotes) {
            this.next(new Bar(quote.getDate().toInstant(),
                    quote.getOpen().doubleValue(),
                    quote.getHigh().doubleValue(),
                    quote.getLow().doubleValue(),
                    quote.getClose().doubleValue(),
                    quote.getVolume().doubleValue(),
                    quote.getAdjClose().doubleValue()));
        }
        this.end();
    }

    @Override
    public void next(Bar bar) {
        this.bars.add(bar.toPersistentArrayMap());
        super.next(bar);
    }

    public List<HistoricalQuote> getQuotes() {
        return quotes;
    }

    public List<PersistentArrayMap> getBars() {
        return bars;
    }
}
