package backtest;

import adapter.impl.BaseSubscriberAdapter;
import bar.Bar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

public class BacktestSubscriberAdapter extends BaseSubscriberAdapter {
    private static final Logger logger = LoggerFactory.getLogger(BacktestSubscriberAdapter.class);
    private String symbol;
    private Calendar from;
    private Calendar to;
    private Interval interval;

    public BacktestSubscriberAdapter(String symbol, Calendar from, Calendar to, Interval interval) {
        this.symbol = symbol;
        this.from = from;
        this.to = to;
        this.interval = interval;
    }

    @Override
    public void run(String symbol) {
        try {
            List<HistoricalQuote> quotes = YahooFinance.get(symbol).getHistory(this.from, this.to, this.interval);
            for (HistoricalQuote quote :
                    quotes) {
                this.next(new Bar(quote.getDate().toInstant(),
                        quote.getOpen().doubleValue(),
                        quote.getHigh().doubleValue(),
                        quote.getLow().doubleValue(),
                        quote.getClose().doubleValue(),
                        quote.getVolume().doubleValue(),
                        quote.getAdjClose().doubleValue()));
            }
            this.end();
        } catch (IOException e) {
            logger.error("Could not find data for symbol " + symbol, e.getMessage());
        }
    }

    public Interval getInterval() {
        return interval;
    }

    public void setInterval(Interval interval) {
        this.interval = interval;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Calendar getFrom() {
        return from;
    }

    public void setFrom(Calendar from) {
        this.from = from;
    }

    public Calendar getTo() {
        return to;
    }

    public void setTo(Calendar to) {
        this.to = to;
    }
}
