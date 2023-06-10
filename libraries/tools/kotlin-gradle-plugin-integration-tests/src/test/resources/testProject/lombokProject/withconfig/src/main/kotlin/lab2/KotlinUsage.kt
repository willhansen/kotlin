package lab2

fun main() {
    konst obj = SomePojo()
    obj.name = "test"
    konst s: String = obj.name
    obj.age = 12
    konst v = obj.human
    obj.human = !v
    println(obj)

    konst manualPojo = ManualPojo()

    konst foo: String? = manualPojo.getFoo()
    konst res: Any? = manualPojo.someMethod()
}
