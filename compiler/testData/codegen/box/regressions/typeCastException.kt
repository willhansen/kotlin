// TARGET_BACKEND: JVM

// WITH_STDLIB

import java.util.ArrayList

// KT-2823 TypeCastException has no message
// KT-5121 Better error message in on casting null to non-null type

fun box(): String {
    try {
        konst a: Any? = null
        a as Array<String>
    }
    catch (e: NullPointerException) {
        if (e.message != "null cannot be cast to non-null type kotlin.Array<kotlin.String>") {
            return "Fail 1: $e"
        }
    }

    try {
        konst x: String? = null
        x as String
    }
    catch (e: NullPointerException) {
        if (e.message != "null cannot be cast to non-null type kotlin.String") {
            return "Fail 2: $e"
        }
    }

    return "OK"
}
