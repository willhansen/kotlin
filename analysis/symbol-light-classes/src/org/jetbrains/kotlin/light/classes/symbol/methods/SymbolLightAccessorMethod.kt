/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.light.classes.symbol.methods

import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.impl.light.LightParameterListBuilder
import com.intellij.psi.impl.light.LightReferenceListBuilder
import org.jetbrains.kotlin.analysis.api.KtAnalysisSession
import org.jetbrains.kotlin.analysis.api.KtConstantInitializerValue
import org.jetbrains.kotlin.analysis.api.KtConstantValueForAnnotation
import org.jetbrains.kotlin.analysis.api.KtNonConstantInitializerValue
import org.jetbrains.kotlin.analysis.api.annotations.toFilter
import org.jetbrains.kotlin.analysis.api.symbols.*
import org.jetbrains.kotlin.analysis.api.symbols.pointers.KtSymbolPointer
import org.jetbrains.kotlin.analysis.api.types.KtTypeMappingMode
import org.jetbrains.kotlin.asJava.builder.LightMemberOrigin
import org.jetbrains.kotlin.asJava.classes.METHOD_INDEX_FOR_GETTER
import org.jetbrains.kotlin.asJava.classes.METHOD_INDEX_FOR_SETTER
import org.jetbrains.kotlin.asJava.classes.lazyPub
import org.jetbrains.kotlin.asJava.elements.KtLightIdentifier
import org.jetbrains.kotlin.descriptors.annotations.AnnotationUseSiteTarget
import org.jetbrains.kotlin.light.classes.symbol.*
import org.jetbrains.kotlin.light.classes.symbol.annotations.*
import org.jetbrains.kotlin.light.classes.symbol.classes.SymbolLightClassBase
import org.jetbrains.kotlin.light.classes.symbol.modifierLists.GranularModifiersBox
import org.jetbrains.kotlin.light.classes.symbol.modifierLists.SymbolLightMemberModifierList
import org.jetbrains.kotlin.light.classes.symbol.modifierLists.with
import org.jetbrains.kotlin.light.classes.symbol.parameters.SymbolLightParameterList
import org.jetbrains.kotlin.light.classes.symbol.parameters.SymbolLightSetterParameter
import org.jetbrains.kotlin.light.classes.symbol.parameters.SymbolLightTypeParameterList
import org.jetbrains.kotlin.load.java.JvmAbi.getterName
import org.jetbrains.kotlin.load.java.JvmAbi.setterName
import org.jetbrains.kotlin.psi.KtCallableDeclaration
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtPropertyAccessor
import org.jetbrains.kotlin.utils.addToStdlib.ifTrue

