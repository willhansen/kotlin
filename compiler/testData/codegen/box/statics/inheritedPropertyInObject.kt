open class Bar<T>(konst prop: String)
object Foo : Bar<Foo>("OK") {

    konst p = Foo.prop
    konst p2 = prop
    konst p3 = this.prop
}
fun box(): String = Foo.prop