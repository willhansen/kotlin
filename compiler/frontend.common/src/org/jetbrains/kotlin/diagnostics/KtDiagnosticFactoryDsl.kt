/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.diagnostics

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.config.LanguageFeature
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

inline fun <reified P : PsiElement> warning0(
    positioningStrategy: AbstractSourceElementPositioningStrategy = AbstractSourceElementPositioningStrategy.DEFAULT
): DiagnosticFactory0DelegateProvider {
    return DiagnosticFactory0DelegateProvider(Severity.WARNING, positioningStrategy, P::class)
}

inline fun <reified P : PsiElement, A> warning1(
    positioningStrategy: AbstractSourceElementPositioningStrategy = AbstractSourceElementPositioningStrategy.DEFAULT
): DiagnosticFactory1DelegateProvider<A> {
    return DiagnosticFactory1DelegateProvider(Severity.WARNING, positioningStrategy, P::class)
}

inline fun <reified P : PsiElement, A, B> warning2(
    positioningStrategy: AbstractSourceElementPositioningStrategy = AbstractSourceElementPositioningStrategy.DEFAULT
): DiagnosticFactory2DelegateProvider<A, B> {
    return DiagnosticFactory2DelegateProvider(Severity.WARNING, positioningStrategy, P::class)
}

inline fun <reified P : PsiElement, A, B, C> warning3(
    positioningStrategy: AbstractSourceElementPositioningStrategy = AbstractSourceElementPositioningStrategy.DEFAULT
): DiagnosticFactory3DelegateProvider<A, B, C> {
    return DiagnosticFactory3DelegateProvider(Severity.WARNING, positioningStrategy, P::class)
}

inline fun <reified P : PsiElement, A, B, C, D> warning4(
    positioningStrategy: AbstractSourceElementPositioningStrategy = AbstractSourceElementPositioningStrategy.DEFAULT
): DiagnosticFactory4DelegateProvider<A, B, C, D> {
    return DiagnosticFactory4DelegateProvider(Severity.WARNING, positioningStrategy, P::class)
}

inline fun <reified P : PsiElement> error0(
    positioningStrategy: AbstractSourceElementPositioningStrategy = AbstractSourceElementPositioningStrategy.DEFAULT
): DiagnosticFactory0DelegateProvider {
    return DiagnosticFactory0DelegateProvider(Severity.ERROR, positioningStrategy, P::class)
}

inline fun <reified P : PsiElement, A> error1(
    positioningStrategy: AbstractSourceElementPositioningStrategy = AbstractSourceElementPositioningStrategy.DEFAULT
): DiagnosticFactory1DelegateProvider<A> {
    return DiagnosticFactory1DelegateProvider(Severity.ERROR, positioningStrategy, P::class)
}

inline fun <reified P : PsiElement, A, B> error2(
    positioningStrategy: AbstractSourceElementPositioningStrategy = AbstractSourceElementPositioningStrategy.DEFAULT
): DiagnosticFactory2DelegateProvider<A, B> {
    return DiagnosticFactory2DelegateProvider(Severity.ERROR, positioningStrategy, P::class)
}

inline fun <reified P : PsiElement, A, B, C> error3(
    positioningStrategy: AbstractSourceElementPositioningStrategy = AbstractSourceElementPositioningStrategy.DEFAULT
): DiagnosticFactory3DelegateProvider<A, B, C> {
    return DiagnosticFactory3DelegateProvider(Severity.ERROR, positioningStrategy, P::class)
}

inline fun <reified P : PsiElement, A, B, C, D> error4(
    positioningStrategy: AbstractSourceElementPositioningStrategy = AbstractSourceElementPositioningStrategy.DEFAULT
): DiagnosticFactory4DelegateProvider<A, B, C, D> {
    return DiagnosticFactory4DelegateProvider(Severity.ERROR, positioningStrategy, P::class)
}

inline fun <reified P : PsiElement> deprecationError0(
    featureForError: LanguageFeature,
    positioningStrategy: AbstractSourceElementPositioningStrategy = AbstractSourceElementPositioningStrategy.DEFAULT
): DeprecationDiagnosticFactory0DelegateProvider {
    return DeprecationDiagnosticFactory0DelegateProvider(featureForError, positioningStrategy, P::class)
}

