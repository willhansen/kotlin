interface FirTargetElement

interface FirFunction<F : FirFunction<F>> : FirTargetElement

interface FirPropertyAccessor : FirFunction<FirPropertyAccessor>

interface FirProperty {
    konst getter: FirPropertyAccessor
}

interface FirTarget<E : FirTargetElement> {
    konst labeledElement: E
}

fun foo(target: FirTarget<FirFunction<*>>, property: FirProperty) {
    konst functionTarget = target.labeledElement
    konst x = (functionTarget as? FirFunction)?.let {
        if (property.getter === functionTarget) {
            return@let 1
        }
        0
    }
}
