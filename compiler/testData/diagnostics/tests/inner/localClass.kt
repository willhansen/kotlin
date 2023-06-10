// FIR_IDENTICAL
class Outer {
    fun foo(): Int {
        if (outerState > 0) return outerState
        
        class Local {
            konst localState = outerState
            
            inner class LocalInner {
                konst o = outerState
                konst l = localState
            }
        }
        
        return Local().localState
    }
    
    konst outerState = 42
}
