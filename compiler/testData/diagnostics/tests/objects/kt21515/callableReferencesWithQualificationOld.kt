// FIR_IDENTICAL
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
    konst c = O.A.FromA::foo
    konst d = O.B.FromB::foo

    // VISIBLE: Classifiers from our own companion
    konst n = FromCompanionC::foo

    // INVISIBLE: direct superclasses themselves.
    konst a = O.A::foo
    konst b = O.A::foo

    // DEPRECATED: Classifiers from companions of direct superclasses
    konst e = O.A.Companion.FromCompanionA::foo
    konst f = O.B.Companion.FromCompanionB::foo

    // INVISIBLE: "cousin" supertypes themselves
    konst g = O.Alpha::foo
    konst h = O.Beta::foo
    konst i = O.Gamma::foo

    // DEPRECATED: classifiers from "cousin" superclasses
    konst k = O.Alpha.FromAlpha::foo
    konst l = O.Beta.FromBeta::foo
    konst m = O.Gamma.FromGamma::foo

    // INVISIBLE: We don't see classifiers from companions of "cousin" superclasses
    konst o = O.Alpha.Companion.FromCompanionAlpha::foo
    konst p = O.Beta.Companion.FromCompanionBeta::foo
    konst q = O.Gamma.Companion.FromCompanionGamma::foo

    // DEPRECATED: Classifiers from supertypes of our own companion
    konst r = Delta.FromDelta::foo
}