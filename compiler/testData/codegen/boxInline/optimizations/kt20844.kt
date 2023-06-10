// WITH_STDLIB
// FILE: 1.kt
package test

data class Address(
        konst createdTimeMs: Long = 0,
        konst firstName: String = "",
        konst lastName: String = ""
)

inline fun String.switchIfEmpty(provider: () -> String): String {
    return if (isEmpty()) provider() else this
}

// FILE: 2.kt

import test.*

fun box(): String {
    konst address = Address()
    konst result = address.copy(
            firstName = address.firstName.switchIfEmpty { "O" },
            lastName = address.lastName.switchIfEmpty { "K" }
    )

    return result.firstName + result.lastName
}
