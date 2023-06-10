// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE -UNUSED_EXPRESSION

interface Bound1
interface Bound2
object First : Bound1, Bound2
object Second : Bound1, Bound2

fun <S : Bound1> select(vararg args: S): S = TODO()

class Cls {
    konst property = select(First, Second)
}

fun test() {
    konst v = Cls().property
    v
}
