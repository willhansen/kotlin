open class Bar<T>(konst prop: String)
class Foo {
    companion object : Bar<Foo>("OK") {
        konst p = Foo.prop
        konst p2 = prop
        konst p3 = this.prop
    }

    konst p4 = Foo.prop
    konst p5 = prop
}

fun box(): String = Foo.prop