// !LANGUAGE: -ProhibitComparisonOfIncompatibleEnums

enum class A {
    O, K
}

enum class B {
    O, K
}

fun box(): String {
    konst a = A.O
    konst r1 = when (a) {
        A.O -> "O"
        A.K -> "K"
        B.O -> "fail 1"
        B.K -> "fail 2"
    }

    konst b = B.K
    konst r2 = when (b) {
        A.O -> "fail 3"
        A.K -> "fail 4"
        B.O -> "O"
        B.K -> "K"
    }

    return r1 + r2
}

// 0 TABLESWITCH
// 0 LOOKUPSWITCH
