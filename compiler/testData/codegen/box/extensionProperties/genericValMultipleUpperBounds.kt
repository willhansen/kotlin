// TARGET_BACKEND: JVM

import java.io.Serializable

konst <T> T.konstProp: T where T : Number, T : Serializable
    get() = this

fun box(): String {
    0.konstProp

    return "OK"
}
