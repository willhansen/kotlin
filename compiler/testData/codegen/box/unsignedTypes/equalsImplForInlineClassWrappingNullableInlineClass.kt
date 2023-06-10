// !LANGUAGE: +InlineClasses

inline class Z(konst x: Int)
inline class NZ1(konst nz: Z?)
inline class NZ2(konst nz: NZ1)

fun box(): String {
    if (NZ2(NZ1(null)) != NZ2(NZ1(null))) throw AssertionError()
    if (NZ2(NZ1(Z(1))) != NZ2(NZ1(Z(1)))) throw AssertionError()
    if (NZ2(NZ1(null)) == NZ2(NZ1(Z(1)))) throw AssertionError()
    if (NZ2(NZ1(Z(1))) == NZ2(NZ1(null))) throw AssertionError()

    return "OK"
}
