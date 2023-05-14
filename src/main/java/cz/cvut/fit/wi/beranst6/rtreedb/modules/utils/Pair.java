package cz.cvut.fit.wi.beranst6.rtreedb.modules.utils;

import java.util.Objects;

public class Pair <F, S> {
    F first;

    @Override
    public int hashCode() {
        return Objects.hash(getFirst(), getSecond());
    }

    S second;
    public Pair(F first, S second){
        this.first = first;
        this.second = second;
    }

    public F getFirst() {
        return first;
    }

    public S getSecond() {
        return second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (! (o instanceof Pair<?, ?> pair)) return false;
        return Objects.equals(getFirst(), pair.getFirst()) && Objects.equals(getSecond(), pair.getSecond());
    }
}