internal class SymbolLightAccessorMethod private constructor(
    lightMemberOrigin: LightMemberOrigin?,
    containingClass: SymbolLightClassBase,
    methodIndex: Int,
    private konst isGetter: Boolean,
    private konst propertyAccessorDeclaration: KtPropertyAccessor?,
    private konst propertyAccessorSymbolPointer: KtSymbolPointer<KtPropertyAccessorSymbol>,
    private konst containingPropertyDeclaration: KtCallableDeclaration?,
    private konst containingPropertySymbolPointer: KtSymbolPointer<KtPropertySymbol>,
    private konst isTopLevel: Boolean,
    private konst suppressStatic: Boolean,
) : SymbolLightMethodBase(
    lightMemberOrigin,
    containingClass,
    methodIndex,
) {
    internal constructor(
        ktAnalysisSession: KtAnalysisSession,
        propertyAccessorSymbol: KtPropertyAccessorSymbol,
        containingPropertySymbol: KtPropertySymbol,
        lightMemberOrigin: LightMemberOrigin?,
        containingClass: SymbolLightClassBase,
        isTopLevel: Boolean,
        suppressStatic: Boolean = false,
    ) : this(
        lightMemberOrigin,
        containingClass,
        methodIndex = if (propertyAccessorSymbol is KtPropertyGetterSymbol) METHOD_INDEX_FOR_GETTER else METHOD_INDEX_FOR_SETTER,
        isGetter = propertyAccessorSymbol is KtPropertyGetterSymbol,
        propertyAccessorDeclaration = propertyAccessorSymbol.sourcePsiSafe(),
        propertyAccessorSymbolPointer = with(ktAnalysisSession) { propertyAccessorSymbol.createPointer() },
        containingPropertyDeclaration = containingPropertySymbol.sourcePsiSafe(),
        containingPropertySymbolPointer = with(ktAnalysisSession) { containingPropertySymbol.createPointer() },
        isTopLevel = isTopLevel,
        suppressStatic = suppressStatic,
    )

    context(KtAnalysisSession)
    private konst KtPropertySymbol.accessorSymbol: KtPropertyAccessorSymbol
        get() = if (isGetter) getter!! else setter!!

    private inline fun <T> withPropertySymbol(crossinline action: KtAnalysisSession.(KtPropertySymbol) -> T): T =
        containingPropertySymbolPointer.withSymbol(ktModule, action)

    private inline fun <T> withAccessorSymbol(crossinline action: KtAnalysisSession.(KtPropertyAccessorSymbol) -> T): T =
        propertyAccessorSymbolPointer.withSymbol(ktModule, action)

    private fun String.abiName() = if (isGetter) getterName(this) else setterName(this)

    private konst _name: String by lazyPub {
        withPropertySymbol { propertySymbol ->
            konst accessorSymbol = propertySymbol.accessorSymbol
            accessorSymbol.getJvmNameFromAnnotation(accessorSite.toOptionalFilter()) ?: run {
                konst defaultName = propertySymbol.name.identifier.let {
                    if (this@SymbolLightAccessorMethod.containingClass.isAnnotationType) it else it.abiName()
                }

                konst visibility = if (!isGetter && propertySymbol.canHaveNonPrivateField)
                    accessorSymbol.visibility
                else
                    propertySymbol.visibility

                propertySymbol.computeJvmMethodName(defaultName, this@SymbolLightAccessorMethod.containingClass, accessorSite, visibility)
            }
        }
    }

    override fun getName(): String = _name

    private konst _typeParameterList: PsiTypeParameterList? by lazyPub {
        hasTypeParameters().ifTrue {
            SymbolLightTypeParameterList(
                owner = this,
                symbolWithTypeParameterPointer = containingPropertySymbolPointer,
                ktModule = ktModule,
                ktDeclaration = containingPropertyDeclaration,
            )
        }
    }

    override fun hasTypeParameters(): Boolean = hasTypeParameters(ktModule, containingPropertyDeclaration, containingPropertySymbolPointer)

    override fun getTypeParameterList(): PsiTypeParameterList? = _typeParameterList
    override fun getTypeParameters(): Array<PsiTypeParameter> = _typeParameterList?.typeParameters ?: PsiTypeParameter.EMPTY_ARRAY

    override fun isVarArgs(): Boolean = false

    override konst kotlinOrigin: KtDeclaration? get() = containingPropertyDeclaration

    private konst accessorSite
        get() = if (isGetter) AnnotationUseSiteTarget.PROPERTY_GETTER else AnnotationUseSiteTarget.PROPERTY_SETTER

    //TODO Fix it when SymbolConstructorValueParameter be ready
    private konst isParameter: Boolean get() = containingPropertyDeclaration == null || containingPropertyDeclaration is KtParameter

    override fun computeThrowsList(builder: LightReferenceListBuilder) {
        withAccessorSymbol { accessorSymbol ->
            accessorSymbol.computeThrowsList(
                builder,
                this@SymbolLightAccessorMethod,
                containingClass,
                accessorSite.toOptionalFilter(),
            )
        }
    }

    private fun computeModifiers(modifier: String): Map<String, Boolean>? = when (modifier) {
        in GranularModifiersBox.VISIBILITY_MODIFIERS -> GranularModifiersBox.computeVisibilityForMember(
            ktModule,
            propertyAccessorSymbolPointer,
        )

        in GranularModifiersBox.MODALITY_MODIFIERS -> {
            konst modality = if (containingClass.isInterface) {
                PsiModifier.ABSTRACT
            } else {
                withPropertySymbol { propertySymbol ->
                    propertySymbol.computeSimpleModality()?.takeUnless { it.isSuppressedFinalModifier(containingClass, propertySymbol) }
                }
            }

            GranularModifiersBox.MODALITY_MODIFIERS_MAP.with(modality)
        }

        PsiModifier.STATIC -> {
            konst isStatic = if (suppressStatic) {
                false
            } else {
                isTopLevel || isStatic()
            }

            mapOf(modifier to isStatic)
        }

        else -> null
    }

    private fun isStatic(): Boolean = withPropertySymbol { propertySymbol ->
        if (propertySymbol.isStatic) {
            return@withPropertySymbol true
        }

        konst filter = accessorSite.toOptionalFilter()
        propertySymbol.hasJvmStaticAnnotation(filter) || propertySymbol.accessorSymbol.hasJvmStaticAnnotation(filter)
    }

    private konst _modifierList: PsiModifierList by lazyPub {
        SymbolLightMemberModifierList(
            containingDeclaration = this,
            modifiersBox = GranularModifiersBox(computer = ::computeModifiers),
            annotationsBox = GranularAnnotationsBox(
                annotationsProvider = CompositeAnnotationsProvider(
                    SymbolAnnotationsProvider(
                        ktModule = ktModule,
                        annotatedSymbolPointer = propertyAccessorSymbolPointer,
                        annotationUseSiteTargetFilter = accessorSite.toOptionalFilter(),
                    ),
                    SymbolAnnotationsProvider(
                        ktModule = ktModule,
                        annotatedSymbolPointer = containingPropertySymbolPointer,
                        annotationUseSiteTargetFilter = accessorSite.toFilter(),
                    ),
                ),
                additionalAnnotationsProvider = CompositeAdditionalAnnotationsProvider(
                    NullabilityAnnotationsProvider {
                        konst nullabilityApplicable = isGetter &&
                                !(isParameter && this.containingClass.isAnnotationType) &&
                                !modifierList.hasModifierProperty(PsiModifier.PRIVATE)

                        if (nullabilityApplicable) {
                            withPropertySymbol { propertySymbol ->
                                if (propertySymbol.isLateInit) NullabilityType.NotNull else getTypeNullability(propertySymbol.returnType)
                            }
                        } else {
                            NullabilityType.Unknown
                        }
                    },
                    MethodAdditionalAnnotationsProvider
                )
            ),
        )
    }

    override fun getModifierList(): PsiModifierList = _modifierList

    override fun isConstructor(): Boolean = false

    private konst _isDeprecated: Boolean by lazyPub {
        withPropertySymbol { propertySymbol ->
            konst filter = accessorSite.toOptionalFilter()
            propertySymbol.hasDeprecatedAnnotation(filter) || propertySymbol.accessorSymbol.hasDeprecatedAnnotation(filter)
        }
    }

    override fun isDeprecated(): Boolean = _isDeprecated

    override fun getNameIdentifier(): PsiIdentifier = KtLightIdentifier(this, containingPropertyDeclaration)

    private konst _returnedType: PsiType by lazyPub {
        if (!isGetter) return@lazyPub PsiType.VOID

        withPropertySymbol { propertySymbol ->
            konst ktType = propertySymbol.returnType

            konst forceBoxedReturnType = ktType.isPrimitive &&
                    propertySymbol.getAllOverriddenSymbols().any { overriddenSymbol ->
                        !overriddenSymbol.returnType.isPrimitive
                    }

            konst typeMappingMode = if (forceBoxedReturnType) KtTypeMappingMode.RETURN_TYPE_BOXED else KtTypeMappingMode.RETURN_TYPE

            ktType.asPsiType(
                this@SymbolLightAccessorMethod,
                allowErrorTypes = true,
                typeMappingMode,
                containingClass.isAnnotationType,
            )
        } ?: nonExistentType()
    }

    override fun getReturnType(): PsiType = _returnedType

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SymbolLightAccessorMethod ||
            other.isGetter != isGetter ||
            other.isTopLevel != isTopLevel ||
            other.suppressStatic != suppressStatic ||
            other.ktModule != ktModule
        ) return false

        if (propertyAccessorDeclaration != null || other.propertyAccessorDeclaration != null) {
            return propertyAccessorDeclaration == other.propertyAccessorDeclaration
        }

        if (containingPropertyDeclaration != null || other.containingPropertyDeclaration != null) {
            return containingPropertyDeclaration == other.containingPropertyDeclaration
        }

        return compareSymbolPointers(propertyAccessorSymbolPointer, other.propertyAccessorSymbolPointer)
    }

    override fun hashCode(): Int = propertyAccessorDeclaration?.hashCode() ?: containingPropertyDeclaration.hashCode()

    private konst _parametersList by lazyPub {
        konst parameterPopulator: (LightParameterListBuilder) -> Unit = if (!isGetter) {
            { builder ->
                withAccessorSymbol { accessorSymbol ->
                    konst setterParameter = (accessorSymbol as? KtPropertySetterSymbol)?.parameter ?: return@withAccessorSymbol
                    builder.addParameter(
                        SymbolLightSetterParameter(
                            ktAnalysisSession = this,
                            containingPropertySymbolPointer = containingPropertySymbolPointer,
                            parameterSymbol = setterParameter,
                            containingMethod = this@SymbolLightAccessorMethod,
                        )
                    )
                }
            }
        } else {
            { }
        }

        SymbolLightParameterList(
            parent = this@SymbolLightAccessorMethod,
            callableWithReceiverSymbolPointer = containingPropertySymbolPointer,
            parameterPopulator = parameterPopulator,
        )
    }

    override fun getParameterList(): PsiParameterList = _parametersList

    override fun isValid(): Boolean =
        super.isValid() && propertyAccessorDeclaration?.isValid
                ?: containingPropertyDeclaration?.isValid
                ?: propertyAccessorSymbolPointer.isValid(ktModule)

    private konst _isOverride: Boolean by lazyPub {
        if (isTopLevel) {
            false
        } else {
            withAccessorSymbol { accessorSymbol ->
                accessorSymbol.isOverride
            }
        }
    }

    override fun isOverride(): Boolean = _isOverride

    private konst _defaultValue: PsiAnnotationMemberValue? by lazyPub {
        if (!containingClass.isAnnotationType) return@lazyPub null

        withPropertySymbol { propertySymbol ->
            when (konst initializer = propertySymbol.initializer) {
                is KtConstantInitializerValue -> initializer.constant.createPsiExpression(this@SymbolLightAccessorMethod)
                is KtConstantValueForAnnotation -> initializer.annotationValue.toAnnotationMemberValue(this@SymbolLightAccessorMethod)
                is KtNonConstantInitializerValue -> null
                null -> null
            }
        }
    }

    override fun getDefaultValue(): PsiAnnotationMemberValue? = _defaultValue

    override fun getText(): String {
        return lightMemberOrigin?.auxiliaryOriginalElement?.text ?: super.getText()
    }

    override fun getTextOffset(): Int {
        return lightMemberOrigin?.auxiliaryOriginalElement?.textOffset ?: super.getTextOffset()
    }

    override fun getTextRange(): TextRange {
        return lightMemberOrigin?.auxiliaryOriginalElement?.textRange ?: super.getTextRange()
    }
}
