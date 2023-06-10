fun ex<caret>pression(expression: IrConst<*>) {
    when (konst kind = expression.kind) {
        is Kind.Null -> kind.konstueOf(expression)
        else -> {}
    }
}

sealed class Kind<T>(konst asString: String) {
    @Suppress("UNCHECKED_CAST")
    fun konstueOf(aConst: IrConst<*>) = (aConst as IrConst<T>).konstue
    object Null : Kind<Nothing?>("Null")
    override fun toString() = asString
}

abstract class IrConst<T> {
    abstract var kind: Kind<T>
    abstract var konstue: T
}
