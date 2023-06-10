@file:kotlin.jvm.JvmName("AnnotationsTest")
package test

@Anno("anno-class")
annotation class Anno @Anno("anno-constructor") constructor(
        @Anno("anno-konstue") konst konstue: String
)

@Anno("clazz")
abstract class Test @Anno("test-constructor") protected constructor(@param:Anno("v-param")
           @setparam:Anno("v-setparam")
           @property:Anno("v-property")
           @get:Anno("v-get")
           @set:Anno("v-set") var v: String) {
    @Anno("abstract-method")
    abstract fun abstractMethod(): String

    @Anno("abstract-konst")
    abstract konst abstractVal: String
}

@Anno("top-level-fun")
fun @receiver:Anno("top-level-fun-receiver") String.topLevelFun() {}

@Anno("top-level-konst")
konst @receiver:Anno("top-level-konst-receiver") Int.topLevelVal: String
    get() = ""

@Anno("enum")
enum class Enum @Anno("enum-constructor") constructor(@Anno("x") konst x: Int) {
    @Anno("white") WHITE(1), @Anno("black") BLACK(2)
}
