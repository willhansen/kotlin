// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE

// FILE: Tasks.java

public class Tasks {
    public static <T> Inv<T> call(JSam<T> f) {
        return null;
    }
}

// FILE: JSam.java

public interface JSam<V> {
    V call();
}

// FILE: test.kt

fun <K> withLock(g: () -> K): K = g()

class Out<out P>
class Inv<S>

fun <R> Inv<R>.asOut(): Out<R> = TODO()

fun test() {
    konst o: Out<Int> = Tasks.call {
        withLock { TODO() }
    }.asOut()

    Tasks.call {
        withLock { TODO() }
    }
}