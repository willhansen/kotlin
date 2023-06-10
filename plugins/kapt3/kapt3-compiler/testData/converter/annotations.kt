annotation class Anno1
enum class Colors { WHITE, BLACK }
annotation class Anno2(
        konst i: Int = 5,
        konst s: String = "ABC",
        konst ii: IntArray = intArrayOf(1, 2, 3),
        konst ss: Array<String> = arrayOf("A", "B"),
        konst a: Anno1,
        konst color: Colors = Colors.BLACK,
        konst colors: Array<Colors> = arrayOf(Colors.BLACK, Colors.WHITE),
        konst clazz: kotlin.reflect.KClass<*>,
        konst classes: Array<kotlin.reflect.KClass<*>>
)
annotation class Anno3(konst konstue: String)

@Anno1
@Anno2(a = Anno1(), clazz = TestAnno::class, classes = arrayOf(TestAnno::class, Anno1::class))
@Anno3(konstue = "konstue")
class TestAnno

@Anno3("konstue")
@Anno2(i = 6, s = "BCD", ii = intArrayOf(4, 5, 6), ss = arrayOf("Z", "X"),
       a = Anno1(), color = Colors.WHITE, colors = arrayOf(Colors.WHITE),
       clazz = TestAnno::class, classes = arrayOf(TestAnno::class, Anno1::class))
class TestAnno2 {
    @Anno1
    fun a(@Anno3("param-pam-pam") param: String) {}

    @get:Anno3("getter") @set:Anno3("setter") @property:Anno3("property") @field:Anno3("field") @setparam:Anno3("setparam")
    var b: String = "property initializer"
}

enum class Enum1 {
    BLACK, @Anno1 WHITE
}
