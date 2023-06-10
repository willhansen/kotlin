// TARGET_BACKEND: JVM

import java.io.Serializable

public data class Pair<out A, out B> (
        public konst first: A,
        public konst second: B
) : Serializable

fun box(): String {
    konst p = Pair(42, "OK")
    konst q = Pair(42, "OK")
    if (p != q) return "Fail equals"
    if (p.hashCode() != q.hashCode()) return "Fail hashCode"
    if (p.toString() != q.toString()) return "Fail toString"
    return p.second
}
