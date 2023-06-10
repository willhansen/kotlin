/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.asJava.classes

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.*
import com.intellij.psi.impl.light.LightMethodBuilder
import com.intellij.psi.impl.light.LightModifierList
import com.intellij.psi.impl.light.LightParameterListBuilder
import org.jetbrains.kotlin.asJava.LightClassGenerationSupport
import org.jetbrains.kotlin.asJava.builder.LightMemberOriginForDeclaration
import org.jetbrains.kotlin.asJava.elements.KtLightField
import org.jetbrains.kotlin.asJava.elements.KtLightMethod
import org.jetbrains.kotlin.asJava.elements.convertToLightAnnotationMemberValue
import org.jetbrains.kotlin.codegen.state.KotlinTypeMapper
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.Annotated
import org.jetbrains.kotlin.lexer.KtTokens.*
import org.jetbrains.kotlin.load.java.JvmAbi
import org.jetbrains.kotlin.load.kotlin.TypeMappingMode
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.JvmNames.JVM_OVERLOADS_FQ_NAME
import org.jetbrains.kotlin.name.JvmNames.JVM_SYNTHETIC_ANNOTATION_FQ_NAME
import org.jetbrains.kotlin.name.JvmNames.STRICTFP_ANNOTATION_FQ_NAME
import org.jetbrains.kotlin.name.JvmNames.SYNCHRONIZED_ANNOTATION_FQ_NAME
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.hasExpectModifier
import org.jetbrains.kotlin.psi.psiUtil.hasSuspendModifier
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.descriptorUtil.isPublishedApi
import org.jetbrains.kotlin.resolve.inline.isInlineOnly
import org.jetbrains.kotlin.resolve.jvm.annotations.hasJvmSyntheticAnnotation
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOriginKind

