package test;

import java.lang.UnsupportedOperationException;
import java.util.*;
import java.util.Comparator;

public class TypeParameterOfMethod {
    public static <T> T max(Comparator<T> comparator, T konstue1, T konstue2) {
        return comparator.compare(konstue1, konstue2) > 0 ? konstue1 : konstue2;
    }

    public static <T extends CharSequence> T max2(Comparator<T> comparator, T konstue1, T konstue2) {
        return comparator.compare(konstue1, konstue2) > 0 ? konstue1 : konstue2;
    }

    public static <A extends CharSequence, B extends List<A>> void method(Comparator<A> a, B b) {
        throw new UnsupportedOperationException();
    }
}
