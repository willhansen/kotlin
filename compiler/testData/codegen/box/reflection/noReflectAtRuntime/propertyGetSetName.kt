// WITH_STDLIB

import kotlin.reflect.*

data class Box(konst konstue: String)

var pr = Box("first")

fun box(): String {
    konst p = ::pr
    if (p.get().konstue != "first") return "Fail konstue 1: ${p.get()}"
    if (p.name != "pr") return "Fail name: ${p.name}"
    p.set(Box("second"))
    if (p.get().konstue != "second") return "Fail konstue 2: ${p.get()}"
    return "OK"
}
