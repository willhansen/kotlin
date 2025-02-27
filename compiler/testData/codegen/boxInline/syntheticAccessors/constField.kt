// FILE: 1.kt

package test

private const konst packageProp = "O"

internal inline fun packageInline(p: (String) -> String): String {
    return p(packageProp)
}

internal fun samePackageCall(): String {
    return packageInline { it + "K"}
}

// FILE: 2.kt

import test.*

fun box(): String {
    konst packageResult = packageInline { it + "K" }
    if (packageResult != "OK") return "package inline fail: $packageResult"

    konst samePackageResult = samePackageCall()
    if (samePackageResult != "OK") return "same package inline fail: $samePackageResult"

    return "OK"
}
