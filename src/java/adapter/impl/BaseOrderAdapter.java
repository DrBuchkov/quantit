package adapter.impl;

import adapter.api.OrderAdapter;
import clojure.java.api.Clojure;
import clojure.lang.IFn;
import clojure.lang.Symbol;
import order.BaseOrder;
import order.MarketOrder;

public abstract class BaseOrderAdapter implements OrderAdapter {
    private static final IFn pullFromChannel = Clojure.var("clojure.core.async", "<!!");
    private static final IFn toJava = Clojure.var("quantit.order.core", "to-java");

    static {
        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("quantit.order.core"));
        require.invoke(Clojure.read("clojure.core.async"));
    }

    private String symbol;

    public void _run_intern(String symbol, Object channel) {
        this.symbol = symbol;
        while (true) {
            Object msg = pullFromChannel.invoke(channel);
            if (Symbol.create("end").equals(msg))
                break;
            this._handle_order_intern(msg);
        }
    }

    public void _handle_order_intern(Object order) {
        BaseOrder baseOrder = (BaseOrder) toJava.invoke(order);
        if (baseOrder instanceof MarketOrder) {
            this.handleMarketOrder((MarketOrder) baseOrder);
        }
    }

    public String getSymbol() {
        return symbol;
    }
}
