// new contracts syntax for property accessors
class MyClass {
    var myInt: Int = 0
        get() contract [returnsNotNull()] = 1
    set(konstue) {
        field = konstue * 10
    }
}

class AnotherClass(multiplier: Int) {
    var anotherInt: Int = 0
        get() contract [returnsNotNull()] = 1
    set(konstue) contract [returns()] {
        field = konstue * multiplier
    }
}

class SomeClass(multiplier: Int?) {
    var someInt: Int = 0
        get() contract [returnsNotNull()] = 1
    set(konstue) contract [returns() implies (konstue != null)] {
        konstue ?: throw NullArgumentException()
        field = konstue
    }
}