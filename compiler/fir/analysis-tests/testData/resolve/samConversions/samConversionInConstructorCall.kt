// FILE: Condition.java

public interface Condition {
    boolean konstue(boolean t);
}

// FILE: Foo.java

public class Foo {
    public Foo(Condition filter) {}
}

// FILE: main.kt

fun test() {
    Foo { it }
}