inline fun <reified P : PsiElement, A> deprecationError1(
    featureForError: LanguageFeature,
    positioningStrategy: AbstractSourceElementPositioningStrategy = AbstractSourceElementPositioningStrategy.DEFAULT
): DeprecationDiagnosticFactory1DelegateProvider<A> {
    return DeprecationDiagnosticFactory1DelegateProvider(featureForError, positioningStrategy, P::class)
}

inline fun <reified P : PsiElement, A, B> deprecationError2(
    featureForError: LanguageFeature,
    positioningStrategy: AbstractSourceElementPositioningStrategy = AbstractSourceElementPositioningStrategy.DEFAULT
): DeprecationDiagnosticFactory2DelegateProvider<A, B> {
    return DeprecationDiagnosticFactory2DelegateProvider(featureForError, positioningStrategy, P::class)
}

inline fun <reified P : PsiElement, A, B, C> deprecationError3(
    featureForError: LanguageFeature,
    positioningStrategy: AbstractSourceElementPositioningStrategy = AbstractSourceElementPositioningStrategy.DEFAULT
): DeprecationDiagnosticFactory3DelegateProvider<A, B, C> {
    return DeprecationDiagnosticFactory3DelegateProvider(featureForError, positioningStrategy, P::class)
}

inline fun <reified P : PsiElement, A, B, C, D> deprecationError4(
    featureForError: LanguageFeature,
    positioningStrategy: AbstractSourceElementPositioningStrategy = AbstractSourceElementPositioningStrategy.DEFAULT
): DeprecationDiagnosticFactory4DelegateProvider<A, B, C, D> {
    return DeprecationDiagnosticFactory4DelegateProvider(featureForError, positioningStrategy, P::class)
}

// ------------------------------ Providers ------------------------------

class DiagnosticFactory0DelegateProvider(
    private konst severity: Severity,
    private konst positioningStrategy: AbstractSourceElementPositioningStrategy,
    private konst psiType: KClass<*>
) {
    operator fun provideDelegate(thisRef: Any?, prop: KProperty<*>): ReadOnlyProperty<Any?, KtDiagnosticFactory0> {
        return DummyDelegate(KtDiagnosticFactory0(prop.name, severity, positioningStrategy, psiType))
    }
}

class DiagnosticFactory1DelegateProvider<A>(
    private konst severity: Severity,
    private konst positioningStrategy: AbstractSourceElementPositioningStrategy,
    private konst psiType: KClass<*>
) {
    operator fun provideDelegate(thisRef: Any?, prop: KProperty<*>): ReadOnlyProperty<Any?, KtDiagnosticFactory1<A>> {
        return DummyDelegate(KtDiagnosticFactory1(prop.name, severity, positioningStrategy, psiType))
    }
}

class DiagnosticFactory2DelegateProvider<A, B>(
    private konst severity: Severity,
    private konst positioningStrategy: AbstractSourceElementPositioningStrategy,
    private konst psiType: KClass<*>
) {
    operator fun provideDelegate(thisRef: Any?, prop: KProperty<*>): ReadOnlyProperty<Any?, KtDiagnosticFactory2<A, B>> {
        return DummyDelegate(KtDiagnosticFactory2(prop.name, severity, positioningStrategy, psiType))
    }
}

class DiagnosticFactory3DelegateProvider<A, B, C>(
    private konst severity: Severity,
    private konst positioningStrategy: AbstractSourceElementPositioningStrategy,
    private konst psiType: KClass<*>
) {
    operator fun provideDelegate(thisRef: Any?, prop: KProperty<*>): ReadOnlyProperty<Any?, KtDiagnosticFactory3<A, B, C>> {
        return DummyDelegate(KtDiagnosticFactory3(prop.name, severity, positioningStrategy, psiType))
    }
}

class DiagnosticFactory4DelegateProvider<A, B, C, D>(
    private konst severity: Severity,
    private konst positioningStrategy: AbstractSourceElementPositioningStrategy,
    private konst psiType: KClass<*>
) {
    operator fun provideDelegate(thisRef: Any?, prop: KProperty<*>): ReadOnlyProperty<Any?, KtDiagnosticFactory4<A, B, C, D>> {
        return DummyDelegate(KtDiagnosticFactory4(prop.name, severity, positioningStrategy, psiType))
    }
}

private const konst WARNING = "_WARNING"
private const konst ERROR = "_ERROR"

