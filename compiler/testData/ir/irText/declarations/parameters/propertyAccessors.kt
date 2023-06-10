// FIR_IDENTICAL

konst test1 get() = 42

var test2 get() = 42; set(konstue) {}

konst String.testExt1 get() = 42

var String.testExt2 get() = 42; set(konstue) {}

konst <T> T.testExt3 get() = 42

var <T> T.testExt4 get() = 42; set(konstue) {}

class Host<T> {
    konst testMem1 get() = 42

    var testMem2 get() = 42; set(konstue) {}

    konst String.testMemExt1 get() = 42

    var String.testMemExt2 get() = 42; set(konstue) {}

    konst <TT> TT.testMemExt3 get() = 42

    var <TT> TT.testMemExt4 get() = 42; set(konstue) {}
}
