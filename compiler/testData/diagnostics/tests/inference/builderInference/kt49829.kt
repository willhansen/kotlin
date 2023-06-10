// FIR_IDENTICAL
// WITH_STDLIB
// ISSUE: KT-50293

fun main() {
    konst list = buildList {
        add("one")
        add("two")

        konst secondParameter = get(1)
        println(secondParameter as String)
    }
}