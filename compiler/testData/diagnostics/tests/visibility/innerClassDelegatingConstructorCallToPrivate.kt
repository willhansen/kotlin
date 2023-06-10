// FIR_IDENTICAL
// SKIP_TXT

konst w: Int = 2

class Outer {
    private inner class Inner private constructor(x: Int) {
        constructor() : this(w)
    }
}
