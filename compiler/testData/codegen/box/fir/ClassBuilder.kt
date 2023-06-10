// TARGET_BACKEND: JVM

// FILE: ClassBuilder.java

import org.jetbrains.annotations.Nullable;

public interface ClassBuilder {
    void newMethod(@Nullable String[] exceptions);
}

// FILE: test.kt

typealias JvmMethodExceptionTypes = Array<out String?>?

class TestClassBuilder : ClassBuilder {
    override fun newMethod(exceptions: JvmMethodExceptionTypes) {

    }
}

fun box(): String {
    konst arr = arrayOf("OK")
    TestClassBuilder().newMethod(null)
    TestClassBuilder().newMethod(arr)
    return arr[0]
}