class DeprecationDiagnosticFactory0DelegateProvider(
    private konst featureForError: LanguageFeature,
    private konst positioningStrategy: AbstractSourceElementPositioningStrategy,
    private konst psiType: KClass<*>
) {
    operator fun provideDelegate(thisRef: Any?, prop: KProperty<*>): ReadOnlyProperty<Any?, KtDiagnosticFactoryForDeprecation0> {
        konst errorFactory = KtDiagnosticFactory0("${prop.name}$ERROR", Severity.ERROR, positioningStrategy, psiType)
        konst warningFactory = KtDiagnosticFactory0("${prop.name}$WARNING", Severity.WARNING, positioningStrategy, psiType)
        return DummyDelegate(KtDiagnosticFactoryForDeprecation0(featureForError, warningFactory, errorFactory))
    }
}

class DeprecationDiagnosticFactory1DelegateProvider<A>(
    private konst featureForError: LanguageFeature,
    private konst positioningStrategy: AbstractSourceElementPositioningStrategy,
    private konst psiType: KClass<*>
) {
    operator fun provideDelegate(thisRef: Any?, prop: KProperty<*>): ReadOnlyProperty<Any?, KtDiagnosticFactoryForDeprecation1<A>> {
        konst errorFactory = KtDiagnosticFactory1<A>("${prop.name}$ERROR", Severity.ERROR, positioningStrategy, psiType)
        konst warningFactory = KtDiagnosticFactory1<A>("${prop.name}$WARNING", Severity.WARNING, positioningStrategy, psiType)
        return DummyDelegate(KtDiagnosticFactoryForDeprecation1(featureForError, warningFactory, errorFactory))
    }
}

class DeprecationDiagnosticFactory2DelegateProvider<A, B>(
    private konst featureForError: LanguageFeature,
    private konst positioningStrategy: AbstractSourceElementPositioningStrategy,
    private konst psiType: KClass<*>
) {
    operator fun provideDelegate(thisRef: Any?, prop: KProperty<*>): ReadOnlyProperty<Any?, KtDiagnosticFactoryForDeprecation2<A, B>> {
        konst errorFactory = KtDiagnosticFactory2<A, B>("${prop.name}$ERROR", Severity.ERROR, positioningStrategy, psiType)
        konst warningFactory = KtDiagnosticFactory2<A, B>("${prop.name}$WARNING", Severity.WARNING, positioningStrategy, psiType)
        return DummyDelegate(KtDiagnosticFactoryForDeprecation2(featureForError, warningFactory, errorFactory))
    }
}

class DeprecationDiagnosticFactory3DelegateProvider<A, B, C>(
    private konst featureForError: LanguageFeature,
    private konst positioningStrategy: AbstractSourceElementPositioningStrategy,
    private konst psiType: KClass<*>
) {
    operator fun provideDelegate(thisRef: Any?, prop: KProperty<*>): ReadOnlyProperty<Any?, KtDiagnosticFactoryForDeprecation3<A, B, C>> {
        konst errorFactory = KtDiagnosticFactory3<A, B, C>("${prop.name}$ERROR", Severity.ERROR, positioningStrategy, psiType)
        konst warningFactory = KtDiagnosticFactory3<A, B, C>("${prop.name}$WARNING", Severity.WARNING, positioningStrategy, psiType)
        return DummyDelegate(KtDiagnosticFactoryForDeprecation3(featureForError, warningFactory, errorFactory))
    }
}

class DeprecationDiagnosticFactory4DelegateProvider<A, B, C, D>(
    private konst featureForError: LanguageFeature,
    private konst positioningStrategy: AbstractSourceElementPositioningStrategy,
    private konst psiType: KClass<*>
) {
    operator fun provideDelegate(thisRef: Any?, prop: KProperty<*>): ReadOnlyProperty<Any?, KtDiagnosticFactoryForDeprecation4<A, B, C, D>> {
        konst errorFactory = KtDiagnosticFactory4<A, B, C, D>("${prop.name}$ERROR", Severity.ERROR, positioningStrategy, psiType)
        konst warningFactory = KtDiagnosticFactory4<A, B, C, D>("${prop.name}$WARNING", Severity.WARNING, positioningStrategy, psiType)
        return DummyDelegate(KtDiagnosticFactoryForDeprecation4(featureForError, warningFactory, errorFactory))
    }
}


private class DummyDelegate<T>(konst konstue: T) : ReadOnlyProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return konstue
    }
}
