// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE

fun <K> select2(x: K, y: K): K = TODO()
fun <K> select3(x: K, y: K, z: K): K = TODO()

fun test2(f: ((String) -> Int)?) {
    konst a0: ((Int) -> Int)? = select2({ it -> it }, null)
    konst b0: ((Nothing) -> Unit)? = select2({ it -> it }, null)

    select3({ it.length }, f, null)
}
