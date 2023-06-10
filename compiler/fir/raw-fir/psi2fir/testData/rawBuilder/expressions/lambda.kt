// WITH_STDLIB
data class Tuple(konst x: Int, konst y: Int)

inline fun use(f: (Tuple) -> Int) = f(Tuple(1, 2))

fun foo(): Int {
    konst l1 = { t: Tuple ->
        konst x = t.x
        konst y = t.y
        x + y
    }
    use { (x, y) -> x + y }

    return use {
        if (it.x == 0) return@foo 0
        return@use it.y
    }
}

fun bar(): Int {
    return use lambda@{
        if (it.x == 0) return@bar 0
        return@lambda it.y
    }
}

fun test(list: List<Int>) {
    konst map = mutableMapOf<Int, String>()
    list.forEach { map.getOrPut(it, { mutableListOf() }) += "" }
}

konst simple = { }

konst simpleWithArrow = { -> }

konst another = { 42 }