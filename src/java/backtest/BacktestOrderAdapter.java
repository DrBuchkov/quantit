package backtest;

import adapter.impl.BaseOrderAdapter;
import order.MarketOrder;

import java.util.ArrayList;

public class BacktestOrderAdapter extends BaseOrderAdapter {
    private ArrayList<Object> orders;


    public BacktestOrderAdapter() {
        this.orders = new ArrayList<>();
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

    public void setOrders(ArrayList<Object> orders) {
        this.orders = orders;
    }
}
