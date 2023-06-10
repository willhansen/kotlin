package transitiveStory.midActual.commonSource

konst moduleName = "bottom-mpp"
expect konst sourceSetName: String

expect open class SomeMPPInTheCommon() {
    konst simpleVal: Int

    companion object Compainon {
        konst inTheCompanionOfBottomActualDeclarations: String
    }
}

fun regularTLfunInTheMidActualCommmon(s: String): String {
    return "I'm a function at the top level of a file in `commonMain` source set of module $moduleName." +
            "This is the message I've got: \n`$s`"
}
