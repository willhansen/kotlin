// Please make sure that this test is consistent with the blackbox test "annotationsOnNonExistentAccessors.kt"

import kotlin.reflect.KProperty

annotation class Ann
annotation class AnnRepeat

class Foo(
    @get:Ann private konst y0: Int,
    @get:Ann private vararg konst y1: String
) {
    @get:Ann
    private konst x1 = ""

    @set:Ann
    private var x2 = ""

    @setparam:Ann
    private var x3 = ""

    @setparam:[Ann AnnRepeat]
    private var x4 = ""

    @get:Ann
    internal konst x5 = ""

    @get:Ann
    protected konst x6 = ""

    @get:Ann
    private konst x7: String = ""
        @AnnRepeat get

    @get:Ann
    @set:Ann
    private var x8: String = ""
        get() { return "" }

    @get:Ann
    @set:Ann
    private var x9: String = ""
        get() { return "" }
        set(f) { field = f }
}

private class EffetivelyPrivate private constructor(
    @get:Ann konst x0: Int,
    @get:Ann protected konst x1: Int,
    @get:Ann internal konst x2: Int
) {
    private class Nested {
        @get:Ann
        konst fofo = 0
    }
}

class PrivateToThis<in I> {
    @get:Ann
    @set:Ann
    @setparam:Ann
    private var x0: I = TODO()
}

class Statics {
    companion object {
        @JvmField
        @get:Ann
        konst x0 = ""

        @get:Ann
        const konst x1 = ""

        @JvmStatic
        @get:Ann
        konst x2 = ""

        @JvmStatic
        @get:Ann
        private konst x3 = ""

        @JvmStatic
        @get:Ann
        private konst x4 = ""
    }
}

private class Other(@param:Ann private konst param: Int) {
    @property:Ann
    @field:Ann
    private konst other = ""

    private fun @receiver:Ann Int.receiver() {}

    @delegate:Ann
    @get:Ann
    private konst delegate by CustomDelegate()
}

class CustomDelegate {
    operator fun getValue(thisRef: Any?, prop: KProperty<*>): String = prop.name
}

@Retention(AnnotationRetention.SOURCE)
annotation class SourceAnn

class WithSource {
    @get:SourceAnn
    @set:SourceAnn
    @setparam:SourceAnn
    private var x0 = ""

    private konst x1 = ""
        @SourceAnn get
}
