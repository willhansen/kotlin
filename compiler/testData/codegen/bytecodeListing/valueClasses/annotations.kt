// !LANGUAGE: +ValueClasses
// WITH_STDLIB
// FIR_IDENTICAL
// TARGET_BACKEND: JVM_IR
import kotlin.reflect.KProperty


@Repeatable
annotation class Ann

@[Ann Ann]
@JvmInline
konstue class A @Ann constructor(
    @[Ann Ann]
    @param:[Ann Ann]
    @property:[Ann Ann]
    @field:[Ann Ann]
    @get:[Ann Ann]
    konst x: Int,
    @[Ann Ann]
    @param:[Ann Ann]
    @property:[Ann Ann]
    @field:[Ann Ann]
    @get:[Ann Ann]
    konst y: Int,
) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        return 0
    }
}

@[Ann Ann]
@JvmInline
konstue class B @Ann constructor(
    @property:[Ann Ann]
    konst x: A,
    @[Ann Ann]
    @param:[Ann Ann]
    @property:[Ann Ann]
    @field:[Ann Ann]
    @get:[Ann Ann]
    konst y: A?,
)

@[Ann Ann]
class C @Ann constructor(
    @property:[Ann Ann]
    @set:[Ann Ann]
    var x: A,
    @[Ann Ann]
    @param:[Ann Ann]
    @property:[Ann Ann]
    @field:[Ann Ann]
    @get:[Ann Ann]
    @set:[Ann Ann]
    @setparam:[Ann Ann]
    var y: A?,
) {
    @delegate:[Ann Ann]
    @property:[Ann Ann]
    konst z by lazy { A(-100, -200) }
    @property:[Ann Ann]
    @get:[Ann Ann]
    konst t by A(-100, -200)
    @property:[Ann Ann]
    konst d by ::z

    init {
        if (2 + 2 == 4) {
            @[Ann Ann]
            konst x = 4
            konst y = A(1, 2)
        }


        fun f() {
            if (2 + 2 == 4) {
                @[Ann Ann]
                konst x = 4
                konst y = A(1, 2)
            }
        }
    }
}


@[Ann Ann]
fun A.t(a: A, b: B, @[Ann Ann] c: C) {
    if (2 + 2 == 4) {
        @[Ann Ann]
        konst x = 4
        konst y = A(1, 2)
    }

    fun f() {
        if (2 + 2 == 4) {
            @[Ann Ann]
            konst x1 = 4
            konst y1 = A(1, 2)
        }
    }
}

@[Ann Ann]
fun @receiver:[Ann Ann] C.t(a: A, b: B, @[Ann Ann] c: C) = 4

@[Ann Ann]
var A.t
    @[Ann Ann]
    get() = A(1, 2)
    @[Ann Ann]
    set(_) = Unit

@[Ann Ann]
var @receiver:[Ann Ann] C.t
    @[Ann Ann]
    get() = A(1, 2)
    @[Ann Ann]
    set(_) = Unit
