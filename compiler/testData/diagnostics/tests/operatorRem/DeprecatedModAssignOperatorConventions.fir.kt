// !LANGUAGE: -ProhibitOperatorMod
// !DIAGNOSTICS: -UNUSED_PARAMETER

class OldAndNew {
    <!DEPRECATED_BINARY_MOD!>operator<!> fun modAssign(x: Int) {}
    operator fun remAssign(x: Int) {}
}

class OnlyOld {
    <!DEPRECATED_BINARY_MOD!>operator<!> fun modAssign(x: Int) {}
}

class OnlyNew {
    operator fun remAssign(x: Int) {}
}

class Sample

<!DEPRECATED_BINARY_MOD!>operator<!> fun Sample.modAssign(x: Int) {}
operator fun Sample.remAssign(x: Int) {}

class ModAndRemAssign {
    <!DEPRECATED_BINARY_MOD!>operator<!> fun mod(x: Int): ModAndRemAssign = ModAndRemAssign()
    operator fun remAssign(x: Int) {}
}

fun test() {
    konst oldAndNew = OldAndNew()
    oldAndNew %= 1

    konst onlyOld = OnlyOld()
    <!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>onlyOld %= 1<!>

    konst onlyNew = OnlyNew()
    onlyNew %= 1

    konst sample = Sample()
    sample %= 1

    var modAndRemAssign = ModAndRemAssign()
    modAndRemAssign %= 1
}
