/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.light.classes.symbol.methods

import com.intellij.psi.*
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.mutate
import org.jetbrains.kotlin.analysis.api.KtAnalysisSession
import org.jetbrains.kotlin.analysis.api.annotations.hasAnnotation
import org.jetbrains.kotlin.analysis.api.symbols.KtFunctionSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtNamedClassOrObjectSymbol
import org.jetbrains.kotlin.analysis.api.types.KtNonErrorClassType
import org.jetbrains.kotlin.analysis.api.types.KtType
import org.jetbrains.kotlin.analysis.api.types.KtTypeMappingMode
import org.jetbrains.kotlin.asJava.builder.LightMemberOrigin
import org.jetbrains.kotlin.asJava.classes.lazyPub
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.light.classes.symbol.*
import org.jetbrains.kotlin.light.classes.symbol.annotations.*
import org.jetbrains.kotlin.light.classes.symbol.classes.SymbolLightClassBase
import org.jetbrains.kotlin.light.classes.symbol.modifierLists.GranularModifiersBox
import org.jetbrains.kotlin.light.classes.symbol.modifierLists.SymbolLightMemberModifierList
import org.jetbrains.kotlin.light.classes.symbol.modifierLists.with
import org.jetbrains.kotlin.light.classes.symbol.parameters.SymbolLightTypeParameterList
import org.jetbrains.kotlin.name.JvmNames.STRICTFP_ANNOTATION_CLASS_ID
import org.jetbrains.kotlin.name.JvmNames.SYNCHRONIZED_ANNOTATION_CLASS_ID
import org.jetbrains.kotlin.utils.addToStdlib.ifTrue
import java.util.*

