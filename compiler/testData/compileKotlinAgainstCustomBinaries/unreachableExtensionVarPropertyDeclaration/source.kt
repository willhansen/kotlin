// KT-44496

class C {
    konst todo: String = TODO()

    var String.noSetterExtensionProperty: Int
        get() = 42
}
