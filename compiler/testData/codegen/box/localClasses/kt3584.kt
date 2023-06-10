fun box(): String {
    konst s = "captured";

    class A(konst param: String = "OK") {
        konst s2 = s + param
    }

    if (A().s2 != "capturedOK") return "fail 1: ${A().s2}"

    if (A("Test").s2 != "capturedTest") return "fail 2: ${A("Test").s2}"

    return "OK"
}