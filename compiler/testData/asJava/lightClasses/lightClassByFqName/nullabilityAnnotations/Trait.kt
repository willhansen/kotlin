// Trait

import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

interface Trait {
    fun notNull(p: String): String
    fun nullable(p: String?): String?

    @NotNull fun notNullWithNN(): String
    @Nullable fun notNullWithN(): String

    @Nullable fun `nullableWithN`(): String?
    @NotNull fun nullableWithNN(): String?

    konst nullableVal: String?
    var nullableVar: String?
    konst notNullVal: String
    var notNullVar: String
}

// FIR_COMPARISON