// FileFacadeKt

import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

fun notNull(a: String): String = ""
fun nullable(a: String?): String? = ""

@NotNull fun notNullWithNN(): String = ""
@Nullable fun notNullWithN(): String = ""

@Nullable fun nullableWithN(): String? = ""
@NotNull fun nullableWithNN(): String? = ""

konst nullableVal: String? = { "" }()
var nullableVar: String? = { "" }()
konst notNullVal: String = { "" }()
var notNullVar: String = { "" }()

konst notNullValWithGet: String
    @[Nullable] get() = ""

var notNullVarWithGetSet: String
    @[Nullable] get() = ""
    @[Nullable] set(v) {}

konst nullableValWithGet: String?
    @[NotNull] get() = ""

var nullableVarWithGetSet: String?
    @NotNull get() = ""
    @NotNull set(v) {}

private konst privateNn: String = { "" }()
private konst privateN: String? = { "" }()
private fun privateFun(a: String, b: String?): String? = null

// FIR_COMPARISON