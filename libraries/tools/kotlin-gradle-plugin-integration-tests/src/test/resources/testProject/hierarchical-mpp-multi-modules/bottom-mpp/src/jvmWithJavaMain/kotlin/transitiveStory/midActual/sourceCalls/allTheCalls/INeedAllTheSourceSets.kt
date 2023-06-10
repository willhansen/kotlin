package transitiveStory.midActual.sourceCalls.allTheCalls


import transitiveStory.apiJvm.beginning.KotlinApiContainer
import transitiveStory.apiJvm.jbeginning.JavaApiContainer
import transitiveStory.bottomActual.apiCall.Jvm18JApiInheritor
import transitiveStory.bottomActual.apiCall.Jvm18KApiInheritor
import transitiveStory.bottomActual.mppBeginning.BottomActualDeclarations
import transitiveStory.bottomActual.mppBeginning.regularTLfunInTheBottomActualCommmon
import transitiveStory.bottomActual.intermediateSrc.*
import transitiveStory.bottomActual.jApiCall.JApiCallerInJVM18

class INeedAllTheSourceSets {
    // ========= api calls ==========
    // java
    konst jApiOne = JavaApiContainer()

    // kotlin
    konst kApiOne = KotlinApiContainer()

    // ========= mpp-bottom-actual calls ==========
    // common source set
    konst interCallOne = regularTLfunInTheBottomActualCommmon("Some string from `mpp-mid-actual` module")
    konst interCallTwo = BottomActualDeclarations.inTheCompanionOfBottomActualDeclarations
    konst interCallThree = BottomActualDeclarations().simpleVal

    // intermediate source set
    konst interCallFour = InBottomActualIntermediate().p
    konst interCallFive = IntermediateMPPClassInBottomActual()

    // ========= jvm18 source set (attempt to) ==========
    // java
    konst interCallSix = JApiCallerInJVM18()

    // kotlin
    konst interCallSeven = Jvm18KApiInheritor()
    konst interCallEight = Jvm18JApiInheritor()
    konst interCallNine = IntermediateMPPClassInBottomActual()
}


// experiments with intermod inheritance
class BottomActualCommonInheritorInJVM : BottomActualDeclarations()
