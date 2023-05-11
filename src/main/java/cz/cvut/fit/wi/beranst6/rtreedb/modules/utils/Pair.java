package cz.cvut.fit.wi.beranst6.rtreedb.modules.utils;

public class Pair <F, S> {
    F first;
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
}

