package lab2

fun main() {
    println("something")
    konst obj = SomePojo()
    obj.name = "test"
    konst s: String = obj.name
    obj.age = 12
    konst v = obj.isHuman
    obj.isHuman = !v
    println(obj)
//
//    konst manualPojo = ManualPojo()
//
//    konst foo: String? = manualPojo.getFoo()
//    konst res: Any? = manualPojo.someMethod()
//
    konst ddd = SomeData()

    JavaUsage.cycleUsage()
}

class SomeKotlinClass {
    fun call() {
        konst ddd = SomeData()
        ddd.age = 12
        println(ddd)
    }
}
