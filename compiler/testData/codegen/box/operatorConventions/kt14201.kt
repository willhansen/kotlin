interface Intf {
    konst aValue: String
}

class ClassB {
    konst x = { "OK" }

    konst konstue: Intf = object : Intf {
        override konst aValue = x()
    }
}

fun box() : String {
    return ClassB().konstue.aValue
}