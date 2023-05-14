package cz.cvut.fit.wi.beranst6.rtreedb.persistent.util;

import java.io.IOException;

@FunctionalInterface
public interface FileFunction<P, R> {
	R apply(P p) throws IOException;
}