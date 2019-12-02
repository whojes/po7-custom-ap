package com.tmax.proobject.whojes.tiberoutil;

@FunctionalInterface
public interface CheckedConsumer<R> {
    void accept(R r) throws Throwable;
}