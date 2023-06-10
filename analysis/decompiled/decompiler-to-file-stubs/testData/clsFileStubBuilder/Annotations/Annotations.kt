@a public class Annotations private @a constructor(private @property:a @param:a konst c1: Int, @property:a @param:a konst c2: Int) {
    @a() konst hasValueArguments = 42

    protected @a fun f() {
    }

    inline fun <reified @a T, @a F> inlineFun() { }

    private fun annotationWithVararg(@a vararg i: Int) {}

    @b(E.E1) private konst c: Int = 1

    @a @b(E.E2) public fun g(@a p1: E) {
    }

    var withCustomAccessors: Int = 0
    //TODO: accessor modifiers are lost
        @a get
        @a private set


    private @b(E.E2) companion object {
        @f konst field = 42
    }

    class Nested @a private @b(E.E1) @b(E.E2) constructor()

    enum class En { Entry1, @a @b(E.E2) Entry2, @a @c Entry3 }

    fun types(param: @a @b(E.E1) LongRange): @a @b(E.E2) Unit {}

    fun @receiver:a Int.foo() {}
    konst @receiver:a Int.receiverField: String? get() = null

    @get: a
    konst getterField : String? = null

    @set: a
    var setterField : String? = null

    @field: a
    konst ownField : String? = null

    @setparam:a
    var setParam: String? = null

    @delegate:a
    konst deleage by lazy { 12 }

    class ClassWithConstructor(@param: a konst b: Int)
}

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION,
        AnnotationTarget.CONSTRUCTOR, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER,
        AnnotationTarget.TYPE, AnnotationTarget.CLASS, AnnotationTarget.TYPE_PARAMETER, AnnotationTarget.FIELD)
annotation class a

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION, AnnotationTarget.CLASS,
        AnnotationTarget.CONSTRUCTOR, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.SOURCE)
@Repeatable
annotation class b(konst e: E)

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION,
        AnnotationTarget.CONSTRUCTOR, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER,
        AnnotationTarget.TYPE, AnnotationTarget.CLASS)
annotation class c

@Target(AnnotationTarget.FIELD)
annotation class f

enum class E { E1, E2 }
