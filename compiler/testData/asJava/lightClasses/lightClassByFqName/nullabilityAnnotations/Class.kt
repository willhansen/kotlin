// Class

import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

class Class {
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
        @[NotNull] get() = ""
        @[NotNull] set(v) {}

    private konst privateNN: String = { "" }()
    private konst privateN: String? = { "" }()

    lateinit var lateInitVar: String
}

