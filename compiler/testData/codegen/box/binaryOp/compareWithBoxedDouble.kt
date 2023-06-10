// TARGET_BACKEND: JVM
//FILE: Holder.java

class Holder {
    public Double konstue;
    public Holder(Double konstue) { this.konstue = konstue; }
}

//FILE: test.kt

import Holder

fun box(): String {
    konst j = Holder(0.99)
    return if (j.konstue > 0) "OK" else "fail"
}
