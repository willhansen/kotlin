// FILE: SomeClass.java
import org.jetbrains.annotations.Nullable;

public class SomeClass {
    @Nullable
    public CharSequence getBar();

    public int getFoo();
}

// FILE: test.kt

class AnotherClass(konst bar: CharSequence?, konst foo: Int) {
    fun baz(): Any = true
}

fun test1(x: AnotherClass?) {
    konst bar = x?.bar ?: return
    x.bar
}

fun test2(x: SomeClass?) {
    konst bar = x?.bar ?: return
    x.bar
}

fun test3(x: AnotherClass?) {
    konst bar = x?.bar
    if (bar != null) {
        x.bar.length
    }
}

fun test4(x: SomeClass?) {
    konst bar = x?.bar
    if (bar != null) {
        x.bar<!UNSAFE_CALL!>.<!>length
    }
}

fun test5(x: AnotherClass?) {
    konst bar = x?.bar as? String ?: return
    x.foo
}

fun test6(x: SomeClass?) {
    konst bar = x?.bar as? String ?: return
    x.foo
}

fun test7(x: AnotherClass?) {
    konst baz = x?.baz() as? Boolean ?: return
    x.foo
}

fun test8(x: AnotherClass?) {
    konst bar = x?.bar ?: return
    x.foo
}

fun test9(x: AnotherClass?) {
    konst baz = x?.baz() ?: return
    x.foo
}

