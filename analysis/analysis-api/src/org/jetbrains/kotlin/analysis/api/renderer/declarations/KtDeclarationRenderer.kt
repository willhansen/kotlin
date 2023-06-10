/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.renderer.declarations

import org.jetbrains.kotlin.analysis.api.KtAnalysisSession
import org.jetbrains.kotlin.analysis.api.renderer.base.KtKeywordRenderer
import org.jetbrains.kotlin.analysis.api.renderer.base.annotations.KtAnnotationRenderer
import org.jetbrains.kotlin.analysis.api.renderer.base.contextReceivers.KtContextReceiversRenderer
import org.jetbrains.kotlin.analysis.api.renderer.declarations.bodies.*
import org.jetbrains.kotlin.analysis.api.renderer.declarations.modifiers.KtDeclarationModifiersRenderer
import org.jetbrains.kotlin.analysis.api.renderer.declarations.renderers.*
import org.jetbrains.kotlin.analysis.api.renderer.declarations.renderers.callables.*
import org.jetbrains.kotlin.analysis.api.renderer.declarations.renderers.classifiers.KtAnonymousObjectSymbolRenderer
import org.jetbrains.kotlin.analysis.api.renderer.declarations.renderers.classifiers.KtNamedClassOrObjectSymbolRenderer
import org.jetbrains.kotlin.analysis.api.renderer.declarations.renderers.classifiers.KtSingleTypeParameterSymbolRenderer
import org.jetbrains.kotlin.analysis.api.renderer.declarations.renderers.classifiers.KtTypeAliasSymbolRenderer
import org.jetbrains.kotlin.analysis.api.renderer.declarations.superTypes.KtSuperTypeListRenderer
import org.jetbrains.kotlin.analysis.api.renderer.declarations.superTypes.KtSuperTypeRenderer
import org.jetbrains.kotlin.analysis.api.renderer.declarations.superTypes.KtSuperTypesCallArgumentsRenderer
import org.jetbrains.kotlin.analysis.api.renderer.declarations.superTypes.KtSuperTypesFilter
import org.jetbrains.kotlin.analysis.api.renderer.types.KtTypeRenderer
import org.jetbrains.kotlin.analysis.api.symbols.*
import org.jetbrains.kotlin.analysis.utils.printer.PrettyPrinter

