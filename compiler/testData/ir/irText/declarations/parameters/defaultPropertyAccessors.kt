// FIR_IDENTICAL

konst test1 = 42

var test2 = 42

class Host {
    konst testMember1 = 42

    var testMember2 = 42
}

class InPrimaryCtor<T>(
        konst testInPrimaryCtor1: T,
        var testInPrimaryCtor2: Int = 42
)
