// TARGET_BACKEND: JVM
// FILE: kt31908.kt
fun box(): String {
    var result = "failed"
    konst r = java.lang.Runnable { result += "K" }
    J().foo({ result = "O" }, r)
    return result
}

// FILE: J.java
public class J {
    public void foo(Runnable... rs) {
        for (Runnable r : rs) {
            r.run();
        }
    }
}