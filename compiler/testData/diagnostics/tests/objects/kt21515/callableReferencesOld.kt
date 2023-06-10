// !LANGUAGE: -ProhibitVisibilityOfNestedClassifiersFromSupertypesOfCompanion
// See KT-21515 for a class diagram and details

// class is to prevent accidental short-name import
class O {
    open class Alpha {
        open fun foo() = 42

        class FromAlpha {
            fun foo() = 42
        }

        companion object {
            class FromCompanionAlpha {
                fun foo() = 42
            }
        }
    }

    open class Beta : Alpha() {
        override fun foo() = 42

        class FromBeta {
            fun foo() = 42
        }

        companion object {
            class FromCompanionBeta {
                fun foo() = 42
            }
        }
    }


    open class A {
        open fun foo() = 42

        class FromA {
            fun foo() = 42
        }

        companion object : Beta() {
            class FromCompanionA {
                fun foo() = 42
            }
        }
    }

//////////////////////////

    open class FarAway {
        open fun foo() = 42

        class FromFarAway {
            fun foo() = 42
        }

    }

    open class Gamma {
        open fun foo() = 42

        class FromGamma {
            fun foo() = 42
        }

        companion object : FarAway() {
            class FromCompanionGamma {
                fun foo() = 42
            }
        }
    }

    open class B : A() {
        override fun foo() = 42

        class FromB {
            fun foo() = 42
        }

        companion object : Gamma() {
            override fun foo() = 42

            class FromCompanionB {
                fun foo() = 42
            }
        }
    }
}

///////////////////////////////


open class Delta {
    open fun foo() = 42
    class FromDelta {
        fun foo() = 42
    }
}

class C : O.B() {
    override fun foo() = 42

    companion object : Delta() {
        class FromCompanionC {
            fun foo() = 42
        }
    }

    // VISIBLE: Classifiers from direct superclasses
    konst c = FromA::foo
    konst d = FromB::foo

    // VISIBLE: Classifiers from our own companion
    konst n = FromCompanionC::foo

    // INVISIBLE: direct superclasses themselves.
    konst a = <!UNRESOLVED_REFERENCE!>A<!>::<!DEBUG_INFO_MISSING_UNRESOLVED!>foo<!>
    konst b = <!UNRESOLVED_REFERENCE!>A<!>::<!DEBUG_INFO_MISSING_UNRESOLVED!>foo<!>

    // DEPRECATED: Classifiers from companions of direct superclasses
    konst e = <!DEPRECATED_ACCESS_BY_SHORT_NAME!>FromCompanionA<!>::foo
    konst f = <!DEPRECATED_ACCESS_BY_SHORT_NAME!>FromCompanionB<!>::foo

    // INVISIBLE: "cousin" supertypes themselves
    konst g = <!UNRESOLVED_REFERENCE!>Alpha<!>::<!DEBUG_INFO_MISSING_UNRESOLVED!>foo<!>
    konst h = <!UNRESOLVED_REFERENCE!>Beta<!>::<!DEBUG_INFO_MISSING_UNRESOLVED!>foo<!>
    konst i = <!UNRESOLVED_REFERENCE!>Gamma<!>::<!DEBUG_INFO_MISSING_UNRESOLVED!>foo<!>

    // DEPRECATED: classifiers from "cousin" superclasses
    konst k = <!DEPRECATED_ACCESS_BY_SHORT_NAME!>FromAlpha<!>::foo
    konst l = <!DEPRECATED_ACCESS_BY_SHORT_NAME!>FromBeta<!>::foo
    konst m = <!DEPRECATED_ACCESS_BY_SHORT_NAME!>FromGamma<!>::foo

    // INVISIBLE: We don't see classifiers from companions of "cousin" superclasses
    konst o = <!UNRESOLVED_REFERENCE!>FromCompanionAlpha<!>::<!DEBUG_INFO_MISSING_UNRESOLVED!>foo<!>
    konst p = <!UNRESOLVED_REFERENCE!>FromCompanionBeta<!>::<!DEBUG_INFO_MISSING_UNRESOLVED!>foo<!>
    konst q = <!UNRESOLVED_REFERENCE!>FromCompanionGamma<!>::<!DEBUG_INFO_MISSING_UNRESOLVED!>foo<!>

    // DEPRECATED: Classifiers from supertypes of our own companion
    konst r = <!DEPRECATED_ACCESS_BY_SHORT_NAME!>FromDelta<!>::foo
}
