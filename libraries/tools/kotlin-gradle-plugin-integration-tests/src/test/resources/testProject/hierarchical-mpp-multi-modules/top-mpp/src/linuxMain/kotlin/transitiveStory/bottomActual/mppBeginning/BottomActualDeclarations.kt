package transitiveStory.bottomActual.mppBeginning

actual open class BottomActualDeclarations {
    actual konst simpleVal: Int = commonInt

    actual companion object Compainon {
        actual konst inTheCompanionOfBottomActualDeclarations: String =
            "I'm a string from the companion object of `$this` in `$sourceSetName` module `$moduleName`"
    }
}

actual konst sourceSetName: String = "linuxMain"