package adapter.api;

public interface SubscriberAdapter {
    void run(String symbol);

    void end();
}
