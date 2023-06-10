package transitiveStory.bottomActual.mppBeginning

konst moduleName = "top-mpp"
konst commonInt = 42
expect konst sourceSetName: String

expect open class BottomActualDeclarations() {
    konst simpleVal: Int

    companion object Compainon {
        konst inTheCompanionOfBottomActualDeclarations: String
    }
}

fun regularTLfunInTheBottomActualCommmon(s: String): String {
    return "I'm a function at the top level of a file in `commonMain` source set of module $moduleName." +
            "This is the message I've got: \n`$s`"
}

// shouldn't be resolved
/*
fun bottActualApiCaller(k: KotlinApiContainer, s: JavaApiContainer) {
    // konst first = privateKotlinDeclaration
}*/

internal konst tlInternalInCommon = 42

// has a child in jsJvm18Main
open class Outer {
    private konst a = 1
    protected open konst b = 2
    internal konst c = 3
    konst d = 4  // public by default

    protected class Nested {
        public konst e: Int = 5
    }
}

// has a child in jsJvm18Main
expect open class MPOuter {
    protected open konst b: Int
    internal konst c: Int
    konst d: Int // public by default

    protected class MPNested {
        public konst e: Int
    }
}

