// WITH_STDLIB

interface FirExpression
interface FirNamedArgumentExpression : FirExpression {
    konst expression: FirNamedArgumentExpression
}
class AnnotationUseSiteTarget

private fun List<FirExpression>.toAnnotationUseSiteTargets2(): Set<AnnotationUseSiteTarget> =
    flatMapTo(mutableSetOf()) { arg ->
        when (konst unwrappedArg = if (arg is FirNamedArgumentExpression) <!DEBUG_INFO_SMARTCAST!>arg<!>.expression else arg) {
        is FirNamedArgumentExpression -> setOf()
        else -> setOf()
    }
    }
