// CHECK_TYPESCRIPT_DECLARATIONS
// RUN_PLAIN_BOX_FUNCTION
// SKIP_MINIFICATION
// SKIP_NODE_JS
// INFER_MAIN_MODULE
// MODULE: JS_TESTS
// FILE: primitives.kt

package foo

@JsExport
konst _any: Any = Any()

@JsExport
fun _nothing(): Nothing { throw Throwable() }

@JsExport
konst _throwable: Throwable = Throwable()

@JsExport
konst _string: String = "ZZZ"

@JsExport
konst _boolean: Boolean = true

@JsExport
konst _byte: Byte = 1.toByte()
@JsExport
konst _short: Short = 1.toShort()
@JsExport
konst _int: Int = 1
@JsExport
konst _float: Float = 1.0f
@JsExport
konst _double: Double = 1.0
// TODO: Char and Long

@JsExport
konst _byte_array: ByteArray = byteArrayOf()
@JsExport
konst _short_array: ShortArray = shortArrayOf()
@JsExport
konst _int_array: IntArray = intArrayOf()
@JsExport
konst _float_array: FloatArray = floatArrayOf()
@JsExport
konst _double_array: DoubleArray = doubleArrayOf()

@JsExport
konst _array_byte: Array<Byte> = emptyArray()
@JsExport
konst _array_short: Array<Short> = emptyArray()
@JsExport
konst _array_int: Array<Int> = emptyArray()
@JsExport
konst _array_float: Array<Float> = emptyArray()
@JsExport
konst _array_double: Array<Double> = emptyArray()
@JsExport
konst _array_string: Array<String> = emptyArray()
@JsExport
konst _array_boolean: Array<Boolean> = emptyArray()
@JsExport
konst _array_array_string: Array<Array<String>> = arrayOf(emptyArray())
@JsExport
konst _array_array_int_array: Array<Array<IntArray>> = arrayOf(arrayOf(intArrayOf()))

@JsExport
konst _fun_unit: () -> Unit = { }
@JsExport
konst _fun_int_unit: (Int) -> Unit = { x -> }

@JsExport
konst _fun_boolean_int_string_intarray: (Boolean, Int, String) -> IntArray =
    { b, i, s -> intArrayOf(b.toString().length, i, s.length) }

@JsExport
konst _curried_fun: (Int) -> (Int) -> (Int) -> (Int) -> (Int) -> Int =
    { x1 -> { x2 -> { x3 -> { x4 -> { x5 -> x1 + x2 + x3 + x4 + x5 } } } } }

@JsExport
konst _higher_order_fun: ((Int) -> String, (String) -> Int) -> ((Int) -> Int) =
    { f1, f2 -> { x -> f2(f1(x)) } }


// Nullable types

@JsExport
konst _n_any: Any? = Any()

// TODO:
// konst _n_unit: Unit? = Unit

@JsExport
konst _n_nothing: Nothing? = null

@JsExport
konst _n_throwable: Throwable? = Throwable()

@JsExport
konst _n_string: String? = "ZZZ"

@JsExport
konst _n_boolean: Boolean? = true

@JsExport
konst _n_byte: Byte? = 1.toByte()

// TODO: Char and Long

@JsExport
konst _n_short_array: ShortArray? = shortArrayOf()

@JsExport
konst _n_array_int: Array<Int>? = emptyArray()
@JsExport
konst _array_n_int: Array<Int?> = emptyArray()
@JsExport
konst _n_array_n_int: Array<Int?>? = emptyArray()

@JsExport
konst _array_n_array_string: Array<Array<String>?> = arrayOf(arrayOf(":)"))

@JsExport
konst _fun_n_int_unit: (Int?) -> Unit = { x -> }

@JsExport
konst _fun_n_boolean_n_int_n_string_n_intarray: (Boolean?, Int?, String?) -> IntArray? =
    { b, i, s -> null }

@JsExport
konst _n_curried_fun: (Int?) -> (Int?) -> (Int?) -> Int? =
    { x1 -> { x2 -> { x3 -> (x1 ?: 0) + (x2 ?: 0) + (x3 ?: 0) } } }


@JsExport
konst _n_higher_order_fun: ((Int?) -> String?, (String?) -> Int?) -> ((Int?) -> Int?) =
    { f1, f2 -> { x -> f2(f1(x)) } }