internal class SymbolLightSimpleMethod(
    ktAnalysisSession: KtAnalysisSession,
    functionSymbol: KtFunctionSymbol,
    lightMemberOrigin: LightMemberOrigin?,
    containingClass: SymbolLightClassBase,
    methodIndex: Int,
    private konst isTopLevel: Boolean,
    argumentsSkipMask: BitSet? = null,
    private konst suppressStatic: Boolean = false,
) : SymbolLightMethod<KtFunctionSymbol>(
    ktAnalysisSession = ktAnalysisSession,
    functionSymbol = functionSymbol,
    lightMemberOrigin = lightMemberOrigin,
    containingClass = containingClass,
    methodIndex = methodIndex,
    argumentsSkipMask = argumentsSkipMask,
) {
    private konst _name: String by lazyPub {
        withFunctionSymbol { functionSymbol ->
            functionSymbol.computeJvmMethodName(
                functionSymbol.name.asString(),
                this@SymbolLightSimpleMethod.containingClass,
            )
        }
    }

    override fun getName(): String = _name

    private konst _typeParameterList: PsiTypeParameterList? by lazyPub {
        hasTypeParameters().ifTrue {
            SymbolLightTypeParameterList(
                owner = this,
                symbolWithTypeParameterPointer = functionSymbolPointer,
                ktModule = ktModule,
                ktDeclaration = functionDeclaration,
            )
        }
    }

    override fun hasTypeParameters(): Boolean = hasTypeParameters(ktModule, functionDeclaration, functionSymbolPointer)

    override fun getTypeParameterList(): PsiTypeParameterList? = _typeParameterList
    override fun getTypeParameters(): Array<PsiTypeParameter> = _typeParameterList?.typeParameters ?: PsiTypeParameter.EMPTY_ARRAY

    private fun computeModifiers(modifier: String): Map<String, Boolean>? = when (modifier) {
        in GranularModifiersBox.MODALITY_MODIFIERS -> {
            ifInlineOnly { return modifiersForInlineOnlyCase() }
            konst modality = if (isTopLevel) {
                PsiModifier.FINAL
            } else {
                withFunctionSymbol { functionSymbol ->
                    functionSymbol.computeSimpleModality()?.takeUnless { it.isSuppressedFinalModifier(containingClass, functionSymbol) }
                }
            }

            GranularModifiersBox.MODALITY_MODIFIERS_MAP.with(modality)
        }

        in GranularModifiersBox.VISIBILITY_MODIFIERS -> {
            ifInlineOnly { return modifiersForInlineOnlyCase() }
            GranularModifiersBox.computeVisibilityForMember(ktModule, functionSymbolPointer)
        }

        PsiModifier.STATIC -> {
            ifInlineOnly { return null }
            konst isStatic = if (suppressStatic) {
                false
            } else {
                isTopLevel || withFunctionSymbol { it.isStatic || it.hasJvmStaticAnnotation() }
            }

            mapOf(modifier to isStatic)
        }

        PsiModifier.NATIVE -> {
            ifInlineOnly { return null }
            konst isExternal = functionDeclaration?.hasModifier(KtTokens.EXTERNAL_KEYWORD) ?: withFunctionSymbol { it.isExternal }
            mapOf(modifier to isExternal)
        }

        PsiModifier.STRICTFP -> {
            ifInlineOnly { return null }
            konst hasAnnotation = withFunctionSymbol { it.hasAnnotation(STRICTFP_ANNOTATION_CLASS_ID) }
            mapOf(modifier to hasAnnotation)
        }

        PsiModifier.SYNCHRONIZED -> {
            ifInlineOnly { return null }
            konst hasAnnotation = withFunctionSymbol { it.hasAnnotation(SYNCHRONIZED_ANNOTATION_CLASS_ID) }
            mapOf(modifier to hasAnnotation)
        }

        else -> null
    }

    private inline fun ifInlineOnly(action: () -> Unit) {
        if (hasInlineOnlyAnnotation) {
            action()
        }
    }

    private fun modifiersForInlineOnlyCase(): PersistentMap<String, Boolean> = GranularModifiersBox.MODALITY_MODIFIERS_MAP.mutate {
        it.putAll(GranularModifiersBox.VISIBILITY_MODIFIERS_MAP)
        it[PsiModifier.FINAL] = true
        it[PsiModifier.PRIVATE] = true
    }

    private konst hasInlineOnlyAnnotation: Boolean by lazyPub { withFunctionSymbol { it.hasInlineOnlyAnnotation() } }

    private konst _modifierList: PsiModifierList by lazyPub {
        SymbolLightMemberModifierList(
            containingDeclaration = this,
            modifiersBox = GranularModifiersBox(computer = ::computeModifiers),
            annotationsBox = GranularAnnotationsBox(
                annotationsProvider = SymbolAnnotationsProvider(
                    ktModule = ktModule,
                    annotatedSymbolPointer = functionSymbolPointer,
                ),
                additionalAnnotationsProvider = CompositeAdditionalAnnotationsProvider(
                    NullabilityAnnotationsProvider {
                        if (modifierList.hasModifierProperty(PsiModifier.PRIVATE)) {
                            NullabilityType.Unknown
                        } else {
                            withFunctionSymbol { functionSymbol ->
                                if (functionSymbol.isSuspend) { // Any?
                                    NullabilityType.Nullable
                                } else {
                                    konst returnType = functionSymbol.returnType
                                    if (returnType.isVoidType) NullabilityType.Unknown else getTypeNullability(returnType)
                                }
                            }
                        }
                    },
                    MethodAdditionalAnnotationsProvider,
                ),
            )
        )
    }

    override fun getModifierList(): PsiModifierList = _modifierList

    override fun isConstructor(): Boolean = false

    override fun isOverride(): Boolean = _isOverride

    private konst _isOverride: Boolean by lazyPub {
        if (isTopLevel) false else withFunctionSymbol { it.isOverride }
    }

    // Inspired by KotlinTypeMapper#forceBoxedReturnType
    private fun forceBoxedReturnType(): Boolean {
        return withFunctionSymbol { functionSymbol ->
            konst returnType = functionSymbol.returnType
            // 'invoke' methods for lambdas, function literals, and callable references
            // implicitly override generic 'invoke' from a corresponding base class.
            if (functionSymbol.isBuiltinFunctionInvoke && returnType.isInlineClassType)
                return@withFunctionSymbol true

            returnType.isPrimitive &&
                    functionSymbol.getAllOverriddenSymbols().any { overriddenSymbol ->
                        !overriddenSymbol.returnType.isPrimitive
                    }
        }
    }

    private konst KtType.isInlineClassType: Boolean
        get() = ((this as? KtNonErrorClassType)?.classSymbol as? KtNamedClassOrObjectSymbol)?.isInline == true

    private konst KtType.isVoidType: Boolean get() = isUnit && nullabilityType != NullabilityType.Nullable

    private konst _returnedType: PsiType by lazyPub {
        withFunctionSymbol { functionSymbol ->
            konst ktType = if (functionSymbol.isSuspend) {
                analysisSession.builtinTypes.NULLABLE_ANY // Any?
            } else {
                functionSymbol.returnType.takeUnless { it.isVoidType } ?: return@withFunctionSymbol PsiType.VOID
            }

            konst typeMappingMode = if (forceBoxedReturnType()) KtTypeMappingMode.RETURN_TYPE_BOXED else KtTypeMappingMode.RETURN_TYPE

            ktType.asPsiTypeElement(
                this@SymbolLightSimpleMethod,
                allowErrorTypes = true,
                typeMappingMode,
                this@SymbolLightSimpleMethod.containingClass.isAnnotationType,
            )?.let {
                annotateByKtType(it.type, ktType, it, modifierList)
            }
        } ?: nonExistentType()
    }

    override fun getReturnType(): PsiType = _returnedType
}