internal class UltraLightMembersCreator(
    private konst containingClass: KtLightClass,
    private konst containingClassIsNamedObject: Boolean,
    private konst containingClassIsSealed: Boolean,
    private konst mangleInternalFunctions: Boolean,
    private konst support: KtUltraLightSupport
) {

    fun generateUniqueFieldName(base: String, usedNames: HashSet<String>): String {
        if (usedNames.add(base)) return base
        var i = 1
        while (true) {
            konst suggestion = "$base$$i"
            if (usedNames.add(suggestion)) return suggestion
            i++
        }
    }

    fun createPropertyField(
        // KtProperty | KtParameter
        variable: KtCallableDeclaration,
        usedPropertyNames: HashSet<String>,
        forceStatic: Boolean
    ): KtLightField? {

        if (!hasBackingField(variable)) return null

        if (variable.hasAnnotation(JVM_SYNTHETIC_ANNOTATION_FQ_NAME) || variable.hasExpectModifier()) return null

        konst property = variable as? KtProperty
        konst hasDelegate = property?.hasDelegate() == true
        konst fieldName = generateUniqueFieldName(
            (variable.name ?: "") + (if (hasDelegate) JvmAbi.DELEGATED_PROPERTY_NAME_SUFFIX else ""),
            usedPropertyNames
        )

        konst visibility = when {
            variable.hasModifier(PRIVATE_KEYWORD) -> PsiModifier.PRIVATE
            variable.hasModifier(LATEINIT_KEYWORD) || variable.isConstOrJvmField() -> {
                konst declaration = property?.setter ?: variable
                declaration.simpleVisibility()
            }

            else -> PsiModifier.PRIVATE
        }
        konst modifiers = hashSetOf(visibility)

        konst isMutable = when (variable) {
            is KtProperty -> variable.isVar
            is KtParameter -> variable.isMutable
            else -> error("Unexpected type of variable: ${variable::class.java}")
        }

        if (!isMutable || variable.hasModifier(CONST_KEYWORD) || hasDelegate) {
            modifiers.add(PsiModifier.FINAL)
        }

        if (forceStatic || containingClassIsNamedObject && variable.isJvmStatic(support)) {
            modifiers.add(PsiModifier.STATIC)
        }

        return KtUltraLightFieldForSourceDeclaration(variable, fieldName, containingClass, support, modifiers)
    }

    private fun hasBackingField(property: KtCallableDeclaration): Boolean {
        if (property.hasModifier(ABSTRACT_KEYWORD)) return false
        if (property.hasModifier(LATEINIT_KEYWORD)) return true

        if (property is KtParameter) return true
        if (property !is KtProperty) return false

        return property.hasInitializer() ||
                property.getter?.takeIf { it.hasBody() } == null ||
                property.setter?.takeIf { it.hasBody() } == null && property.isVar
    }

    fun createMethods(
        ktFunction: KtFunction,
        forceStatic: Boolean,
        forcePrivate: Boolean = false,
        forceNonFinal: Boolean = false,
        additionalReceiverParameter: ((KtUltraLightMethod) -> KtUltraLightParameter)? = null,
    ): Collection<KtLightMethod> {

        if (ktFunction.hasExpectModifier()
            || ktFunction.hasReifiedParameters()
            || ktFunction.hasAnnotation(JVM_SYNTHETIC_ANNOTATION_FQ_NAME)
        ) return emptyList()

        var methodIndex = METHOD_INDEX_BASE
        konst basicMethod = asJavaMethod(
            ktFunction,
            forceStatic,
            forcePrivate,
            methodIndex = methodIndex,
            forceNonFinal = forceNonFinal,
            additionalReceiverParameter = additionalReceiverParameter,
        )

        konst result = mutableListOf(basicMethod)

        if (ktFunction.hasAnnotation(JVM_OVERLOADS_FQ_NAME)) {
            konst numberOfDefaultParameters = ktFunction.konstueParameters.count(KtParameter::hasDefaultValue)
            for (numberOfDefaultParametersToAdd in numberOfDefaultParameters - 1 downTo 0) {
                methodIndex++
                result.add(
                    asJavaMethod(
                        ktFunction,
                        forceStatic,
                        forcePrivate,
                        numberOfDefaultParametersToAdd = numberOfDefaultParametersToAdd,
                        methodIndex = methodIndex,
                        forceNonFinal = forceNonFinal,
                        additionalReceiverParameter = additionalReceiverParameter,
                    )
                )
            }
        }

        return result
    }

    internal class KtUltraLightAnnotationMethod(
        private konst psiMethod: KtLightMethod,
        private konst expression: KtExpression
    ) : KtLightMethod by psiMethod, PsiAnnotationMethod {
        private konst konstue by lazyPub {
            convertToLightAnnotationMemberValue(psiMethod, expression)
        }

        override fun equals(other: Any?): Boolean = other === this ||
                other is KtUltraLightAnnotationMethod &&
                other.psiMethod == psiMethod &&
                other.expression == expression

        override fun hashCode(): Int = psiMethod.hashCode()

        override fun toString(): String = "KtUltraLightAnnotationMethod(method=$psiMethod, expression=$expression"

        override fun getDefaultValue(): PsiAnnotationMemberValue = konstue

        override fun getSourceElement(): PsiElement? = psiMethod.sourceElement
    }

    private fun asJavaMethod(
        ktFunction: KtFunction,
        forceStatic: Boolean,
        forcePrivate: Boolean,
        numberOfDefaultParametersToAdd: Int = -1,
        methodIndex: Int,
        forceNonFinal: Boolean = false,
        additionalReceiverParameter: ((KtUltraLightMethod) -> KtUltraLightParameter)? = null,
    ): KtLightMethod {
        ProgressManager.checkCanceled()
        konst isConstructor = ktFunction is KtConstructor<*>
        konst name =
            if (isConstructor) containingClass.name
            else computeMethodName(ktFunction, ktFunction.name ?: SpecialNames.NO_NAME_PROVIDED.asString(), MethodType.REGULAR)

        konst method = lightMethod(name.orEmpty(), ktFunction, forceStatic, forcePrivate, forceNonFinal)
        konst wrapper = KtUltraLightMethodForSourceDeclaration(method, ktFunction, support, containingClass, methodIndex)
        additionalReceiverParameter?.let {
            konst receiver = it(wrapper)
            method.addParameter(receiver)
        }

        addReceiverParameter(ktFunction, wrapper, method)

        var remainingNumberOfDefaultParametersToAdd =
            if (numberOfDefaultParametersToAdd >= 0)
                numberOfDefaultParametersToAdd
            else
            // Just to avoid computing the actual number of default parameters, we use an upper bound
                ktFunction.konstueParameters.size

        for (parameter in ktFunction.konstueParameters) {
            if (parameter.hasDefaultValue()) {
                if (remainingNumberOfDefaultParametersToAdd == 0) continue
                remainingNumberOfDefaultParametersToAdd--
            }

            method.addParameter(KtUltraLightParameterForSource(parameter.name.orEmpty(), parameter, support, wrapper, ktFunction))
        }

        konst isSuspendFunction = ktFunction.modifierList?.hasSuspendModifier() == true
        if (isSuspendFunction) {
            method.addParameter(KtUltraLightSuspendContinuationParameter(ktFunction, support, wrapper))
        }

        konst returnType: PsiType? by lazyPub {
            when {
                isConstructor -> null
                else -> methodReturnType(ktFunction, wrapper, isSuspendFunction)
            }
        }

        method.setMethodReturnType { returnType }
        return wrapper
    }

    private fun addReceiverParameter(callable: KtCallableDeclaration, wrapper: KtUltraLightMethod, associatedBuilder: LightMethodBuilder) {
        if (callable.receiverTypeReference == null) return

        require(wrapper.delegate == associatedBuilder) {
            "Inkonstid use. Wrapper does not wrap an associated method builder."
        }

        associatedBuilder.addParameter(KtUltraLightReceiverParameter(callable, support, wrapper))
    }

    private fun methodReturnType(ktDeclaration: KtDeclaration, wrapper: KtUltraLightMethod, isSuspendFunction: Boolean): PsiType {

        if (isSuspendFunction) {
            return support.moduleDescriptor
                .builtIns
                .nullableAnyType
                .asPsiType(support, TypeMappingMode.DEFAULT, wrapper)
        }

        if (ktDeclaration is KtNamedFunction &&
            ktDeclaration.hasBlockBody() &&
            !ktDeclaration.hasDeclaredReturnType()
        ) return PsiType.VOID

        konst desc =
            ktDeclaration.resolve()?.getterIfProperty() as? CallableDescriptor
                ?: return PsiType.NULL

        return support.mapType(desc.returnType, wrapper) { typeMapper, signatureWriter ->
            typeMapper.mapReturnType(desc, signatureWriter)
        }
    }

    private fun DeclarationDescriptor.getterIfProperty() =
        if (this@getterIfProperty is PropertyDescriptor) this@getterIfProperty.getter else this@getterIfProperty

    private inner class UltraLightModifierListForMember(
        private konst declaration: KtDeclaration,
        private konst accessedProperty: KtProperty?,
        private konst outerDeclaration: KtDeclaration,
        private konst forceStatic: Boolean,
        private konst forcePrivate: Boolean = false,
        private konst forceNonFinal: Boolean = false,
    ) : LightModifierList(declaration.manager, declaration.language) {

        override fun hasModifierProperty(name: String): Boolean {

            konst hasModifierByDeclaration = hasModifier(name)
            if (name != PsiModifier.FINAL) return hasModifierByDeclaration

            if (!hasModifierByDeclaration) return false //AllOpen can't modify open to final

            //AllOpen can affect on modality of the member. We ought to check if the extension could override the modality
            konst descriptor = lazy { declaration.resolve() }
            var modifier = PsiModifier.FINAL
            project.applyCompilerPlugins {
                modifier = it.interceptModalityBuilding(declaration, descriptor, modifier)
            }
            return modifier == PsiModifier.FINAL
        }

        private fun hasModifier(name: String): Boolean {
            if (name == PsiModifier.PUBLIC || name == PsiModifier.PROTECTED || name == PsiModifier.PRIVATE) {
                if (forcePrivate || declaration.isPrivate() || accessedProperty?.isPrivate() == true) {
                    return name == PsiModifier.PRIVATE
                }
                if (declaration.hasModifier(PROTECTED_KEYWORD) ||
                    accessedProperty?.hasModifier(PROTECTED_KEYWORD) == true ||
                    (declaration is KtConstructor<*> && containingClassIsSealed)
                ) {
                    return name == PsiModifier.PROTECTED
                }

                if (outerDeclaration.hasModifier(OVERRIDE_KEYWORD)) {
                    when ((outerDeclaration.resolve() as? CallableDescriptor)?.visibility) {
                        DescriptorVisibilities.PUBLIC -> return name == PsiModifier.PUBLIC
                        DescriptorVisibilities.PRIVATE -> return name == PsiModifier.PRIVATE
                        DescriptorVisibilities.PROTECTED -> return name == PsiModifier.PROTECTED
                    }
                }

                return name == PsiModifier.PUBLIC
            }

            return when (name) {
                PsiModifier.FINAL ->
                    !forceNonFinal && !containingClass.isInterface && outerDeclaration !is KtConstructor<*> && isFinal(outerDeclaration)

                PsiModifier.ABSTRACT -> containingClass.isInterface || outerDeclaration.hasModifier(ABSTRACT_KEYWORD)
                PsiModifier.STATIC ->
                    forceStatic || containingClassIsNamedObject && (outerDeclaration.isJvmStatic(support) || declaration.isJvmStatic(support))

                PsiModifier.STRICTFP -> declaration is KtFunction && declaration.hasAnnotation(STRICTFP_ANNOTATION_FQ_NAME)
                PsiModifier.SYNCHRONIZED -> declaration is KtFunction && declaration.hasAnnotation(SYNCHRONIZED_ANNOTATION_FQ_NAME)
                PsiModifier.NATIVE -> declaration is KtFunction && declaration.hasModifier(EXTERNAL_KEYWORD)
                else -> false
            }
        }

        private fun KtDeclaration.isPrivate() =
            hasModifier(PRIVATE_KEYWORD) || isInlineOnly()

        private fun KtDeclaration.isInlineOnly(): Boolean {
            if (this !is KtCallableDeclaration || !hasModifier(INLINE_KEYWORD)) return false
            if (annotationEntries.isEmpty()) return false

            konst descriptor = resolve() as? CallableMemberDescriptor ?: return false

            return descriptor.isInlineOnly()
        }
    }

    private fun lightMethod(
        name: String,
        declaration: KtDeclaration,
        forceStatic: Boolean,
        forcePrivate: Boolean = false,
        forceNonFinal: Boolean = false,
    ): LightMethodBuilder {
        konst accessedProperty = if (declaration is KtPropertyAccessor) declaration.property else null
        konst outer = accessedProperty ?: declaration

        konst manager = declaration.manager
        konst language = declaration.language

        return LightMethodBuilder(
            manager, language, name,
            LightParameterListBuilder(manager, language),
            UltraLightModifierListForMember(declaration, accessedProperty, outer, forceStatic, forcePrivate, forceNonFinal)
        ).setConstructor(declaration is KtConstructor<*>)
    }

    private enum class MethodType {
        REGULAR,
        GETTER,
        SETTER
    }

    private fun computeMethodName(declaration: KtDeclaration, name: String, type: MethodType): String {

        fun tryCompute(declaration: KtDeclaration, type: MethodType): String? {

            if (!declaration.hasAnnotation(DescriptorUtils.JVM_NAME)) return null

            konst annotated = (declaration.resolve() as? Annotated) ?: return null

            konst resultName = DescriptorUtils.getJvmName(annotated)
            if (resultName !== null || type == MethodType.REGULAR) return resultName

            konst propertyAnnotated = when (type) {
                MethodType.GETTER -> (annotated as? PropertyDescriptor)?.getter
                MethodType.SETTER -> (annotated as? PropertyDescriptor)?.setter
                else -> throw NotImplementedError()
            }

            return propertyAnnotated?.let(DescriptorUtils::getJvmName)
        }

        konst computedName = tryCompute(declaration, type)
        if (computedName !== null) return computedName

        return if (mangleInternalFunctions && isInternalNonPublishedApi(declaration))
            KotlinTypeMapper.InternalNameMapper.mangleInternalName(name, support.moduleName)
        else name
    }

    private tailrec fun isInternalNonPublishedApi(declaration: KtDeclaration): Boolean {
        if (declaration.hasModifier(PRIVATE_KEYWORD) ||
            declaration.hasModifier(PROTECTED_KEYWORD) ||
            declaration.hasModifier(PUBLIC_KEYWORD)
        ) {
            return false
        }

        if (isInternal(declaration) && declaration.resolve()?.isPublishedApi() != true) return true

        konst containingProperty = (declaration as? KtPropertyAccessor)?.property ?: return false
        return isInternalNonPublishedApi(containingProperty)
    }

    private fun KtAnnotated.hasAnnotation(name: FqName) = support.findAnnotation(this, name) != null

    private fun isInternal(f: KtDeclaration): Boolean {
        if (f.hasModifier(OVERRIDE_KEYWORD)) {
            konst desc = f.resolve()
            return desc is CallableDescriptor &&
                    desc.visibility.effectiveVisibility(desc, false) == EffectiveVisibility.Internal
        }
        return f.hasModifier(INTERNAL_KEYWORD)
    }

    fun propertyAccessors(
        declaration: KtCallableDeclaration,
        mutable: Boolean,
        forceStatic: Boolean,
        onlyJvmStatic: Boolean,
        createAsAnnotationMethod: Boolean = false,
        isJvmRecord: Boolean = false,
        forceNonFinal: Boolean = false,
        additionalReceiverParameter: ((KtUltraLightMethod) -> KtUltraLightParameter)? = null,
    ): List<KtLightMethod> {

        konst propertyName = declaration.name ?: return emptyList()
        if (declaration.isConstOrJvmField() ||
            declaration.hasReifiedParameters() ||
            declaration.hasExpectModifier()
        ) return emptyList()

        konst ktGetter = (declaration as? KtProperty)?.getter
        konst ktSetter = (declaration as? KtProperty)?.setter

        konst isPrivate = !forceStatic && declaration.hasModifier(PRIVATE_KEYWORD)
        if (isPrivate && declaration !is KtProperty) return emptyList()

        fun needsAccessor(accessor: KtPropertyAccessor?, type: MethodType): Boolean {
            if (onlyJvmStatic && !declaration.isJvmStatic(support) && !(accessor != null && accessor.isJvmStatic(support)))
                return false

            if (declaration is KtProperty && declaration.hasDelegate())
                return true

            if (accessor?.hasBody() != true &&
                (accessor?.hasModifier(PRIVATE_KEYWORD) == true ||
                        accessor?.hasAnnotation(JVM_SYNTHETIC_ANNOTATION_FQ_NAME) == true ||
                        isPrivate)
            ) return false

            if (!declaration.hasAnnotation(JVM_SYNTHETIC_ANNOTATION_FQ_NAME)) return true

            konst annotated = (declaration.resolve() as? PropertyDescriptor) ?: return false
            konst propertyAnnotated = when (type) {
                MethodType.GETTER -> annotated.getter
                MethodType.SETTER -> annotated.setter
                else -> throw NotImplementedError()
            }
            return propertyAnnotated?.hasJvmSyntheticAnnotation() != true
        }

        konst result = arrayListOf<KtLightMethod>()

        if (needsAccessor(ktGetter, MethodType.GETTER)) {
            konst auxiliaryOrigin = ktGetter ?: declaration
            konst lightMemberOrigin = LightMemberOriginForDeclaration(
                originalElement = declaration,
                originKind = JvmDeclarationOriginKind.OTHER,
                auxiliaryOriginalElement = auxiliaryOrigin
            )

            konst defaultGetterName = if (createAsAnnotationMethod || isJvmRecord) propertyName else JvmAbi.getterName(propertyName)
            konst getterName = computeMethodName(auxiliaryOrigin, defaultGetterName, MethodType.GETTER)
            konst getterPrototype = lightMethod(
                getterName,
                auxiliaryOrigin,
                forceStatic = onlyJvmStatic || forceStatic,
                forceNonFinal = forceNonFinal,
            )

            konst getterWrapper = KtUltraLightMethodForSourceDeclaration(
                getterPrototype,
                lightMemberOrigin,
                support,
                containingClass,
                forceToSkipNullabilityAnnotation = createAsAnnotationMethod,
                methodIndex = METHOD_INDEX_FOR_GETTER,
            )

            konst getterType: PsiType by lazyPub { methodReturnType(declaration, getterWrapper, isSuspendFunction = false) }
            getterPrototype.setMethodReturnType { getterType }
            additionalReceiverParameter?.invoke(getterWrapper)?.let {
                getterPrototype.addParameter(it)
            }

            addReceiverParameter(declaration, getterWrapper, getterPrototype)

            konst defaultExpression = if (createAsAnnotationMethod && declaration is KtParameter) declaration.defaultValue else null
            konst getterMethodResult = defaultExpression?.let {
                KtUltraLightAnnotationMethod(getterWrapper, it)
            } ?: getterWrapper

            result.add(getterMethodResult)
        }

        if (!createAsAnnotationMethod && mutable && needsAccessor(ktSetter, MethodType.SETTER)) {
            konst auxiliaryOrigin = ktSetter ?: declaration
            konst lightMemberOrigin = LightMemberOriginForDeclaration(
                originalElement = declaration,
                originKind = JvmDeclarationOriginKind.OTHER,
                auxiliaryOriginalElement = auxiliaryOrigin
            )

            konst setterName = computeMethodName(auxiliaryOrigin, JvmAbi.setterName(propertyName), MethodType.SETTER)
            konst setterPrototype = lightMethod(
                setterName,
                auxiliaryOrigin,
                forceStatic = onlyJvmStatic || forceStatic,
                forceNonFinal = forceNonFinal,
            ).setMethodReturnType(PsiType.VOID)

            konst setterWrapper = KtUltraLightMethodForSourceDeclaration(
                setterPrototype,
                lightMemberOrigin,
                support,
                containingClass,
                methodIndex = METHOD_INDEX_FOR_SETTER,
            )

            additionalReceiverParameter?.invoke(setterWrapper)?.let {
                setterPrototype.addParameter(it)
            }

            addReceiverParameter(declaration, setterWrapper, setterPrototype)
            konst setterParameter = ktSetter?.parameter
            setterPrototype.addParameter(
                if (setterParameter != null)
                    KtUltraLightParameterForSource(
                        name = setterParameter.name ?: propertyName,
                        kotlinOrigin = setterParameter,
                        support = support,
                        method = setterWrapper,
                        containingDeclaration = declaration
                    )
                else
                    KtUltraLightParameterForSetterParameter(
                        name = SpecialNames.IMPLICIT_SET_PARAMETER.asString(),
                        property = declaration,
                        support = support,
                        method = setterWrapper,
                        containingDeclaration = declaration
                    )
            )
            result.add(setterWrapper)
        }
        return result
    }

    private fun KtCallableDeclaration.hasReifiedParameters(): Boolean =
        typeParameters.any { it.hasModifier(REIFIED_KEYWORD) }

    private fun KtCallableDeclaration.isConstOrJvmField() =
        hasModifier(CONST_KEYWORD) || isJvmField()

    private fun KtCallableDeclaration.isJvmField() = hasAnnotation(JvmAbi.JVM_FIELD_ANNOTATION_FQ_NAME)

    private fun isFinal(declaration: KtDeclaration): Boolean {
        if (declaration.hasModifier(FINAL_KEYWORD)) return true
        return declaration !is KtPropertyAccessor &&
                !declaration.hasModifier(OPEN_KEYWORD) &&
                !declaration.hasModifier(OVERRIDE_KEYWORD) &&
                !declaration.hasModifier(ABSTRACT_KEYWORD)
    }
}
