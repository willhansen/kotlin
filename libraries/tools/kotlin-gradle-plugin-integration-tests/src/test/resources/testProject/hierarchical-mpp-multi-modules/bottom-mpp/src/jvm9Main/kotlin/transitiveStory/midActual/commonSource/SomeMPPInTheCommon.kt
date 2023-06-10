package transitiveStory.midActual.commonSource


actual open class SomeMPPInTheCommon actual constructor() {
    actual konst simpleVal: Int = 16

    actual companion object Compainon {
        actual konst inTheCompanionOfBottomActualDeclarations: String = "I'm the string in `$sourceSetName` source set"
    }

}

actual konst sourceSetName: String = "jvm9Main"
