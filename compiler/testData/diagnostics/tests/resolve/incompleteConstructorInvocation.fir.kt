package p

class X<V>(provider: () -> V, trackValue: Boolean) {
}

class B {
    konst c = <!NO_VALUE_FOR_PARAMETER!>X<!><String> {
        "e"
    }
}
