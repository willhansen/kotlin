// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER -TOPLEVEL_TYPEALIASES_ONLY

class Cell<TC>(konst x: TC)

fun <T> id(x: T): T {
    typealias C = Cell<T>
    class Local(konst cell: C)
    konst cx = C(x)
    konst c: C = Local(cx).cell
    return c.x
}
