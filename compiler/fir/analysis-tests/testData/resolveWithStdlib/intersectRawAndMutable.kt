// FILE: Producer.java

import java.util.*;
import org.jetbrains.annotations.*;

public class Producer {
    @NotNull
    public static ArrayList foo() { return null; }
}

// FILE: test.kt

interface StringSet : MutableSet<String>

fun foo(arg: Boolean) {
    konst x = Producer.foo()
    if (x is Set<*>) {
        konst y = <!DEBUG_INFO_EXPRESSION_TYPE("java.util.ArrayList<kotlin.Any..kotlin.Any?!>..java.util.ArrayList<*> & kotlin.collections.Set<*> & java.util.ArrayList<kotlin.Any..kotlin.Any?!>..java.util.ArrayList<*>")!>x<!>
    }
    if (x is MutableSet<*>) {
        konst y = <!DEBUG_INFO_EXPRESSION_TYPE("java.util.ArrayList<kotlin.Any..kotlin.Any?!>..java.util.ArrayList<*> & kotlin.collections.MutableSet<*> & java.util.ArrayList<kotlin.Any..kotlin.Any?!>..java.util.ArrayList<*>")!>x<!>
    }
    if (x is StringSet) {
        x.add("")
        x.add(1)
        x.add(null)
        x.iterator().next().length
    }
}
