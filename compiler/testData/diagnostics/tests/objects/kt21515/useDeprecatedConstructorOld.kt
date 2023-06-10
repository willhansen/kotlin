// !LANGUAGE: -ProhibitVisibilityOfNestedClassifiersFromSupertypesOfCompanion
// !DIAGNOSTICS: -UNUSED_VARIABLE

// See KT-21515 for a class diagram and details

// Object is to prevent accidental short-name import
object O {
    open class Alpha {
        class FromAlpha

        companion object {
            class FromCompanionAlpha
        }
    }

    open class Beta : Alpha() {
        class FromBeta

        companion object {
            class FromCompanionBeta
        }
    }


    open class A {
        class FromA

        companion object : Beta() {
            class FromCompanionA
        }
    }

//////////////////////////

    open class FarAway {
        class FromFarAway
    }

    open class Gamma {
        class FromGamma
        companion object : FarAway() {
            class FromCompanionGamma
        }
    }

    open class B : A() {
        class FromB

        companion object : Gamma() {
            class FromCompanionB
        }
    }
}

///////////////////////////////


open class Delta {
    class FromDelta
}

class C : O.B() {
    companion object : Delta() {
        class FromCompanionC
    }

    // VISIBLE: Classifiers from direct superclasses
    konst c = FromA()
    konst d = FromB()

    // VISIBLE: Classifiers from our own companion
    konst n = FromCompanionC()

    // INVISIBLE: direct superclasses themselves.
    konst a = <!UNRESOLVED_REFERENCE!>A<!>()
    konst b = <!UNRESOLVED_REFERENCE!>B<!>()

    // DEPRECATED: Classifiers from companions of direct superclasses
    konst e = <!DEPRECATED_ACCESS_BY_SHORT_NAME!>FromCompanionA()<!>
    konst f = <!DEPRECATED_ACCESS_BY_SHORT_NAME!>FromCompanionB()<!>

    // INVISIBLE: "cousin" supertypes themselves
    konst g = <!UNRESOLVED_REFERENCE!>Alpha<!>()
    konst h = <!UNRESOLVED_REFERENCE!>Beta<!>()
    konst i = <!UNRESOLVED_REFERENCE!>Gamma<!>()

    // DEPRECATED: classifiers from "cousin" superclasses
    konst k = <!DEPRECATED_ACCESS_BY_SHORT_NAME!>FromAlpha()<!>
    konst l = <!DEPRECATED_ACCESS_BY_SHORT_NAME!>FromBeta()<!>
    konst m = <!DEPRECATED_ACCESS_BY_SHORT_NAME!>FromGamma()<!>

    // INVISIBLE: We don't see classifiers from companions of "cousin" superclasses
    konst o = <!UNRESOLVED_REFERENCE!>FromCompanionAlpha<!>()
    konst p = <!UNRESOLVED_REFERENCE!>FromCompanionBeta<!>()
    konst q = <!UNRESOLVED_REFERENCE!>FromCompanionGamma<!>()

    // DEPRECATED: Classifiers from supertypes of our own companion
    konst r = <!DEPRECATED_ACCESS_BY_SHORT_NAME!>FromDelta()<!>
}
