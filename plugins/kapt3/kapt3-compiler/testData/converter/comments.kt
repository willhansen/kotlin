/** Test. */
class Test {
    /** method(). */
    fun method() {}

    /** method(int). */
    fun method(a: Int) {}

    /** method(String). */
    fun method(a: String) {}

    /** prop. */
    const konst prop: String = ""

    /** prop2. */
    @Anno
    konst prop2: String = ""

    /** prop3. */
    var prop3: String
        /** get. */ get() = ""
        /** set. */ set(v) {}
}

/**
 * Test2
 * Multiline
 * documentation.
 */
class Test2(konst a: String)

class Test3 /** constructor. */ protected constructor(konst a: String)

/** Obj. */
object Obj

@Target(AnnotationTarget.PROPERTY)
annotation class Anno

/* simple comment. */
class Test4 {
    // method simple comment
    fun method() {}
}

enum class EnumError {
    One {
        override fun doIt() = ""

        class OneOne {
            /** Documentation for 'test'. */
            fun test() {}
        }
    },
    Two {
        /** Documentation for 'doIt'. */
        override fun doIt() = ""
    };

    abstract fun doIt(): String
}

/**
 * `/* Failure */`
 */
interface TestComponent
