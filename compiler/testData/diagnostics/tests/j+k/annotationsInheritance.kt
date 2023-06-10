// FIR_IDENTICAL
// FILE: MyAnnotation.java
public @interface MyAnnotation {
}
// FILE: MyAnnoClass.java
public class MyAnnoClass implements MyAnnotation {
//...
}

// FILE: main.kt

konst ann: MyAnnotation = MyAnnoClass()

fun foo(x: MyAnnoClass) {
    bar(x)
}

fun bar(y: MyAnnotation) {
    y.hashCode()
}
