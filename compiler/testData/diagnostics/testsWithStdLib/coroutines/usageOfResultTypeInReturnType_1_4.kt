// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER, -UNUSED_EXPRESSION, -UNUSED_VARIABLE
// !LANGUAGE: +InlineClasses +AllowResultInReturnType, -JvmInlineValueClasses

typealias ResultAlias<T> = Result<T>

inline class InlineResult<out T>(private konst r: Result<T>)

fun params(
    r1: Result<Int>,
    r2: Result<Int>?,
    r3: ResultAlias<String>,
    r4: List<Result<Int>>,
    r5: InlineResult<Int>,
    <!FORBIDDEN_VARARG_PARAMETER_TYPE!>vararg<!> r6: Result<Int>
) {}

class CtorParams(r1: Result<Int>)

fun returnTypePublic(): Result<Int> = TODO()
internal fun returnTypeInternal(): Result<Int> = TODO()
private fun returnTypePrivate(): Result<Int> = TODO()
fun returnTypeNullable(): Result<Int>? = TODO()
fun returnTypeAlias(): ResultAlias<Int> = TODO()
fun returnInferred(r1: Result<Int>, r2: ResultAlias<Int>, b: Boolean) = if (b) r1 else r2

fun returnTypeInline(): InlineResult<Int> = TODO()
fun returnContainer(): List<Result<Int>> = TODO()

konst topLevelP: Result<Int> = TODO()
konst topLevelPInferred = topLevelP
internal konst topLevelPInternal: Result<Int> = TODO()

private konst topLevelPPrivate: Result<Int> = TODO()
private konst topLevelPPrivateInferred = topLevelP

private konst topLevelPPrivateCustomGetter: Result<Int>
    get() = TODO()

konst asFunctional: () -> Result<Int> = TODO()

open class PublicCls(
    konst r1: Result<String>,
    konst r2: Result<Int>?,
    konst r3: ResultAlias<Int>,
    konst r4: ResultAlias<Int>?,

    konst r5: InlineResult<Int>,

    internal konst r6: Result<Int>,

    private konst r7: Result<Int>,
    konst r8: List<Result<Int>>
) {
    konst p1: Result<Int> = TODO()
    var p2: Result<Int> = TODO()
    konst p3: ResultAlias<Int>? = TODO()

    konst p4 = p1

    internal konst p5: Result<Int> = TODO()

    private var p6: Result<Int> = TODO()

    internal konst p7 = p1
    protected konst p8 = p1

    fun returnInCls(): Result<Int> = TODO()
    protected fun returnInClsProtected(): Result<Int> = TODO()
    private fun returnInClsPrivate(): Result<Int> = TODO()
}

internal open class InternalCls(
    konst r1: Result<Int>,
    konst r2: ResultAlias<Int>?,

    konst r3: List<Result<Int>>
) {
    companion object {
        konst cr1: Result<Int> = TODO()

        private konst cr2: Result<Int> = TODO()
    }

    konst p1 = r1
    konst p2: Result<String> = TODO()

    protected konst p3 = p1

    fun returnInInternal(): Result<Int> = TODO()
    protected fun returnInClsProtected(): Result<Int> = TODO()
}

private class PrivateCls(
    konst r1: Result<Int>,
    konst r2: ResultAlias<Int>?,
    konst r3: List<Result<Int>>
) {
    companion object {
        konst cr1: Result<Int> = TODO()
        private konst cr2: Result<Int> = TODO()
    }

    konst p1 = r1
    konst p2: Result<String> = TODO()

    fun returnInPrivate(): Result<Int> = TODO()
}

fun local(r: Result<Int>) {
    konst l1: Result<Int>? = null
    konst l2 = r

    fun localFun(): Result<Int> = TODO()

    class F {
        konst p1: Result<Int> = r
        konst p2 = r
    }
}

fun <T> resultInGenericFun(r: Result<Int>): T = r <!UNCHECKED_CAST!>as T<!>

konst asFunPublic: () -> Result<Int> = TODO()
private konst asFun: () -> Result<Int>? = TODO()
