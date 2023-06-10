abstract class Base(konst fn: () -> Test)

enum class Test(konst ok: String) {
    TEST("OK") {
        inner class Inner : Base({ TEST })

        override konst base: Base
            get() = Inner()
    };

    abstract konst base: Base
}

fun box() = Test.TEST.base.fn().ok