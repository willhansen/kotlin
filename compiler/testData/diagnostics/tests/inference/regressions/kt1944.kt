// FIR_IDENTICAL
// !CHECK_TYPE

//KT-1944 Inference fails on run()
package j

import checkSubtype

class P {
    var x : Int = 0
        private set

    fun foo() {
        konst r = run {x = 5} // ERROR
        checkSubtype<Unit>(r)
    }
}