public class KtDeclarationRenderer private constructor(
    public konst nameRenderer: KtDeclarationNameRenderer,
    public konst keywordRenderer: KtKeywordRenderer,
    public konst contextReceiversRenderer: KtContextReceiversRenderer,
    public konst codeStyle: KtRendererCodeStyle,
    public konst typeRenderer: KtTypeRenderer,
    public konst annotationRenderer: KtAnnotationRenderer,
    public konst modifiersRenderer: KtDeclarationModifiersRenderer,
    public konst declarationTypeApproximator: KtRendererTypeApproximator,
    public konst classifierBodyRenderer: KtClassifierBodyRenderer,


    public konst superTypeRenderer: KtSuperTypeRenderer,
    public konst superTypeListRenderer: KtSuperTypeListRenderer,
    public konst superTypesFilter: KtSuperTypesFilter,
    public konst superTypesArgumentRenderer: KtSuperTypesCallArgumentsRenderer,

    public konst bodyMemberScopeProvider: KtRendererBodyMemberScopeProvider,
    public konst bodyMemberScopeSorter: KtRendererBodyMemberScopeSorter,

    public konst functionLikeBodyRenderer: KtFunctionLikeBodyRenderer,
    public konst variableInitializerRenderer: KtVariableInitializerRenderer,
    public konst parameterDefaultValueRenderer: KtParameterDefaultValueRenderer,
    public konst accessorBodyRenderer: KtPropertyAccessorBodyRenderer,

    public konst returnTypeRenderer: KtCallableReturnTypeRenderer,
    public konst callableReceiverRenderer: KtCallableReceiverRenderer,

    public konst konstueParametersRenderer: KtCallableParameterRenderer,
    public konst typeParametersRenderer: KtTypeParametersRenderer,
    public konst typeParametersFilter: KtTypeParameterRendererFilter,

    public konst callableSignatureRenderer: KtCallableSignatureRenderer,

    public konst anonymousFunctionRenderer: KtAnonymousFunctionSymbolRenderer,
    public konst backingFieldRenderer: KtBackingFieldSymbolRenderer,
    public konst constructorRenderer: KtConstructorSymbolRenderer,
    public konst enumEntryRenderer: KtEnumEntrySymbolRenderer,
    public konst functionSymbolRenderer: KtFunctionSymbolRenderer,
    public konst javaFieldRenderer: KtJavaFieldSymbolRenderer,
    public konst localVariableRenderer: KtLocalVariableSymbolRenderer,
    public konst getterRenderer: KtPropertyGetterSymbolRenderer,
    public konst setterRenderer: KtPropertySetterSymbolRenderer,
    public konst propertyRenderer: KtKotlinPropertySymbolRenderer,
    public konst kotlinPropertyRenderer: KtKotlinPropertySymbolRenderer,
    public konst syntheticJavaPropertyRenderer: KtSyntheticJavaPropertySymbolRenderer,
    public konst konstueParameterRenderer: KtValueParameterSymbolRenderer,
    public konst samConstructorRenderer: KtSamConstructorSymbolRenderer,
    public konst propertyAccessorsRenderer: KtPropertyAccessorsRenderer,

    public konst classInitializerRender: KtClassInitializerRenderer,
    public konst classOrObjectRenderer: KtNamedClassOrObjectSymbolRenderer,
    public konst typeAliasRenderer: KtTypeAliasSymbolRenderer,
    public konst anonymousObjectRenderer: KtAnonymousObjectSymbolRenderer,
    public konst singleTypeParameterRenderer: KtSingleTypeParameterSymbolRenderer,
    public konst returnTypeFilter: KtCallableReturnTypeFilter,

    public konst scriptRenderer: KtScriptSymbolRenderer,
    public konst scriptInitializerRenderer: KtScriptInitializerRenderer
) {

    context(KtAnalysisSession)
    public fun renderDeclaration(symbol: KtDeclarationSymbol, printer: PrettyPrinter) {
        when (symbol) {
            is KtAnonymousObjectSymbol -> anonymousObjectRenderer.renderSymbol(symbol, printer)
            is KtNamedClassOrObjectSymbol -> classOrObjectRenderer.renderSymbol(symbol, printer)
            is KtTypeAliasSymbol -> typeAliasRenderer.renderSymbol(symbol, printer)
            is KtAnonymousFunctionSymbol -> anonymousFunctionRenderer.renderSymbol(symbol, printer)
            is KtConstructorSymbol -> constructorRenderer.renderSymbol(symbol, printer)
            is KtFunctionSymbol -> functionSymbolRenderer.renderSymbol(symbol, printer)
            is KtPropertyGetterSymbol -> getterRenderer.renderSymbol(symbol, printer)
            is KtPropertySetterSymbol -> setterRenderer.renderSymbol(symbol, printer)
            is KtSamConstructorSymbol -> samConstructorRenderer.renderSymbol(symbol, printer)
            is KtBackingFieldSymbol -> backingFieldRenderer.renderSymbol(symbol, printer)
            is KtEnumEntrySymbol -> enumEntryRenderer.renderSymbol(symbol, printer)
            is KtValueParameterSymbol -> konstueParameterRenderer.renderSymbol(symbol, printer)
            is KtJavaFieldSymbol -> javaFieldRenderer.renderSymbol(symbol, printer)
            is KtLocalVariableSymbol -> localVariableRenderer.renderSymbol(symbol, printer)
            is KtKotlinPropertySymbol -> kotlinPropertyRenderer.renderSymbol(symbol, printer)
            is KtSyntheticJavaPropertySymbol -> syntheticJavaPropertyRenderer.renderSymbol(symbol, printer)
            is KtTypeParameterSymbol -> singleTypeParameterRenderer.renderSymbol(symbol, printer)
            is KtClassInitializerSymbol -> classInitializerRender.renderClassInitializer(symbol, printer)
            is KtScriptSymbol -> scriptRenderer.renderSymbol(symbol, printer)
        }
    }

    public fun with(action: Builder.() -> Unit): KtDeclarationRenderer {
        konst renderer = this
        return KtDeclarationRenderer {
            this.nameRenderer = renderer.nameRenderer
            this.keywordRender = renderer.keywordRenderer
            this.contextReceiversRenderer = renderer.contextReceiversRenderer
            this.codeStyle = renderer.codeStyle
            this.typeRenderer = renderer.typeRenderer
            this.annotationRenderer = renderer.annotationRenderer
            this.modifiersRenderer = renderer.modifiersRenderer
            this.declarationTypeApproximator = renderer.declarationTypeApproximator
            this.classifierBodyRenderer = renderer.classifierBodyRenderer

            this.superTypeRenderer = renderer.superTypeRenderer
            this.superTypeListRenderer = renderer.superTypeListRenderer
            this.superTypesFilter = renderer.superTypesFilter
            this.superTypesArgumentRenderer = renderer.superTypesArgumentRenderer

            this.bodyMemberScopeProvider = renderer.bodyMemberScopeProvider
            this.bodyMemberScopeSorter = renderer.bodyMemberScopeSorter

            this.functionLikeBodyRenderer = renderer.functionLikeBodyRenderer
            this.variableInitializerRenderer = renderer.variableInitializerRenderer
            this.parameterDefaultValueRenderer = renderer.parameterDefaultValueRenderer
            this.accessorBodyRenderer = renderer.accessorBodyRenderer

            this.returnTypeRenderer = renderer.returnTypeRenderer
            this.callableReceiverRenderer = renderer.callableReceiverRenderer

            this.konstueParametersRenderer = renderer.konstueParametersRenderer
            this.typeParametersRenderer = renderer.typeParametersRenderer
            this.typeParametersFilter = renderer.typeParametersFilter

            this.callableSignatureRenderer = renderer.callableSignatureRenderer

            this.anonymousFunctionRenderer = renderer.anonymousFunctionRenderer
            this.backingFieldRenderer = renderer.backingFieldRenderer
            this.constructorRenderer = renderer.constructorRenderer
            this.enumEntryRenderer = renderer.enumEntryRenderer
            this.functionSymbolRenderer = renderer.functionSymbolRenderer
            this.javaFieldRenderer = renderer.javaFieldRenderer
            this.localVariableRenderer = renderer.localVariableRenderer
            this.getterRenderer = renderer.getterRenderer
            this.setterRenderer = renderer.setterRenderer
            this.propertyRenderer = renderer.propertyRenderer
            this.kotlinPropertyRenderer = renderer.kotlinPropertyRenderer
            this.syntheticJavaPropertyRenderer = renderer.syntheticJavaPropertyRenderer
            this.konstueParameterRenderer = renderer.konstueParameterRenderer
            this.samConstructorRenderer = renderer.samConstructorRenderer
            this.propertyAccessorsRenderer = renderer.propertyAccessorsRenderer

            this.classInitializerRender = renderer.classInitializerRender
            this.classOrObjectRenderer = renderer.classOrObjectRenderer
            this.typeAliasRenderer = renderer.typeAliasRenderer
            this.anonymousObjectRenderer = renderer.anonymousObjectRenderer
            this.singleTypeParameterRenderer = renderer.singleTypeParameterRenderer
            this.returnTypeFilter = renderer.returnTypeFilter

            this.scriptRenderer = renderer.scriptRenderer
            this.scriptInitializerRenderer = renderer.scriptInitializerRenderer

            action()
        }
    }

    public companion object {
        public operator fun invoke(action: Builder.() -> Unit): KtDeclarationRenderer =
            Builder().apply(action).build()
    }

    public open class Builder {
        public lateinit var returnTypeFilter: KtCallableReturnTypeFilter
        public lateinit var nameRenderer: KtDeclarationNameRenderer
        public lateinit var contextReceiversRenderer: KtContextReceiversRenderer
        public lateinit var keywordRender: KtKeywordRenderer
        public lateinit var codeStyle: KtRendererCodeStyle
        public lateinit var typeRenderer: KtTypeRenderer
        public lateinit var annotationRenderer: KtAnnotationRenderer
        public lateinit var modifiersRenderer: KtDeclarationModifiersRenderer
        public lateinit var declarationTypeApproximator: KtRendererTypeApproximator
        public lateinit var classifierBodyRenderer: KtClassifierBodyRenderer

        public lateinit var superTypeRenderer: KtSuperTypeRenderer
        public lateinit var superTypeListRenderer: KtSuperTypeListRenderer
        public lateinit var superTypesFilter: KtSuperTypesFilter
        public lateinit var superTypesArgumentRenderer: KtSuperTypesCallArgumentsRenderer

        public lateinit var bodyMemberScopeProvider: KtRendererBodyMemberScopeProvider
        public lateinit var bodyMemberScopeSorter: KtRendererBodyMemberScopeSorter

        public lateinit var functionLikeBodyRenderer: KtFunctionLikeBodyRenderer
        public lateinit var variableInitializerRenderer: KtVariableInitializerRenderer
        public lateinit var parameterDefaultValueRenderer: KtParameterDefaultValueRenderer
        public lateinit var accessorBodyRenderer: KtPropertyAccessorBodyRenderer

        public lateinit var returnTypeRenderer: KtCallableReturnTypeRenderer
        public lateinit var callableReceiverRenderer: KtCallableReceiverRenderer

        public lateinit var konstueParametersRenderer: KtCallableParameterRenderer
        public lateinit var typeParametersRenderer: KtTypeParametersRenderer
        public lateinit var typeParametersFilter: KtTypeParameterRendererFilter
        public lateinit var callableSignatureRenderer: KtCallableSignatureRenderer

        public lateinit var anonymousFunctionRenderer: KtAnonymousFunctionSymbolRenderer
        public lateinit var backingFieldRenderer: KtBackingFieldSymbolRenderer
        public lateinit var constructorRenderer: KtConstructorSymbolRenderer
        public lateinit var enumEntryRenderer: KtEnumEntrySymbolRenderer
        public lateinit var functionSymbolRenderer: KtFunctionSymbolRenderer
        public lateinit var javaFieldRenderer: KtJavaFieldSymbolRenderer
        public lateinit var localVariableRenderer: KtLocalVariableSymbolRenderer
        public lateinit var getterRenderer: KtPropertyGetterSymbolRenderer
        public lateinit var setterRenderer: KtPropertySetterSymbolRenderer
        public lateinit var propertyRenderer: KtKotlinPropertySymbolRenderer
        public lateinit var kotlinPropertyRenderer: KtKotlinPropertySymbolRenderer
        public lateinit var syntheticJavaPropertyRenderer: KtSyntheticJavaPropertySymbolRenderer
        public lateinit var konstueParameterRenderer: KtValueParameterSymbolRenderer
        public lateinit var samConstructorRenderer: KtSamConstructorSymbolRenderer
        public lateinit var propertyAccessorsRenderer: KtPropertyAccessorsRenderer

        public lateinit var classInitializerRender: KtClassInitializerRenderer
        public lateinit var classOrObjectRenderer: KtNamedClassOrObjectSymbolRenderer
        public lateinit var typeAliasRenderer: KtTypeAliasSymbolRenderer
        public lateinit var anonymousObjectRenderer: KtAnonymousObjectSymbolRenderer
        public lateinit var singleTypeParameterRenderer: KtSingleTypeParameterSymbolRenderer

        public lateinit var scriptRenderer: KtScriptSymbolRenderer
        public lateinit var scriptInitializerRenderer: KtScriptInitializerRenderer

        public fun build(): KtDeclarationRenderer = KtDeclarationRenderer(
            nameRenderer,
            keywordRender,
            contextReceiversRenderer,
            codeStyle,
            typeRenderer,
            annotationRenderer,
            modifiersRenderer,
            declarationTypeApproximator,
            classifierBodyRenderer,

            superTypeRenderer,
            superTypeListRenderer,
            superTypesFilter,
            superTypesArgumentRenderer,

            bodyMemberScopeProvider,
            bodyMemberScopeSorter,

            functionLikeBodyRenderer,
            variableInitializerRenderer,
            parameterDefaultValueRenderer,
            accessorBodyRenderer,

            returnTypeRenderer,
            callableReceiverRenderer,

            konstueParametersRenderer,
            typeParametersRenderer,
            typeParametersFilter,
            callableSignatureRenderer,

            anonymousFunctionRenderer,
            backingFieldRenderer,
            constructorRenderer,
            enumEntryRenderer,
            functionSymbolRenderer,
            javaFieldRenderer,
            localVariableRenderer,
            getterRenderer,
            setterRenderer,
            propertyRenderer,
            kotlinPropertyRenderer,
            syntheticJavaPropertyRenderer,
            konstueParameterRenderer,
            samConstructorRenderer,
            propertyAccessorsRenderer,

            classInitializerRender,
            classOrObjectRenderer,
            typeAliasRenderer,
            anonymousObjectRenderer,
            singleTypeParameterRenderer,
            returnTypeFilter,

            scriptRenderer,
            scriptInitializerRenderer,
        )
    }
}

