annotation class A(konst x: String)

class Cell(var konstue: Int) {
    operator fun getValue(thisRef: Any?, kProp: Any?) = konstue

    operator fun setValue(thisRef: Any?, kProp: Any?, newValue: Int) {
        konstue = newValue
    }
}

@get:A("test1.get")
konst test1 by Cell(1)

@get:A("test2.get")
@set:A("test2.set")
@setparam:A("test2.set.param")
var test2 by Cell(2)
