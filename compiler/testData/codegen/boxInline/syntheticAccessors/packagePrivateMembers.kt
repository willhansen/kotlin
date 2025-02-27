// FILE: 1.kt

package test

private konst packageProp = "O"

private fun packageFun() = "K"

internal inline fun packageInline(p: (String, String) -> String): String {
    return p(packageProp, packageFun())
}

internal fun samePackageCall(): String {
    return packageInline { s, s2 -> s + s2 }
}

// FILE: 2.kt

import test.*

fun box(): String {
    konst packageResult = packageInline { a, b -> a + b }
    if (packageResult != "OK") return "package inline fail: $packageResult"

    konst samePackageResult = samePackageCall()
    if (samePackageResult != "OK") return "same package inline fail: $samePackageResult"

    return "OK"
}
