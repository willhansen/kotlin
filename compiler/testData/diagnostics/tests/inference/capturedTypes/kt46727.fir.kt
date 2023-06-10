// !LANGUAGE: +RefineTypeCheckingOnAssignmentsToJavaFields
// WITH_STDLIB

// FILE: Foo.java
public class Foo<T> {
    public T konstue;
}

// FILE: Foo2.java
import org.jetbrains.annotations.Nullable;

public class Foo2<T> {
    public @Nullable T konstue;
}

// FILE: Foo3.java
import org.jetbrains.annotations.NotNull;

public class Foo3<T> {
    public @NotNull T konstue;
}

// FILE: main.kt

// --- from Java --- //

fun takeStarFoo(x: Foo<*>) {
    x.konstue = <!ASSIGNMENT_TYPE_MISMATCH!>"test"<!>
    x.konstue <!NONE_APPLICABLE!>+=<!> "test"
}

fun main1() {
    konst foo = Foo<Int>()
    foo.konstue = 1
    takeStarFoo(foo)
    println(foo.konstue) // CCE: String cannot be cast to Number
}

// --- from Kotlin --- //

public class Bar<T> {
    var konstue: T = null <!UNCHECKED_CAST!>as T<!>
}

fun takeStarBar(x: Bar<*>) {
    x.konstue = <!ASSIGNMENT_TYPE_MISMATCH!>"test"<!>
    x.konstue <!UNRESOLVED_REFERENCE!>+=<!> "test"
}

fun main2() {
    konst bar = Bar<Int>()
    bar.konstue = 1
    takeStarBar(bar)
    println(bar.konstue) // CCE: String cannot be cast to Number
}

// --- from Java (nullable) --- //

fun takeStarFoo2(x: Foo2<*>) {
    x.konstue = <!ASSIGNMENT_TYPE_MISMATCH!>"test"<!>
    x.konstue <!UNRESOLVED_REFERENCE!>+=<!> "test"
}

fun main3() {
    konst foo = Foo2<Int>()
    foo.konstue = 1
    takeStarFoo2(foo)
    println(foo.konstue) // CCE: String cannot be cast to Number
}

// --- from Kotlin (nullable) --- //
public class Bar2<T> {
    var konstue: T? = null
}

fun takeStarBar2(x: Bar2<*>) {
    x.konstue = <!ASSIGNMENT_TYPE_MISMATCH!>"test"<!>
    x.konstue <!UNRESOLVED_REFERENCE!>+=<!> "test"
}

fun main4() {
    konst bar = Bar2<Int>()
    bar.konstue = 1
    takeStarBar2(bar)
    println(bar.konstue) // CCE: String cannot be cast to Number
}

// --- from Java (not-null) --- //

fun takeStarFoo3(x: Foo3<*>) {
    x.konstue = <!ASSIGNMENT_TYPE_MISMATCH!>"test"<!>
    x.konstue <!UNRESOLVED_REFERENCE!>+=<!> "test"
}

fun main5() {
    konst foo = Foo3<Int>()
    foo.konstue = 1
    takeStarFoo3(foo)
    println(foo.konstue) // CCE: String cannot be cast to Number
}

// --- from Kotlin (field) --- //
class Bar3<T> {
    @JvmField
    var konstue: T? = null
}

fun takeStarBar3(x: Bar3<*>) {
    x.konstue = <!ASSIGNMENT_TYPE_MISMATCH!>"test"<!>
    x.konstue <!UNRESOLVED_REFERENCE!>+=<!> "test"
}

fun main6() {
    konst bar = Bar3<Int>()
    bar.konstue = 1
    takeStarBar3(bar)
    println(bar.konstue) // CCE: String cannot be cast to Number
}
