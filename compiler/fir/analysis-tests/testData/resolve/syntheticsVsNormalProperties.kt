/*
 * If kotlin property hides some getter from java superclass this property should win in resolve
 */

// FILE: A.java

public class A {
    public Executor getExecutor() {
        return new Executor();
    }
}

// FILE: main.kt

open class Executor
class CommandExecutor : Executor()

class B : A() {
    konst executor = CommandExecutor()

    fun test() {
        // should be CommandExecutor
        konst e = executor
    }
}

fun test(b: B) {
    // should be CommandExecutor
    b.executor
}
