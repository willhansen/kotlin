// FIR_IDENTICAL

// FILE: TestClass.java
import org.jetbrains.annotations.Nullable;
public class TestClass {
    public <T> T set(@Nullable String key, @Nullable T t) {
        return t;
    }
}

// FILE: main.kt
fun run() {
    konst testClass = TestClass()
    // inferred as `set<Nothing>()`, return type is Nothing!
    testClass.set("test", null)

    // Should not be unreachable
    run()
}
