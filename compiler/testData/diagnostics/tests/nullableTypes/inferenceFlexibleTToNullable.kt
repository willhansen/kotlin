// !DIAGNOSTICS: -UNUSED_VARIABLE -UNCHECKED_CAST -UNUSED_VALUE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE
// SKIP_TXT

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
    konst a: Number = <!TYPE_MISMATCH!>Foo.simpleId(n)<!>
    konst b: Number? = Foo.simpleId(n)
    konst c = <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Number?")!>Foo.simpleId(n)<!>

    konst x4 = Foo(if (true) 10 else null)
    konst x5: Number = <!TYPE_MISMATCH!>x4.produceT()<!>
    konst x6: Number? = x4.produceT()
    konst x7 = <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int?")!>x4.produceT()<!>
    konst x8 = <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int")!>x4.produceNotNullT()<!>

    x4.consumeT(x7)

    konst x9: T = Foo.simpleId(d)
    konst x10: T? = Foo.simpleId(d)

    if (e != null) {
        var x11 = e
        x11 = Foo.simpleId(d) // assign to definitely not-null T, the lack an error is consistent with old inference
    }

    var x11 = Foo<T>(null as T).x
    x11 = Foo.simpleId(d) // assign to flexible T

    var x12 = Foo.bar()
    x12 = Foo.simpleId(n) // assign to flexible Number

    var x13 = e
    x13 = Foo.simpleId(d)
}
