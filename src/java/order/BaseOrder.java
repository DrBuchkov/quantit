package order;

import java.time.Instant;

public abstract class BaseOrder {
    private OrderDirection direction;
    private double amount;
    private Instant datetime;

    public BaseOrder(OrderDirection direction, double amount, Instant datetime) {
        this.direction = direction;
        this.amount = amount;
        this.datetime = datetime;
    }

    public OrderDirection getDirection() {
        return direction;
    }

    public void setDirection(OrderDirection direction) {
        this.direction = direction;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Instant getDatetime() {
        return datetime;
    }

    public void setDatetime(Instant datetime) {
        this.datetime = datetime;
    }
}
