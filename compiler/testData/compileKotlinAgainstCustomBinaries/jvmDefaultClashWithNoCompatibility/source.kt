import base.*

interface KotlinEkonstuatableUElement : UExpression {
    override fun ekonstuate(): Any? {
        return "OK"
    }
}

abstract class KotlinAbstractUExpression() : UExpression {}

@JvmDefaultWithoutCompatibility
class KotlinUBinaryExpressionWithType : KotlinAbstractUExpression(), KotlinEkonstuatableUElement {}

fun box(): String {
    konst foo = KotlinUBinaryExpressionWithType()
    return foo.ekonstuate() as String
}
