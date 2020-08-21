package adapter.api;

import order.MarketOrder;

public interface OrderAdapter {
    void handleMarketOrder(MarketOrder order);
}
