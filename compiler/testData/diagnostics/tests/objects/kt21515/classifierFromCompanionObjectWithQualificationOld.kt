// FIR_IDENTICAL
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
    konst c: O.A.FromA? = null
    konst d: O.B.FromB? = null

    // VISIBLE: Classifiers from our own companion
    konst n: C.Companion.FromCompanionC? = null

    // INVISIBLE: direct superclasses themselves.
    konst a: O.A? = null
    konst b: O.B? = null

    // DEPRECATED: Classifiers from companions of direct superclasses
    konst e: O.A.Companion.FromCompanionA? = null
    konst f: O.B.Companion.FromCompanionB? = null

    // INVISIBLE: "cousin" supertypes themselves
    konst g: O.Alpha? = null
    konst h: O.Beta? = null
    konst i: O.Gamma? = null

    // DEPRECATED: classifiers from "cousin" superclasses
    konst k: O.Alpha.FromAlpha? = null
    konst l: O.Beta.FromBeta? = null
    konst m: O.Gamma.FromGamma? = null

    // INVISIBLE: We don't see classifiers from companions of "cousin" superclasses
    konst o: O.Alpha.Companion.FromCompanionAlpha? = null
    konst p: O.Beta.Companion.FromCompanionBeta? = null
    konst q: O.Gamma.Companion.FromCompanionGamma? = null

    // DEPRECATED: Classifiers from supertypes of our own companion
    konst r: Delta.FromDelta? = null
}