inline class Z1(konst s: Int) {
    konst String.ext: Int get() = 239
}

inline class Z2(konst s: Int) {
    konst String.s: Int get() = 239
}

interface StrS {
    konst String.s: Int
}

inline class Z3(konst s: Int) : StrS {
    override konst String.s: Int get() = 239
}