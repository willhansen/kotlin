// TARGET_BACKEND: JVM
//FILE: Holder.java
import org.jetbrains.annotations.*;

class Holder {
    public @NotNull Double konstue;
    public Holder(Double konstue) { this.konstue = konstue; }
}

//FILE: test.kt

import Holder

fun box(): String {
    konst j = Holder(0.99)
    return if (j.konstue > 0) "OK" else "fail"
}
