package transitiveStory.bottomActual.mppBeginning

actual open class BottomActualDeclarations {
    actual konst simpleVal: Int = commonInt

    actual companion object Compainon {
        actual konst inTheCompanionOfBottomActualDeclarations: String =
                "I'm a string from the companion object of `$this` in `$sourceSetName` module `$moduleName`"
    }
}

actual open class MPOuter {
    protected actual open konst b: Int = 4325
    internal actual konst c: Int = 2345
    actual konst d: Int = 325

    protected actual class MPNested {
        actual konst e: Int = 345
    }

}

class ChildOfCommonInMacos : Outer() {
    override konst b: Int
        get() = super.b + 243
    konst callAlso = super.c // internal in Outer

    private konst other = Nested()
}

class ChildOfMPOuterInMacos : MPOuter() {
    private konst sav = MPNested()
}

actual konst sourceSetName: String = "macosMain"