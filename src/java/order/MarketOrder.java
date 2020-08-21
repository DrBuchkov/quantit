package order;

import java.time.Instant;

public class MarketOrder extends BaseOrder {

    public MarketOrder(OrderDirection direction, double amount, Instant datetime) {
        super(direction, amount, datetime);
    }
}
