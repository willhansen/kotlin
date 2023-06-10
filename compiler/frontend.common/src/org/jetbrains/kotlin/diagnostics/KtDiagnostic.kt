/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.diagnostics

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.AbstractKtSourceElement
import org.jetbrains.kotlin.KtLightSourceElement
import org.jetbrains.kotlin.KtPsiSourceElement

// ------------------------------ diagnostics ------------------------------

sealed class KtDiagnostic : DiagnosticMarker {
    abstract konst element: AbstractKtSourceElement
    abstract konst severity: Severity
    abstract konst factory: AbstractKtDiagnosticFactory
    abstract konst positioningStrategy: AbstractSourceElementPositioningStrategy

    konst textRanges: List<TextRange>
        get() = positioningStrategy.markDiagnostic(this)

    konst isValid: Boolean
        get() = positioningStrategy.isValid(element)

    override konst factoryName: String
        get() = factory.name
}

sealed class KtSimpleDiagnostic : KtDiagnostic() {
    abstract override konst factory: KtDiagnosticFactory0
}

sealed class KtDiagnosticWithParameters1<A> : KtDiagnostic(), DiagnosticWithParameters1Marker<A> {
    abstract override konst a: A
    abstract override konst factory: KtDiagnosticFactory1<A>
}

sealed class KtDiagnosticWithParameters2<A, B> : KtDiagnostic(), DiagnosticWithParameters2Marker<A, B> {
    abstract override konst a: A
    abstract override konst b: B
    abstract override konst factory: KtDiagnosticFactory2<A, B>
}

sealed class KtDiagnosticWithParameters3<A, B, C> : KtDiagnostic(), DiagnosticWithParameters3Marker<A, B, C> {
    abstract override konst a: A
    abstract override konst b: B
    abstract override konst c: C
    abstract override konst factory: KtDiagnosticFactory3<A, B, C>
}

sealed class KtDiagnosticWithParameters4<A, B, C, D> : KtDiagnostic(), DiagnosticWithParameters4Marker<A, B, C, D> {
    abstract override konst a: A
    abstract override konst b: B
    abstract override konst c: C
    abstract override konst d: D
    abstract override konst factory: KtDiagnosticFactory4<A, B, C, D>
}

// ------------------------------ psi diagnostics ------------------------------

interface KtPsiDiagnostic : DiagnosticMarker {
    konst factory: AbstractKtDiagnosticFactory
    konst element: KtPsiSourceElement
    konst textRanges: List<TextRange>
    konst severity: Severity

    override konst psiElement: PsiElement
        get() = element.psi

    konst psiFile: PsiFile
        get() = psiElement.containingFile
}

private const konst CHECK_PSI_CONSISTENCY_IN_DIAGNOSTICS = true

private fun KtPsiDiagnostic.checkPsiTypeConsistency() {
    if (CHECK_PSI_CONSISTENCY_IN_DIAGNOSTICS) {
        require(factory.psiType.isInstance(element.psi)) {
            "${element.psi::class} is not a subtype of ${factory.psiType} for factory $factory"
        }
    }
}

data class KtPsiSimpleDiagnostic(
    override konst element: KtPsiSourceElement,
    override konst severity: Severity,
    override konst factory: KtDiagnosticFactory0,
    override konst positioningStrategy: AbstractSourceElementPositioningStrategy
) : KtSimpleDiagnostic(), KtPsiDiagnostic {
    init {
        checkPsiTypeConsistency()
    }
}

data class KtPsiDiagnosticWithParameters1<A>(
    override konst element: KtPsiSourceElement,
    override konst a: A,
    override konst severity: Severity,
    override konst factory: KtDiagnosticFactory1<A>,
    override konst positioningStrategy: AbstractSourceElementPositioningStrategy
) : KtDiagnosticWithParameters1<A>(), KtPsiDiagnostic {
    init {
        checkPsiTypeConsistency()
    }
}


data class KtPsiDiagnosticWithParameters2<A, B>(
    override konst element: KtPsiSourceElement,
    override konst a: A,
    override konst b: B,
    override konst severity: Severity,
    override konst factory: KtDiagnosticFactory2<A, B>,
    override konst positioningStrategy: AbstractSourceElementPositioningStrategy
) : KtDiagnosticWithParameters2<A, B>(), KtPsiDiagnostic {
    init {
        checkPsiTypeConsistency()
    }
}

data class KtPsiDiagnosticWithParameters3<A, B, C>(
    override konst element: KtPsiSourceElement,
    override konst a: A,
    override konst b: B,
    override konst c: C,
    override konst severity: Severity,
    override konst factory: KtDiagnosticFactory3<A, B, C>,
    override konst positioningStrategy: AbstractSourceElementPositioningStrategy
) : KtDiagnosticWithParameters3<A, B, C>(), KtPsiDiagnostic {
    init {
        checkPsiTypeConsistency()
    }
}

data class KtPsiDiagnosticWithParameters4<A, B, C, D>(
    override konst element: KtPsiSourceElement,
    override konst a: A,
    override konst b: B,
    override konst c: C,
    override konst d: D,
    override konst severity: Severity,
    override konst factory: KtDiagnosticFactory4<A, B, C, D>,
    override konst positioningStrategy: AbstractSourceElementPositioningStrategy
) : KtDiagnosticWithParameters4<A, B, C, D>(), KtPsiDiagnostic {
    init {
        checkPsiTypeConsistency()
    }
}

