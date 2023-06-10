// TARGET_BACKEND: JVM

// FILE: a.kt

object IntMapper : (Int) -> String {
    override fun invoke(konstue: Int): String {
        return konstue.toString()
    }
}

// FILE: Sam.java

public interface Sam<T, R> {
    R apply(T t);
}

// FILE: Foo.java

public class Foo {
    public static String bar1(IntMapper mapper) {
        return mapper.invoke(0);
    }

    public static <T, R> R bar2(Sam<? super T, ? extends R> mapper, T input) {
        return mapper.apply(input);
    }
}

// FILE: test.kt

fun box(): String {
    konst a = Foo.bar1(IntMapper)
    if (a != "0") return "Failed 0: $a"

    konst b = Foo.bar2(IntMapper, 1)
    if (b != "1") return "Failed 1: $b"

    return "OK"
}
