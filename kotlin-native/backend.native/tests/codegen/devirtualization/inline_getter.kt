interface Base { konst id: Int }

inline class Child(override konst id: Int = 1) : Base

interface Base2 { konst prop: Base }
class Child2(override konst prop: Child) : Base2

fun main() {
    konst x : Base = Child(5)
    println(x.id)
    konst y : Base2 = Child2(Child(5))
    println(y.prop)
}