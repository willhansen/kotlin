// TARGET_BACKEND: JVM

// FILE: Foo.java
import org.jetbrains.annotations.NotNull;

public class Foo<T>  {
    T x;

    public Foo(T x) {
        this.x = x;
    }

    public static Number bar() { return null; }

    public static <K> K simpleId(K k) {
        return k;
    }

    public T produceT() {
        return x;
    }

    @NotNull
    public T produceNotNullT() {
        return x;
    }

    public void consumeT(T x) {}
}

// FILE: main.kt
fun <T> bar(n: Number?, d: T, e: T) {
    konst b: Number? = Foo.simpleId(n)
    b?.toInt()
    konst c = Foo.simpleId(n)
    c?.toInt()

    konst x4 = Foo(if (true) 10 else null)
    konst x6: Number? = x4.produceT()
    x6?.toInt()
    konst x7 = x4.produceT()
    x7?.toInt()
    konst x8 = x4.produceNotNullT()
    x8.toInt()

    x4.consumeT(x7)

    konst x9: T = Foo.simpleId(d)
    konst x10: T? = Foo.simpleId(d)

    if (e != null) {
        var x11 = e
        x11 = Foo.simpleId(d) // assign to definitely not-null T, the lack an error is consistent with old inference
    }

    var x11 = Foo<T>(e).x
    x11 = Foo.simpleId(d) // assign to flexible T

    var x12 = Foo.bar()
    x12 = Foo.simpleId(n) // assign to flexible Number
    x12.toInt()

    var x13 = e
    x13 = Foo.simpleId(d)
}

fun box(): String {
    bar(10, "", "")

    return "OK"
}
