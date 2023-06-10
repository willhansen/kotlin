// FIR_IDENTICAL
// !CHECK_TYPE
// !DIAGNOSTICS: -UNUSED_PARAMETER, -UNUSED_VARIABLE

class AbstractSelector<S, I>
class SelectorFor<S>

inline operator fun <S, I> SelectorFor<S>.invoke(f: S.() -> I): AbstractSelector<S, I> = TODO()

class State(konst p1: Double, konst p2: () -> Int, konst p3: String?)

fun test(s: SelectorFor<State>): Double {
    konst a = s { p1 }
    a checkType { _<AbstractSelector<State, Double>>() }

    konst b = s { p2 }
    b checkType { _<AbstractSelector<State, () -> Int>>()}

    konst c = s { p3 }
    c checkType { _<AbstractSelector<State, String?>>() }

    konst d = s { }
    d checkType { _<AbstractSelector<State, Unit>>() }

    konst e = s { return p1 }
    e checkType { _<AbstractSelector<State, Nothing>>() }

    return null!!
}