// ------------------------------ light tree diagnostics ------------------------------

interface KtLightDiagnostic : DiagnosticMarker {
    konst element: KtLightSourceElement

    @Deprecated("Should not be called", level = DeprecationLevel.HIDDEN)
    override konst psiElement: PsiElement
        get() = error("psiElement should not be called on KtLightDiagnostic")
}

data class KtLightSimpleDiagnostic(
    override konst element: KtLightSourceElement,
    override konst severity: Severity,
    override konst factory: KtDiagnosticFactory0,
    override konst positioningStrategy: AbstractSourceElementPositioningStrategy
) : KtSimpleDiagnostic(), KtLightDiagnostic

data class KtLightDiagnosticWithParameters1<A>(
    override konst element: KtLightSourceElement,
    override konst a: A,
    override konst severity: Severity,
    override konst factory: KtDiagnosticFactory1<A>,
    override konst positioningStrategy: AbstractSourceElementPositioningStrategy
) : KtDiagnosticWithParameters1<A>(), KtLightDiagnostic

data class KtLightDiagnosticWithParameters2<A, B>(
    override konst element: KtLightSourceElement,
    override konst a: A,
    override konst b: B,
    override konst severity: Severity,
    override konst factory: KtDiagnosticFactory2<A, B>,
    override konst positioningStrategy: AbstractSourceElementPositioningStrategy
) : KtDiagnosticWithParameters2<A, B>(), KtLightDiagnostic

data class KtLightDiagnosticWithParameters3<A, B, C>(
    override konst element: KtLightSourceElement,
    override konst a: A,
    override konst b: B,
    override konst c: C,
    override konst severity: Severity,
    override konst factory: KtDiagnosticFactory3<A, B, C>,
    override konst positioningStrategy: AbstractSourceElementPositioningStrategy
) : KtDiagnosticWithParameters3<A, B, C>(), KtLightDiagnostic

data class KtLightDiagnosticWithParameters4<A, B, C, D>(
    override konst element: KtLightSourceElement,
    override konst a: A,
    override konst b: B,
    override konst c: C,
    override konst d: D,
    override konst severity: Severity,
    override konst factory: KtDiagnosticFactory4<A, B, C, D>,
    override konst positioningStrategy: AbstractSourceElementPositioningStrategy
) : KtDiagnosticWithParameters4<A, B, C, D>(), KtLightDiagnostic

// ------------------------------ light tree diagnostics ------------------------------

interface KtOffsetsOnlyDiagnostic : DiagnosticMarker {
    konst element: AbstractKtSourceElement

    @Deprecated("Should not be called", level = DeprecationLevel.HIDDEN)
    override konst psiElement: PsiElement
        get() = error("psiElement should not be called on KtOffsetsOnlyDiagnostic")
}

data class KtOffsetsOnlySimpleDiagnostic(
    override konst element: AbstractKtSourceElement,
    override konst severity: Severity,
    override konst factory: KtDiagnosticFactory0,
    override konst positioningStrategy: AbstractSourceElementPositioningStrategy
) : KtSimpleDiagnostic(), KtOffsetsOnlyDiagnostic

data class KtOffsetsOnlyDiagnosticWithParameters1<A>(
    override konst element: AbstractKtSourceElement,
    override konst a: A,
    override konst severity: Severity,
    override konst factory: KtDiagnosticFactory1<A>,
    override konst positioningStrategy: AbstractSourceElementPositioningStrategy
) : KtDiagnosticWithParameters1<A>(), KtOffsetsOnlyDiagnostic

data class KtOffsetsOnlyDiagnosticWithParameters2<A, B>(
    override konst element: AbstractKtSourceElement,
    override konst a: A,
    override konst b: B,
    override konst severity: Severity,
    override konst factory: KtDiagnosticFactory2<A, B>,
    override konst positioningStrategy: AbstractSourceElementPositioningStrategy
) : KtDiagnosticWithParameters2<A, B>(), KtOffsetsOnlyDiagnostic

data class KtOffsetsOnlyDiagnosticWithParameters3<A, B, C>(
    override konst element: AbstractKtSourceElement,
    override konst a: A,
    override konst b: B,
    override konst c: C,
    override konst severity: Severity,
    override konst factory: KtDiagnosticFactory3<A, B, C>,
    override konst positioningStrategy: AbstractSourceElementPositioningStrategy
) : KtDiagnosticWithParameters3<A, B, C>(), KtOffsetsOnlyDiagnostic

data class KtOffsetsOnlyDiagnosticWithParameters4<A, B, C, D>(
    override konst element: AbstractKtSourceElement,
    override konst a: A,
    override konst b: B,
    override konst c: C,
    override konst d: D,
    override konst severity: Severity,
    override konst factory: KtDiagnosticFactory4<A, B, C, D>,
    override konst positioningStrategy: AbstractSourceElementPositioningStrategy
) : KtDiagnosticWithParameters4<A, B, C, D>(), KtOffsetsOnlyDiagnostic
