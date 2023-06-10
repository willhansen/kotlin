// FILE: JClass.java

import org.jetbrains.annotations.NotNull;

public class JClass {
    @NotNull
    public static <T> T getNotNullT() {
        return null;
    }
}

// FILE: test.kt
fun <T : Any> test() {
    var konstue: T? = null
    if (konstue == null) {
        konstue = JClass.getNotNullT()
    }

    konstue.hashCode() // unsafe call error
}