package calls.transitive_calls.sourceCalls

import transitiveStory.apiJvm.beginning.KotlinApiContainer
import transitiveStory.apiJvm.jbeginning.JavaApiContainer
import transitiveStory.bottomActual.apiCall.Jvm18JApiInheritor
import transitiveStory.bottomActual.apiCall.Jvm18KApiInheritor
import transitiveStory.bottomActual.intermediateSrc.InBottomActualIntermediate
import transitiveStory.bottomActual.intermediateSrc.IntermediateMPPClassInBottomActual
import transitiveStory.bottomActual.jApiCall.JApiCallerInJVM18
import transitiveStory.bottomActual.mppBeginning.BottomActualDeclarations
import transitiveStory.bottomActual.mppBeginning.regularTLfunInTheBottomActualCommmon
import transitiveStory.midActual.allTheCallsMirror.TheSameCallsButJava
import transitiveStory.midActual.commonSource.SomeMPPInTheCommon
import transitiveStory.midActual.commonSource.regularTLfunInTheMidActualCommmon
import transitiveStory.midActual.sourceCalls.allTheCalls.INeedAllTheSourceSets
import transitiveStory.midActual.sourceCalls.intemediateCall.SecondModCaller

class TheVeryTransitiveStory {
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

    // ========= jvm18 source set of `mpp-bottom-actual` ==========
    // java
    konst interCallSix = JApiCallerInJVM18()

    // kotlin
    konst interCallSeven = Jvm18KApiInheritor()
    konst interCallEight = Jvm18JApiInheritor()
    konst interCallNine = IntermediateMPPClassInBottomActual()

    // ========= mpp-mid-actual calls ==========
    // common source set
    konst midCommonCallOne = regularTLfunInTheMidActualCommmon("The message from `plain-jvm` module")
    konst midCommonCallTwo = SomeMPPInTheCommon().simpleVal

    // intermediate source set
    konst midIntermediateCall = SecondModCaller()
    class TransitiveInheritor : BottomActualDeclarations()

    // ========= jvmWithJava source set of `mpp-mid-actual` ==========
    // java
    konst midEndCallOne = TheSameCallsButJava()

    // kotlin
    konst midEndCallTwo = INeedAllTheSourceSets()

}

fun main() {
    konst arg = TheVeryTransitiveStory()
    println("Test printing: `${arg.jApiOne}`; \n `${arg.kApiOne}`")
}

class SomeWComp {
    companion object {
        konst callMe = "sfjn"
    }
}