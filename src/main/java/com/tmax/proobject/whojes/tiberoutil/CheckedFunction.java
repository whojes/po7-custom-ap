package com.tmax.proobject.whojes.tiberoutil;

@FunctionalInterface
public interface CheckedFunction<T, R> {
    R run(T t) throws Throwable;
}