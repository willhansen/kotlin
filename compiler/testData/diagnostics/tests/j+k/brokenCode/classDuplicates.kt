// FILE: Hello.kt
private class Hello()
{
    konst a = 4
}

fun test() {
    // no exception is thrown (see KT-3897)
    Hello().a
}

// FILE: Hello.java
public class Hello {}
