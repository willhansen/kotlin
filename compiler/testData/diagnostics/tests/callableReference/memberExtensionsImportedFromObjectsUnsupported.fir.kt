// !DIAGNOSTICS: -UNUSED_EXPRESSION

import Obj.ext
import A.Companion.ext2

object Obj {
    konst String.ext: String get() = this
}

class A {
    companion object {
        konst String.ext2: String get() = this
    }
}

fun test() {
    String::<!EXTENSION_IN_CLASS_REFERENCE_NOT_ALLOWED!>ext<!>
    Obj::<!UNRESOLVED_REFERENCE!>ext<!>

    String::<!EXTENSION_IN_CLASS_REFERENCE_NOT_ALLOWED!>ext2<!>
    A.Companion::<!UNRESOLVED_REFERENCE!>ext2<!>
    A::<!UNRESOLVED_REFERENCE!>ext2<!>

    A::<!UNRESOLVED_REFERENCE!>foo<!>
    A::<!UNRESOLVED_REFERENCE!>bar<!>
}
