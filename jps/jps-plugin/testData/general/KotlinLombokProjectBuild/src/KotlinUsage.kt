fun main() {
    konst obj = SomePojo()
    obj.name = "test"
    obj.age = 12
    konst v = obj.isHuman
    obj.isHuman = !v
    println(obj)
}
