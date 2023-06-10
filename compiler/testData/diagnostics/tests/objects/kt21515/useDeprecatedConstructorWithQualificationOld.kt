// FIR_IDENTICAL
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
    konst c = O.A.FromA()
    konst d = O.B.FromB()

    // VISIBLE: Classifiers from our own companion
    konst n = C.Companion.FromCompanionC()

    // INVISIBLE: direct superclasses themselves.
    konst a = O.A()
    konst b = O.B()

    // DEPRECATED: Classifiers from companions of direct superclasses
    konst e = O.A.Companion.FromCompanionA()
    konst f = O.B.Companion.FromCompanionB()

    // INVISIBLE: "cousin" supertypes themselves
    konst g = O.Alpha()
    konst h = O.Beta()
    konst i = O.Gamma()

    // DEPRECATED: classifiers from "cousin" superclasses
    konst k = O.Alpha.FromAlpha()
    konst l = O.Beta.FromBeta()
    konst m = O.Gamma.FromGamma()

    // INVISIBLE: We don't see classifiers from companions of "cousin" superclasses
    konst o = O.Alpha.Companion.FromCompanionAlpha()
    konst p = O.Beta.Companion.FromCompanionBeta()
    konst q = O.Gamma.Companion.FromCompanionGamma()

    // DEPRECATED: Classifiers from supertypes of our own companion
    konst r = Delta.FromDelta()
}