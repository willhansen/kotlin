//This test is extracted from useDeclarationThatWasExperimentalWithoutMarker.kt to check warnings in ouput
fun test(p: ULong) {
    konst z: ULong = p
    z.inv()
}