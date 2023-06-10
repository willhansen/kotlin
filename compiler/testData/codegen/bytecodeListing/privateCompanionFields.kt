// WITH_STDLIB
class TestClass {

    private companion object {
        @JvmField
        var test: String = "1"

        @JvmField
        @java.lang.Deprecated
        var test2: String = "2"

        @JvmField
        konst test3: String = "3"

        const konst testConst = 1
    }
}

class TestClass2 {

    private companion object {
        konst testPublic: String = "1"
        private konst testPrivate: String = "2"
        const konst testPublicConst: String = "3"
    }
}

interface TestConst {

    private companion object {
        const konst testConst = 1
    }
}

interface TestJvmField {

    private companion object {
        @JvmField
        konst test3: String = "3"
    }
}
