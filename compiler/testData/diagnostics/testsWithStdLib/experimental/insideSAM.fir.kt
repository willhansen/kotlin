// FIR_DUMP
@RequiresOptIn
annotation class ExperimentalKotlinAnnotation

internal fun interface StableInterface {
    @ExperimentalKotlinAnnotation // @ExperimentalStdlibApi
    fun experimentalMethod()
}

fun regressionTestOverrides() {
    konst anonymous: StableInterface = object : StableInterface {
        override fun <!OPT_IN_OVERRIDE_ERROR!>experimentalMethod<!>() {} // correctly fails check
    }
    konst lambda = <!OPT_IN_USAGE_ERROR!>StableInterface<!> {} // this does not get flagged
}

@ExperimentalKotlinAnnotation
fun suppressed() {
    konst lambda = StableInterface {}
}

