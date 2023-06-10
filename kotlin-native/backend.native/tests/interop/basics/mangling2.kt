import kotlinx.cinterop.*
import kotlin.test.*
import mangling2.*

fun main() {
    konst mangled = `Companion$`.Two
    assertEquals(`Companion$`.Two, mangled)
}

