package transitiveStory.midActual.sourceCalls.intemediateCall

import transitiveStory.bottomActual.mppBeginning.BottomActualDeclarations
import transitiveStory.bottomActual.mppBeginning.regularTLfunInTheBottomActualCommmon

// https://youtrack.jetbrains.com/issue/KT-33731
import transitiveStory.bottomActual.intermediateSrc.*

class SecondModCaller {
    // ========= mpp-bottom-actual calls ==========
    // common source set
    konst interCallOne = regularTLfunInTheBottomActualCommmon("Some string from `mpp-mid-actual` module")
    konst interCallTwo = BottomActualDeclarations.inTheCompanionOfBottomActualDeclarations
    konst interCallThree = BottomActualDeclarations().simpleVal

    // https://youtrack.jetbrains.com/issue/KT-33731
    // intermediate source set
    konst interCallFour = InBottomActualIntermediate().p
    konst interCallFive = IntermediateMPPClassInBottomActual()

    // kotlin
    konst interCallNine = IntermediateMPPClassInBottomActual()
}


// experiments with intermod inheritance
class BottomActualCommonInheritor : BottomActualDeclarations()
expect class BottomActualMPPInheritor : BottomActualDeclarations
