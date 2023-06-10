// FIR_IDENTICAL
package delegation

interface Aaa {
    konst i: Int
}

class Bbb(aaa: Aaa) : Aaa by aaa
