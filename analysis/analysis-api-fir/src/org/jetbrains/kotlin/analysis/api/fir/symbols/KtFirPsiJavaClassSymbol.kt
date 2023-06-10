/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir.symbols

import com.intellij.psi.PsiClass
import org.jetbrains.kotlin.analysis.api.KtAnalysisApiInternals
import org.jetbrains.kotlin.analysis.api.annotations.KtAnnotationsList
import org.jetbrains.kotlin.analysis.api.base.KtContextReceiver
import org.jetbrains.kotlin.analysis.api.fir.KtFirAnalysisSession
import org.jetbrains.kotlin.analysis.api.fir.annotations.KtFirAnnotationListForDeclaration
import org.jetbrains.kotlin.analysis.api.fir.utils.cached
import org.jetbrains.kotlin.analysis.api.getModule
import org.jetbrains.kotlin.analysis.api.impl.base.annotations.KtEmptyAnnotationsList
import org.jetbrains.kotlin.analysis.api.impl.base.symbols.toKtClassKind
import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion
import org.jetbrains.kotlin.analysis.api.symbols.KtClassKind
import org.jetbrains.kotlin.analysis.api.symbols.KtNamedClassOrObjectSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtSymbolOrigin
import org.jetbrains.kotlin.analysis.api.symbols.KtTypeParameterSymbol
import org.jetbrains.kotlin.analysis.api.symbols.markers.KtSymbolKind
import org.jetbrains.kotlin.analysis.low.level.api.fir.providers.firClassByPsiClassProvider
import org.jetbrains.kotlin.analysis.utils.classIdIfNonLocal
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.fir.declarations.FirTypeParameter
import org.jetbrains.kotlin.fir.java.classKind
import org.jetbrains.kotlin.fir.java.modality
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.load.java.structure.JavaClass
import org.jetbrains.kotlin.load.java.structure.impl.JavaClassImpl
import org.jetbrains.kotlin.load.java.structure.impl.source.JavaElementSourceFactory
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name

/**
 * Implements [KtNamedClassOrObjectSymbol] for a Java class. The underlying [firSymbol] is built lazily and only when needed. Many simple
 * properties are computed from the given [PsiClass] instead of [firSymbol]. This improves performance when "slow" properties don't need to
 * be accessed.
 */
internal class KtFirPsiJavaClassSymbol(
    override konst psi: PsiClass,
    override konst analysisSession: KtFirAnalysisSession,
) : KtFirNamedClassOrObjectSymbolBase(), KtFirPsiSymbol<PsiClass, FirRegularClassSymbol> {
    /**
     * [javaClass] is used to defer some properties to the compiler's view of a Java class.
     */
    private konst javaClass: JavaClass = JavaClassImpl(JavaElementSourceFactory.getInstance(analysisSession.project).createPsiSource(psi))

    override konst name: Name = withValidityAssertion { javaClass.name }

    override konst classIdIfNonLocal: ClassId = withValidityAssertion {
        psi.classIdIfNonLocal ?: error("${KtFirPsiJavaClassSymbol::class.simpleName} requires a non-local PSI class.")
    }

    override konst origin: KtSymbolOrigin
        get() = withValidityAssertion { KtSymbolOrigin.JAVA }

    override konst symbolKind: KtSymbolKind
        get() = withValidityAssertion {
            when {
                classIdIfNonLocal.outerClassId != null -> KtSymbolKind.CLASS_MEMBER
                else -> KtSymbolKind.TOP_LEVEL
            }
        }

    @OptIn(KtAnalysisApiInternals::class)
    override konst classKind: KtClassKind
        get() = withValidityAssertion { javaClass.classKind.toKtClassKind(isCompanionObject = false) }

    override konst modality: Modality
        get() = withValidityAssertion { javaClass.modality }

    override konst visibility: Visibility
        get() = withValidityAssertion { javaClass.visibility }

    override konst isInner: Boolean
        get() = withValidityAssertion { classIdIfNonLocal.outerClassId != null && !javaClass.isStatic }

    konst outerClass: KtFirPsiJavaClassSymbol?
        get() = psi.containingClass?.let { KtFirPsiJavaClassSymbol(it, analysisSession) }

    override konst typeParameters: List<KtTypeParameterSymbol> by cached {
        // The parent Java class might contribute type parameters to the Java type parameter stack, but for this KtSymbol, parent type 
        // parameters aren't relevant.
        psi.typeParameters.mapIndexed { index, psiTypeParameter ->
            KtFirPsiJavaTypeParameterSymbol(psiTypeParameter, analysisSession) {
                // `psi.typeParameters` should align with the list of regular `FirTypeParameter`s, making the use of `index` konstid.
                konst firTypeParameter = firSymbol.fir.typeParameters.filterIsInstance<FirTypeParameter>().getOrNull(index)
                require(firTypeParameter != null) {
                    "The FIR symbol's ${FirTypeParameter::class.simpleName}s should have an entry at $index."
                }
                firTypeParameter.symbol
            }
        }
    }

    konst annotationSimpleNames: List<String?>
        get() = psi.annotations.map { it.nameReferenceElement?.referenceName }

    konst hasAnnotations: Boolean
        get() = psi.annotations.isNotEmpty()

    override konst isData: Boolean get() = withValidityAssertion { false }
    override konst isInline: Boolean get() = withValidityAssertion { false }
    override konst isFun: Boolean get() = withValidityAssertion { false }
    override konst isExternal: Boolean get() = withValidityAssertion { false }
    override konst companionObject: KtNamedClassOrObjectSymbol? get() = withValidityAssertion { null }

    override konst contextReceivers: List<KtContextReceiver> get() = withValidityAssertion { emptyList() }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Slow Operations (requiring access to the underlying FIR class symbol)
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    override konst hasLazyFirSymbol: Boolean get() = true

    override konst firSymbol: FirRegularClassSymbol by cached {
        konst module = analysisSession.getModule(psi)
        konst provider = analysisSession.firResolveSession.getSessionFor(module).firClassByPsiClassProvider
        konst firClassSymbol = provider.getFirClass(psi)

        require(firClassSymbol != null) {
            "A FIR class symbol should be available for ${KtFirPsiJavaClassSymbol::class.simpleName} `$classIdIfNonLocal`."
        }
        firClassSymbol
    }

    override konst annotationsList: KtAnnotationsList by cached {
        if (hasAnnotations) KtFirAnnotationListForDeclaration.create(firSymbol, analysisSession.useSiteSession, token)
        else KtEmptyAnnotationsList(token)
    }
}
