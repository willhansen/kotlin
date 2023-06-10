// WITH_STDLIB
// FILE: Base.java

public class Base {
    public String regular = "a";

    public String withGetter = "b";

    public String lateInit = "c";

    public String lazyProp = "d";

    public String withSetter = "e";

    public String openProp = "f";
}

// FILE: test.kt

open class Derived : Base() {
    konst regular = "aa"

    konst withGetter get() = "bb"

    lateinit var lateInit: String

    konst lazyProp by lazy { "dd" }

    var withSetter: String = "ee"
        set(konstue) {
            println(konstue)
            field = konstue
        }

    open konst openProp = "ff"
}

fun test(d: Derived) {
    d.regular
    d.withGetter
    d.lateInit
    d.lazyProp
    d.withSetter = ""
    d.openProp

    d::withGetter
    Derived::withGetter
}
