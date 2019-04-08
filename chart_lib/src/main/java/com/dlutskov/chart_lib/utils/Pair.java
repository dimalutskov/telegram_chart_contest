package com.dlutskov.chart_lib.utils;

/**
 * Simple mutable pair
 */
public class Pair<A extends Object, B extends Object> {

    public A first;

    public B second;

    public Pair(A a, B b) {
        update(a, b);
    }

    public Pair<A, B> update(A a, B b) {
        this.first = a;
        this.second = b;
        return this;
    }
}
