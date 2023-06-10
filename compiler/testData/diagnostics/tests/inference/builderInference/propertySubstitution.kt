// FIR_IDENTICAL
// WITH_STDLIB

interface FirJavaClass2 {
    konst superTypeRefs: MutableList<FirTypeRef2>
}

interface ConeKotlinType2

class ClassId2

konst ConeKotlinType2.classId: ClassId2? get() = null

interface FirSignatureEnhancement2 {
    fun enhanceSuperType(type: FirTypeRef2): FirTypeRef2 = TODO()
}

interface FirTypeRef2

konst FirTypeRef2.coneType: ConeKotlinType2 get() = TODO()

private fun FirJavaClass2.getPurelyImplementedSupertype(): ConeKotlinType2? = null

inline fun buildResolvedTypeRef2(init: FirResolvedTypeRefBuilder2.() -> Unit): FirResolvedTypeRef2 = TODO()

interface FirResolvedTypeRef2 : FirTypeRef2

class FirResolvedTypeRefBuilder2 {
    lateinit var type: ConeKotlinType2
}

fun foo(firJavaClass: FirJavaClass2, enhancement: FirSignatureEnhancement2) {
    konst enhancedSuperTypes = buildList {
        konst purelyImplementedSupertype = firJavaClass.getPurelyImplementedSupertype()
        konst purelyImplementedSupertypeClassId = purelyImplementedSupertype?.classId
        firJavaClass.superTypeRefs.mapNotNullTo(this) { superType ->
            enhancement.enhanceSuperType(superType).takeUnless {
                purelyImplementedSupertypeClassId != null && it.coneType.classId == purelyImplementedSupertypeClassId
            }
        }
        purelyImplementedSupertype?.let {
            add(buildResolvedTypeRef2 { type = it })
        }
    }
}
