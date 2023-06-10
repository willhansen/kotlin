// EXPECTED_REACHABLE_NODES: 1282
package foo

class X

fun box(): String {
    konst a = X()
    konst b = X()
    if (a !== a) return "a !== a"
    if (a === b) return "X() === X()"
    konst c = a
    if (c !== a) return "c = a; c !== a"

    if (X() === a) return "X() === a"

    konst t = !(X() === a)
    if (!t) return "t = !(X() === a); t == false"

    konst f = !!(X() === a)
    if (f) return "f = !!(X() === null); f == true"
    return "OK";
}