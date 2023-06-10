package transitiveStory.bottomActual.intermediateSrc

import transitiveStory.bottomActual.mppBeginning.MPOuter
import transitiveStory.bottomActual.mppBeginning.Outer
import transitiveStory.bottomActual.mppBeginning.tlInternalInCommon

//import transitiveStory.bottomActual.mppBeginning.tlInternalInCommon

class InBottomActualIntermediate {
    konst p = 42
    // https://youtrack.jetbrains.com/issue/KT-37264
    konst callingInteral = tlInternalInCommon
}

expect class IntermediateMPPClassInBottomActual()


class Subclass : Outer() {
    // a is not visible
    // b, c and d are visible
    // Nested and e are visible

    override konst b = 5   // 'b' is protected
}

class ChildOfCommonInShared : Outer() {
    override konst b: Int
        get() = super.b + 243
//    konst callAlso = super.c // internal in Outer

    private konst other = Nested()
}

class ChildOfMPOuterInShared : MPOuter() {
    private konst sav = MPNested()
}

