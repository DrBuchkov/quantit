package backtest;

import adapter.impl.BaseOrderAdapter;
import order.MarketOrder;

import java.util.ArrayList;

public class BacktestOrderAdapter extends BaseOrderAdapter {
    private final ArrayList<Object> orders;
//    private final List<HistoricalQuote> quotes;

    public BacktestOrderAdapter() {
        this.orders = new ArrayList<>();
//        this.quotes = quotes;
    }

    @Override
    public void _handle_order_intern(Object order) {
        this.orders.add(order);
    }

    @Override
    public void handleMarketOrder(MarketOrder order) {
    }

    public ArrayList<Object> getOrders() {
        return orders;
    }

//    public List<HistoricalQuote> getQuotes() {
//        return quotes;
//    }
}
