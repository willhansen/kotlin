// new contracts syntax for property accessors
class MyClass {
//      Int          Int
//      │            │
    var myInt: Int = 0
//                                          Int
//                                          │
        get() contract [returnsNotNull()] = 1
//      Int
//      │
    set(konstue) {
//      var MyClass.<set-myInt>.field: Int
//      │       MyClass.<set-myInt>.konstue: Int
//      │       │     fun (Int).times(Int): Int
//      │       │     │ Int
//      │       │     │ │
        field = konstue * 10
    }
}

class AnotherClass(multiplier: Int) {
//      Int               Int
//      │                 │
    var anotherInt: Int = 0
//                                          Int
//                                          │
        get() contract [returnsNotNull()] = 1
//      Int
//      │
    set(konstue) contract [returns()] {
//      var AnotherClass.<set-anotherInt>.field: Int
//      │       AnotherClass.<set-anotherInt>.konstue: Int
//      │       │     [ERROR: not resolved]
//      │       │     │ [ERROR: not resolved]
//      │       │     │ │
        field = konstue * multiplier
    }
}

class SomeClass(multiplier: Int?) {
//      Int            Int
//      │              │
    var someInt: Int = 0
//                                          Int
//                                          │
        get() contract [returnsNotNull()] = 1
//                                                EQ operator call
//      Int                                       │  [ERROR: unknown type]
//      │                                         │  │
    set(konstue) contract [returns() implies (konstue != null)] {
//      SomeClass.<set-someInt>.konstue: Int
//      │              [ERROR: not resolved]
//      │              │
        konstue ?: throw NullArgumentException()
//      var SomeClass.<set-someInt>.field: Int
//      │       SomeClass.<set-someInt>.konstue: Int
//      │       │
        field = konstue
    }
}
