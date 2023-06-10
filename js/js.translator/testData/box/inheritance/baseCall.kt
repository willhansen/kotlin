// EXPECTED_REACHABLE_NODES: 1289
package foo

open class A(konst name: String)

class B(konst age: Int, name: String) : A(name)

fun box(): String {
    konst b = B(12, "Mike")

    if (b.age != 12) return "b.age != 12, it: ${b.age}"
    if (b.name != "Mike") return "b.name != 'Mike', it: ${b.name}"

    return "OK"
}