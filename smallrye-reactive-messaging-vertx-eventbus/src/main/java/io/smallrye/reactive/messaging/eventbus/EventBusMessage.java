package io.smallrye.reactive.messaging.eventbus;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.vertx.core.MultiMap;

public class EventBusMessage<T> implements Message<T> {

    private final Supplier<CompletionStage<Void>> ack;
    private io.vertx.core.eventbus.Message<T> wrapped;
    private T payload;
    private String address;
    private String replyAddress;

    private MultiMap headers;

    EventBusMessage(io.vertx.core.eventbus.Message<T> m, Supplier<CompletionStage<Void>> ack) {
        this.wrapped = m;
        this.payload = m.body();
        this.address = m.address();
        this.replyAddress = m.replyAddress();
        this.headers = m.headers();
        this.ack = ack;
    }

    EventBusMessage(io.vertx.reactivex.core.eventbus.Message<T> m, Supplier<CompletionStage<Void>> ack) {
        this(m.getDelegate(), ack);
    }

    @Override
    public CompletionStage<Void> ack() {
        if (this.ack != null) {
            return ack.get();
        }
        return null;
    }

    @Override
    public T getPayload() {
        return payload;
    }

    public String getAddress() {
        return address;
    }

    public Optional<String> getReplyAddress() {
        return Optional.ofNullable(replyAddress);
    }

    public Optional<String> getHeader(String key) {
        return Optional.ofNullable(this.headers.get(key));
    }

    public List<String> getHeaders(String key) {
        return this.headers.getAll(key);
    }

    public io.vertx.core.eventbus.Message<T> unwrap() {
        return wrapped;
    }

}
