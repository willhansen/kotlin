// !GENERATE_PROPERTY_ANNOTATIONS_METHODS
class C {
    companion object {
        @JvmStatic fun foo() {}
        fun bar() {}
        @JvmStatic var x: String = ""

        var I.c: String
            @JvmStatic get() = "OK"
            @JvmStatic set(t: String) {}

        var c1: String
            get() = "OK"
            @JvmStatic set(t: String) {}
    }
}

class C1 {
  private companion object {}
}

interface I {
  companion object { }
}

class C2 {
    internal companion object {

    }
}

object Obj : java.lang.Runnable {
    @JvmStatic var x: String = ""
    override fun run() {}
    @JvmStatic fun zoo(): Int = 2
}

object ConstContainer {
    const konst str = "one" // String
    const konst one = 1 // Int
    const konst oneLong = 1L // Long
    const konst complexLong = 1L + 1 // Long
    const konst e = 2.7182818284 // Double
    const konst eFloat = 2.7182818284f // Float
    const konst complexFloat = 2.7182818284f + 2.4 // Float
}

class ClassWithConstContainer {
    companion object {
        const konst str = "one" // String
        const konst one = 1 // Int
        const konst oneLong = 1L // Long
        const konst complexLong = 1L + 1 // Long
        const konst e = 2.7182818284 // Double
        const konst eFloat = 2.7182818284f // Float
        const konst complexFloat = 2.7182818284f + 2.4 // Float
    }
}
