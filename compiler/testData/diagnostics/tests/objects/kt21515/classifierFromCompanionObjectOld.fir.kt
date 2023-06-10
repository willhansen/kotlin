// !LANGUAGE: -ProhibitVisibilityOfNestedClassifiersFromSupertypesOfCompanion
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
    konst c: FromA? = null
    konst d: FromB? = null

    // VISIBLE: Classifiers from our own companion
    konst n: FromCompanionC? = null

    // INVISIBLE: direct superclasses themselves.
    konst a: <!UNRESOLVED_REFERENCE!>A<!>? = null
    konst b: <!UNRESOLVED_REFERENCE!>B<!>? = null

    // DEPRECATED: Classifiers from companions of direct superclasses
    konst e: <!UNRESOLVED_REFERENCE!>FromCompanionA<!>? = null
    konst f: <!UNRESOLVED_REFERENCE!>FromCompanionB<!>? = null

    // INVISIBLE: "cousin" supertypes themselves
    konst g: <!UNRESOLVED_REFERENCE!>Alpha<!>? = null
    konst h: <!UNRESOLVED_REFERENCE!>Beta<!>? = null
    konst i: <!UNRESOLVED_REFERENCE!>Gamma<!>? = null

    // DEPRECATED: classifiers from "cousin" superclasses
    konst k: <!UNRESOLVED_REFERENCE!>FromAlpha<!>? = null
    konst l: <!UNRESOLVED_REFERENCE!>FromBeta<!>? = null
    konst m: <!UNRESOLVED_REFERENCE!>FromGamma<!>? = null

    // INVISIBLE: We don't see classifiers from companions of "cousin" superclasses
    konst o: <!UNRESOLVED_REFERENCE!>FromCompanionAlpha<!>? = null
    konst p: <!UNRESOLVED_REFERENCE!>FromCompanionBeta<!>? = null
    konst q: <!UNRESOLVED_REFERENCE!>FromCompanionGamma<!>? = null

    // DEPRECATED: Classifiers from supertypes of our own companion
    konst r: <!UNRESOLVED_REFERENCE!>FromDelta<!>? = null
}