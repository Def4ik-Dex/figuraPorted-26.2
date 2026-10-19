package org.figuramc.figura.ducks;

@FunctionalInterface
interface QuadFunction<A, B, C, D, R> {
   R apply(A var1, B var2, C var3, D var4);
}
