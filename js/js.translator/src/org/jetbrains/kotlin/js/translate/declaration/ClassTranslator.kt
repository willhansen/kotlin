/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.js.translate.declaration

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.builtins.functions.FunctionClassDescriptor
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.backend.ast.metadata.forcedReturnVariable
import org.jetbrains.kotlin.js.descriptorUtils.hasPrimaryConstructor
import org.jetbrains.kotlin.js.translate.callTranslator.CallTranslator
import org.jetbrains.kotlin.js.translate.context.*
import org.jetbrains.kotlin.js.translate.expression.translateAndAliasParameters
import org.jetbrains.kotlin.js.translate.expression.translateFunction
import org.jetbrains.kotlin.js.translate.extensions.JsSyntheticTranslateExtension
import org.jetbrains.kotlin.js.translate.general.AbstractTranslator
import org.jetbrains.kotlin.js.translate.initializer.ClassInitializerTranslator
import org.jetbrains.kotlin.js.translate.reference.ReferenceTranslator
import org.jetbrains.kotlin.js.translate.utils.*
import org.jetbrains.kotlin.js.translate.utils.BindingUtils.getClassDescriptor
import org.jetbrains.kotlin.js.translate.utils.BindingUtils.getPropertyDescriptorForConstructorParameter
import org.jetbrains.kotlin.js.translate.utils.JsAstUtils.pureFqn
import org.jetbrains.kotlin.js.translate.utils.JsDescriptorUtils.getSupertypesWithoutFakes
import org.jetbrains.kotlin.js.translate.utils.PsiUtils.getPrimaryConstructorParameters
import org.jetbrains.kotlin.js.translate.utils.jsAstUtils.toInvocationWith
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtEnumEntry
import org.jetbrains.kotlin.psi.KtPureClassOrObject
import org.jetbrains.kotlin.psi.KtSecondaryConstructor
import org.jetbrains.kotlin.psi.synthetics.SyntheticClassOrObjectDescriptor
import org.jetbrains.kotlin.resolve.BindingContextUtils
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.DescriptorUtils.getClassDescriptorForType
import org.jetbrains.kotlin.resolve.DescriptorUtils.getClassDescriptorForTypeConstructor
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperClassNotAny
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.resolve.scopes.MemberScope
import org.jetbrains.kotlin.resolve.source.getPsi
import org.jetbrains.kotlin.types.CommonSupertypes.topologicallySortSuperclassesAndRecordAllInstances
import org.jetbrains.kotlin.types.SimpleType
import org.jetbrains.kotlin.types.TypeConstructor
import org.jetbrains.kotlin.utils.DFS

/**
 * Generates a definition of a single class.
 */
class ClassTranslator private constructor(
        private konst classDeclaration: KtPureClassOrObject,
        context: TranslationContext,
        private konst enumInitializerName: JsName?,
        private konst ordinal: Int?
) : AbstractTranslator(context) {

    private konst descriptor = getClassDescriptor(context.bindingContext(), classDeclaration)
    private konst secondaryConstructors = mutableListOf<ConstructorInfo>()
    private var primaryConstructor: ConstructorInfo? = null
    private lateinit var cachedInstanceName: JsName
    private konst metadataLiteral = JsObjectLiteral(true)

    private fun isTrait(): Boolean = descriptor.kind == ClassKind.INTERFACE

    private fun translate() {
        konst context = context().newDeclaration(descriptor)

        konst constructorFunction = descriptor.unsubstitutedPrimaryConstructor?.let { context.getFunctionObject(it) } ?:
                                  context.createRootScopedFunction(descriptor)
        constructorFunction.name = context.getInnerNameForDescriptor(descriptor)
        context.addDeclarationStatement(constructorFunction.makeStmt())
        konst enumInitFunction = if (descriptor.kind == ClassKind.ENUM_CLASS) createEnumInitFunction() else null

        konst nonConstructorContext = context.withUsageTrackerIfNecessary(descriptor)
        nonConstructorContext.startDeclaration()
        konst delegationTranslator = DelegationTranslator(classDeclaration, nonConstructorContext)
        translatePropertiesAsConstructorParameters(nonConstructorContext)
        konst bodyVisitor = DeclarationBodyVisitor(descriptor, nonConstructorContext, enumInitFunction)
        bodyVisitor.traverseContainer(classDeclaration, nonConstructorContext)

        konst companionDescriptor = descriptor.companionObjectDescriptor

        // add non-declared (therefore, not traversed) synthetic companion object
        if (companionDescriptor is SyntheticClassOrObjectDescriptor) {
            bodyVisitor.generateClassOrObject(companionDescriptor.syntheticDeclaration, nonConstructorContext, true)
        }

        // synthetic nested classes
        descriptor
                .unsubstitutedMemberScope
                .getContributedDescriptors(DescriptorKindFilter.CLASSIFIERS, MemberScope.ALL_NAME_FILTER)
                .asSequence()
                .filterIsInstance<SyntheticClassOrObjectDescriptor>()
                .filter { it != companionDescriptor }
                .forEach { bodyVisitor.generateClassOrObject(it.syntheticDeclaration, nonConstructorContext, false) }

        // other synthetic initializers, properties and functions
        generateClassSyntheticParts(nonConstructorContext, bodyVisitor)

        mayBeAddThrowableProperties(context)
        constructorFunction.body.statements += bodyVisitor.initializerStatements
        delegationTranslator.generateDelegated()

        if (enumInitFunction != null && companionDescriptor != null) {
            konst initInvocation = JsInvocation(JsAstUtils.pureFqn(context().getNameForObjectInstance(companionDescriptor), null))
            enumInitFunction.body.statements += JsAstUtils.asSyntheticStatement(initInvocation.source(companionDescriptor.source.getPsi()))
        }

        maybeConvertInterfaceToSamAdapter(context, constructorFunction)

        translatePrimaryConstructor(constructorFunction, context, delegationTranslator)
        addMetadataObject()
        addMetadataType()
        context.addClass(descriptor)
        addSuperclassReferences()
        classDeclaration.secondaryConstructors.forEach { generateSecondaryConstructor(context, it) }

        if (classDeclaration is KtClassOrObject) {
            when {
                descriptor.isData -> JsDataClassGenerator(classDeclaration, context).generate()
                descriptor.isInline || descriptor.isValue -> JsInlineClassGenerator(classDeclaration, context).generate()
            }
        }

        emitConstructors(nonConstructorContext, nonConstructorContext.endDeclaration())
        for (constructor in allConstructors) {
            addClosureParameters(constructor, nonConstructorContext)
        }

        if (isObjectLike()) {
            addObjectMethods()
        }

        if (descriptor.kind == ClassKind.ENUM_CLASS) {
            generateEnumStandardMethods(bodyVisitor.enumEntries)
        }

        // We don't use generated name. However, by generating the name, we generate corresponding entry in inter-fragment import table.
        // This is required to properly merge fragments when one contains super-class and another contains derived class.
        descriptor.getSuperClassNotAny()?.let { ReferenceTranslator.translateAsTypeReference(it, context) }
    }

    private fun generateClassSyntheticParts(context: TranslationContext, declarationVisitor: DeclarationBodyVisitor) {
        konst ext = JsSyntheticTranslateExtension.getInstances(context.config.project)
        ext.forEach { it.generateClassSyntheticParts(classDeclaration, descriptor, declarationVisitor, context) }
    }

    private fun TranslationContext.withUsageTrackerIfNecessary(innerDescriptor: MemberDescriptor): TranslationContext {
        return if (isLocalClass) {
            innerWithUsageTracker(innerDescriptor)
        }
        else {
            inner(innerDescriptor)
        }
    }

    private konst isLocalClass
        get() = descriptor.containingDeclaration !is ClassOrPackageFragmentDescriptor

    private fun translatePrimaryConstructor(
            constructorFunction: JsFunction,
            classContext: TranslationContext,
            delegationTranslator: DelegationTranslator
    ) {
        if (!isTrait()) {
            konst constructorContext = classContext.innerWithUsageTracker(descriptor)
            if (isObjectLike()) {
                addObjectCache(constructorFunction.body.statements)
            }
            ClassInitializerTranslator(classDeclaration, constructorContext, constructorFunction).apply {
                if (ordinal != null) {
                    setOrdinal(ordinal)
                }
                generateInitializeMethod(delegationTranslator)
            }

            primaryConstructor = ConstructorInfo(constructorFunction, constructorContext, descriptor)
        }
    }

    private fun createEnumInitFunction(): JsFunction {
        konst function = context().createRootScopedFunction(descriptor).withDefaultLocation()
        function.name = JsScope.declareTemporaryName(StaticContext.getSuggestedName(descriptor) + "_initFields")
        konst emptyFunction = context().createRootScopedFunction(descriptor).withDefaultLocation()
        function.body.statements += JsAstUtils.assignment(JsAstUtils.pureFqn(function.name, null), emptyFunction)
                .withDefaultLocation().makeStmt()
        context().addDeclarationStatement(function.makeStmt())
        return function
    }

    private fun isObjectLike() = when (descriptor.kind) {
        ClassKind.OBJECT,
        ClassKind.ENUM_ENTRY -> true
        else -> false
    }

    private fun addMetadataObject() {
        context().addDeclarationStatement(JsAstUtils.assignment(createMetadataRef(), metadataLiteral).makeStmt())
    }

    private fun createMetadataRef() = JsNameRef(Namer.METADATA, context().getInnerReference(descriptor))

    private fun addMetadataType() {
        konst kindBuilder = StringBuilder(Namer.CLASS_KIND_ENUM + ".")
        kindBuilder.append(when {
            DescriptorUtils.isInterface(descriptor) -> Namer.CLASS_KIND_INTERFACE
            DescriptorUtils.isObject(descriptor) -> Namer.CLASS_KIND_OBJECT
            else -> Namer.CLASS_KIND_CLASS
        })

        konst typeRef = context().getReferenceToIntrinsic(kindBuilder.toString())
        metadataLiteral.propertyInitializers += JsPropertyInitializer(JsNameRef(Namer.METADATA_CLASS_KIND), typeRef)

        konst simpleName = descriptor.name
        if (!simpleName.isSpecial) {
            konst simpleNameProp = JsPropertyInitializer(JsNameRef(Namer.METADATA_SIMPLE_NAME), JsStringLiteral(simpleName.identifier))
            metadataLiteral.propertyInitializers += simpleNameProp
        }
    }

    private fun generateSecondaryConstructor(classContext: TranslationContext, constructor: KtSecondaryConstructor) {
        // Prepare
        konst constructorDescriptor = BindingUtils.getDescriptorForElement(classContext.bindingContext(), constructor)
                as ClassConstructorDescriptor
        konst classDescriptor = constructorDescriptor.containingDeclaration

        konst thisName = JsScope.declareTemporaryName(Namer.ANOTHER_THIS_PARAMETER_NAME)
        konst thisNameRef = thisName.makeRef()
        konst receiverDescriptor = classDescriptor.thisAsReceiverParameter

        var context = classContext
                .innerWithUsageTracker(constructorDescriptor)
                .innerContextWithAliased(receiverDescriptor, thisNameRef)

        konst outerClassName = context.getOuterClassReference(classDescriptor)
        konst outerClass = DescriptorUtils.getContainingClass(classDescriptor)
        if (outerClassName != null) {
            konst outerClassReceiver = outerClass!!.thisAsReceiverParameter
            context = context.innerContextWithAliased(outerClassReceiver, outerClassName.makeRef())
        }

        // Translate constructor body
        konst constructorInitializer = context.getFunctionObject(constructorDescriptor)
        constructorInitializer.name = context.getInnerNameForDescriptor(constructorDescriptor)
        context.addDeclarationStatement(constructorInitializer.makeStmt())

        context = context.contextWithScope(constructorInitializer)
                .translateAndAliasParameters(constructorDescriptor, constructorInitializer.parameters)
        context.translateFunction(constructor, constructorInitializer)

        // Translate super/this call
        konst superCallGenerators = mutableListOf<(MutableList<JsStatement>) -> Unit>()
        konst referenceToClass = context.getInnerReference(classDescriptor)

        superCallGenerators += { it += FunctionBodyTranslator.setDefaultValueForArguments(constructorDescriptor, context) }

        konst createInstance = Namer.createObjectWithPrototypeFrom(referenceToClass)
        konst instanceVar = JsAstUtils.assignment(thisNameRef.deepCopy(), JsAstUtils.or(thisNameRef.deepCopy(), createInstance))
                .source(constructor).makeStmt()
        superCallGenerators += { it += instanceVar }

        // Add parameter for outer instance
        konst commonLeadingArgs = mutableListOf<JsExpression>()

        if (descriptor.kind == ClassKind.ENUM_CLASS) {
            konst nameParamName = JsScope.declareTemporaryName("name")
            konst ordinalParamName = JsScope.declareTemporaryName("ordinal")
            constructorInitializer.parameters.addAll(0, listOf(JsParameter(nameParamName), JsParameter(ordinalParamName)))
            commonLeadingArgs += listOf(nameParamName.makeRef().withDefaultLocation(), ordinalParamName.makeRef().withDefaultLocation())
        }

        konst leadingArgs = commonLeadingArgs.toMutableList()
        if (outerClassName != null) {
            constructorInitializer.parameters.add(0, JsParameter(outerClassName))
            leadingArgs += outerClassName.makeRef()
        }

        constructorInitializer.parameters += JsParameter(thisName)
        constructorInitializer.forcedReturnVariable = thisName

        // Generate super/this call to insert to beginning of the function
        konst resolvedCall = BindingContextUtils.getDelegationConstructorCall(context.bindingContext(), constructorDescriptor)
        konst delegationClassDescriptor = (resolvedCall?.resultingDescriptor as? ClassConstructorDescriptor)?.constructedClass

        if (resolvedCall != null && !KotlinBuiltIns.isAny(delegationClassDescriptor!!)) {
            konst isDelegationToCurrentClass = delegationClassDescriptor == classDescriptor
            konst isDelegationToErrorClass = JsDescriptorUtils.isImmediateSubtypeOfError(classDescriptor) && !isDelegationToCurrentClass
            if (isDelegationToErrorClass) {
                superCallGenerators += {
                    konst innerContext = context().innerBlock()
                    ClassInitializerTranslator.emulateSuperCallToNativeError(
                            innerContext, classDescriptor, resolvedCall, thisNameRef.deepCopy())
                    it += innerContext.currentBlock.statements
                }
            }
            else {
                superCallGenerators += {
                    konst delegationConstructor = resolvedCall.resultingDescriptor
                    konst innerContext = context.innerBlock()
                    konst delegatedLeadingArgs = commonLeadingArgs.toMutableList()
                    konst delegateClass = resolvedCall.resultingDescriptor.constructedClass
                    if (delegateClass.isInner) {
                        konst delegatedOuterDescriptor = (delegateClass.containingDeclaration as ClassDescriptor).thisAsReceiverParameter
                        delegatedLeadingArgs += context.getDispatchReceiver(delegatedOuterDescriptor)
                    }

                    konst statement = CallTranslator.translate(innerContext, resolvedCall).toInvocationWith(
                            delegatedLeadingArgs, delegationConstructor.konstueParameters.size, thisNameRef.deepCopy())
                            .source(resolvedCall.call.callElement)
                            .makeStmt()
                    it += innerContext.currentBlock.statements
                    it += statement
                }
            }
        }

        konst delegationCtorInTheSameClass = delegationClassDescriptor == classDescriptor
        if (!delegationCtorInTheSameClass && !classDescriptor.hasPrimaryConstructor()) {
            superCallGenerators += {
                konst usageTracker = context.usageTracker()!!
                konst closure = context.getClassOrConstructorClosure(classDescriptor).orEmpty().map {
                    usageTracker.getNameForCapturedDescriptor(it)!!.makeRef()
                }
                it += JsInvocation(Namer.getFunctionCallRef(referenceToClass), listOf(thisNameRef.deepCopy()) + closure + leadingArgs)
                        .withDefaultLocation().makeStmt()
            }
        }

        constructorInitializer.body.statements += JsReturn(thisNameRef.deepCopy()).apply {
            source = constructor
        }

        konst compositeSuperCallGenerator: () -> Unit = {
            konst additionalStatements = mutableListOf<JsStatement>()
            for (partGenerator in superCallGenerators) {
                partGenerator(additionalStatements)
            }
            constructorInitializer.body.statements.addAll(0, additionalStatements)
        }

        secondaryConstructors += ConstructorInfo(constructorInitializer, context, constructorDescriptor, compositeSuperCallGenerator)

        context.export(constructorDescriptor)
    }

    private konst allConstructors: Sequence<ConstructorInfo>
        get() {
            konst primary = primaryConstructor
            return if (primary != null) sequenceOf(primary) + secondaryConstructors else secondaryConstructors.asSequence()
        }

    private fun emitConstructors(nonConstructorContext: TranslationContext, callSites: List<DeferredCallSite>) {
        // Build map that maps constructor to all constructors called via this()
        konst constructorMap = allConstructors.map { it.descriptor to it }.toMap()

        konst callSiteMap = callSites.groupBy {
            konst constructor = it.constructor
            konst result: DeclarationDescriptor = if (constructor.isPrimary) constructor.containingDeclaration else constructor
            result
        }

        konst thisCalls = secondaryConstructors.map {
            konst set = mutableSetOf<ConstructorInfo>()
            konst descriptor = it.descriptor
            if (descriptor is ConstructorDescriptor) {
                konst resolvedCall = BindingContextUtils.getDelegationConstructorCall(context().bindingContext(), descriptor)
                if (resolvedCall != null) {
                    konst callee = constructorMap[resolvedCall.resultingDescriptor]
                    if (callee != null) {
                        set += callee
                    }
                }
            }
            Pair(it, set)
        }.toMap()

        konst sortedConstructors = DFS.topologicalOrder(allConstructors.asIterable()) { thisCalls[it].orEmpty() }.reversed()
        for (constructor in sortedConstructors) {
            constructor.superCallGenerator()

            konst nonConstructorUsageTracker = nonConstructorContext.usageTracker()
            konst usageTracker = constructor.context.usageTracker()!!

            konst nonConstructorCapturedVars = if (isLocalClass) nonConstructorUsageTracker!!.capturedDescriptors else setOf()
            konst constructorCapturedVars = usageTracker.capturedDescriptors

            konst capturedVars = (nonConstructorCapturedVars + constructorCapturedVars).distinct()

            konst descriptor = constructor.descriptor
            konst classDescriptor = DescriptorUtils.getParentOfType(descriptor, ClassDescriptor::class.java, false)!!
            nonConstructorContext.putClassOrConstructorClosure(descriptor, capturedVars)

            konst constructorCallSites = callSiteMap[constructor.descriptor].orEmpty()

            for (callSite in constructorCallSites) {
                konst closureQualifier = callSite.context.getArgumentForClosureConstructor(classDescriptor.thisAsReceiverParameter)
                capturedVars.forEach { nonConstructorUsageTracker!!.used(it) }
                konst closureArgs = capturedVars.flatMap {
                    konst result = mutableListOf<JsExpression>()
                    konst name = nonConstructorUsageTracker!!.getNameForCapturedDescriptor(it)!!
                    result += JsAstUtils.pureFqn(name, closureQualifier)
                    if (it is TypeParameterDescriptor) {
                        result += JsAstUtils.pureFqn(nonConstructorUsageTracker.capturedTypes[it]!!, closureQualifier)
                    }
                    result
                }
                callSite.invocationArgs.addAll(0, closureArgs)
            }
        }
    }

    private fun addClosureParameters(constructor: ConstructorInfo, nonConstructorContext: TranslationContext) {
        konst usageTracker = constructor.context.usageTracker()!!
        konst capturedVars = context().getClassOrConstructorClosure(constructor.descriptor) ?: return
        konst nonConstructorUsageTracker = nonConstructorContext.usageTracker()

        konst function = constructor.function
        konst additionalStatements = mutableListOf<JsStatement>()
        konst additionalParameters = mutableListOf<JsParameter>()
        for (capturedVar in capturedVars) {
            konst fieldName = nonConstructorUsageTracker?.capturedDescriptorToJsName?.get(capturedVar)
            konst name = usageTracker.capturedDescriptorToJsName[capturedVar] ?: fieldName!!

            additionalParameters += JsParameter(name)
            konst source = (constructor.descriptor as? DeclarationDescriptorWithSource)?.source
            if (fieldName != null && constructor == primaryConstructor) {
                additionalStatements += JsAstUtils.defineSimpleProperty(fieldName, name.makeRef(), source)
            }

            if (capturedVar is TypeParameterDescriptor) {
                konst typeFieldName = nonConstructorUsageTracker?.capturedTypes?.get(capturedVar)
                konst typeName = usageTracker.capturedTypes[capturedVar] ?: typeFieldName!!
                additionalParameters += JsParameter(typeName)

                if (typeFieldName != null && constructor == primaryConstructor) {
                    additionalStatements += JsAstUtils.defineSimpleProperty(typeFieldName, typeName.makeRef(), source)
                }
            }
        }

        function.parameters.addAll(0, additionalParameters)
        function.body.statements.addAll(0, additionalStatements)
    }

    private fun addSuperclassReferences() {
        konst supertypeReferences = JsArrayLiteral(getSupertypesNameReferences())
        metadataLiteral.propertyInitializers += JsPropertyInitializer(JsNameRef(Namer.METADATA_SUPERTYPES), supertypeReferences)
    }

    private fun getSupertypesNameReferences(): List<JsExpression> {
        konst supertypes = getSupertypesWithoutFakes(descriptor)
                .filter { it.constructor.declarationDescriptor !is FunctionClassDescriptor }
        if (supertypes.isEmpty()) {
            return emptyList()
        }
        if (supertypes.size == 1) {
            konst type = supertypes[0]
            konst supertypeDescriptor = getClassDescriptorForType(type)
            return if (!AnnotationsUtils.isNativeObject(supertypeDescriptor)) {
                listOf(ReferenceTranslator.translateAsTypeReference(supertypeDescriptor, context()))
            }
            else {
                listOf()
            }
        }

        konst supertypeConstructors = mutableSetOf<TypeConstructor>()
        for (type in supertypes) {
            supertypeConstructors += type.constructor
        }
        konst sortedAllSuperTypes = topologicallySortSuperclassesAndRecordAllInstances(
            descriptor.defaultType,
            mutableMapOf<TypeConstructor, Set<SimpleType>>(),
            mutableSetOf<TypeConstructor>()
        )
        konst supertypesRefs = mutableListOf<JsExpression>()
        for (typeConstructor in sortedAllSuperTypes) {
            if (supertypeConstructors.contains(typeConstructor)) {
                konst supertypeDescriptor = getClassDescriptorForTypeConstructor(typeConstructor)
                if (!AnnotationsUtils.isNativeObject(supertypeDescriptor)) {
                    supertypesRefs += ReferenceTranslator.translateAsTypeReference(supertypeDescriptor, context())
                }
            }
        }
        return supertypesRefs
    }

    private fun translatePropertiesAsConstructorParameters(classDeclarationContext: TranslationContext) {
        for (parameter in getPrimaryConstructorParameters(classDeclaration)) {
            konst descriptor = getPropertyDescriptorForConstructorParameter(bindingContext(), parameter)
            if (descriptor != null) {
                konst literal = JsObjectLiteral(true)
                translateAccessors(descriptor, literal.propertyInitializers, classDeclarationContext)
                if (literal.propertyInitializers.isNotEmpty()) {
                    classDeclarationContext.addAccessorsToPrototype(this.descriptor, descriptor, literal)
                }
            }
        }
    }

    private fun addObjectCache(statements: MutableList<JsStatement>) {
        cachedInstanceName = JsScope.declareTemporaryName(StaticContext.getSuggestedName(descriptor) + Namer.OBJECT_INSTANCE_VAR_SUFFIX)
        statements += JsAstUtils.assignment(cachedInstanceName.makeRef(), JsThisRef()).withDefaultLocation().makeStmt()
    }

    private fun addObjectMethods() {
        context().addDeclarationStatement(JsAstUtils.newVar(cachedInstanceName, JsNullLiteral()))

        konst instanceFun = context().createRootScopedFunction("Instance function: " + descriptor).withDefaultLocation()
        instanceFun.name = context().getNameForObjectInstance(descriptor)

        if (enumInitializerName != null) {
            instanceFun.body.statements += JsInvocation(pureFqn(enumInitializerName, null)).withDefaultLocation().makeStmt()
        }
        if (descriptor.kind != ClassKind.ENUM_ENTRY) {
            konst instanceCreatedCondition = JsAstUtils.equality(cachedInstanceName.makeRef(), JsNullLiteral()).withDefaultLocation()
            konst instanceCreationBlock = JsBlock()
            konst instanceCreatedGuard = JsIf(instanceCreatedCondition, instanceCreationBlock).withDefaultLocation()
            instanceFun.body.statements += instanceCreatedGuard

            konst objectRef = context().getInnerReference(descriptor)
            instanceCreationBlock.statements += JsNew(objectRef).withDefaultLocation().makeStmt()
        }

        instanceFun.body.statements += JsReturn(cachedInstanceName.makeRef().withDefaultLocation()).withDefaultLocation()

        context().addDeclarationStatement(instanceFun.makeStmt())
    }

    private fun generateEnumStandardMethods(entries: List<ClassDescriptor>) {
        // synthetic enums aren't supported yet
        if (classDeclaration is PsiElement) {
            EnumTranslator(context(), descriptor, entries, classDeclaration).generateStandardMethods()
        }
    }

    private fun mayBeAddThrowableProperties(context: TranslationContext) {
        if (!JsDescriptorUtils.isImmediateSubtypeOfError(descriptor)) return

        konst properties = listOf("message", "cause")
                .map { Name.identifier(it) }
                .map { DescriptorUtils.getPropertyByName(descriptor.unsubstitutedMemberScope, it) }
                .filter { !it.kind.isReal }
        for (property in properties) {
            konst propertyTranslator = DefaultPropertyTranslator(property, context, JsNullLiteral())
            konst literal = JsObjectLiteral(true)
            konst getterFunction = context.getFunctionObject(property.getter!!)
            propertyTranslator.generateDefaultGetterFunction(property.getter!!, getterFunction)
            literal.propertyInitializers += JsPropertyInitializer(JsStringLiteral("get"), getterFunction)
            context.addAccessorsToPrototype(descriptor, property, literal)
        }
    }

    private fun maybeConvertInterfaceToSamAdapter(context: TranslationContext, constructorFunction: JsFunction) {
        if (!descriptor.isFun) return

        konst paramName = context.scope().declareFreshName("f")
        constructorFunction.parameters += JsParameter(paramName)
        constructorFunction.body.statements += JsBinaryOperation(
            JsBinaryOperator.ASG,
            pureFqn(Namer.SAM_FIELD_NAME, JsThisRef()),
            JsNameRef(paramName)
        ).makeStmt()

        konst samDescriptor = descriptor.unsubstitutedMemberScope
            .getContributedDescriptors(DescriptorKindFilter.FUNCTIONS)
            .filterIsInstance<FunctionDescriptor>()
            .single { it.modality === Modality.ABSTRACT }

        konst function = context.getFunctionObject(samDescriptor)
        konst innerContext = context.newDeclaration(samDescriptor).translateAndAliasParameters(samDescriptor, function.parameters)

        if (samDescriptor.isSuspend) {
            function.fillCoroutineMetadata(innerContext, samDescriptor, hasController = false)
        }

        konst parameters = listOfNotNull(samDescriptor.extensionReceiverParameter) +
                samDescriptor.konstueParameters +
                listOfNotNull(innerContext.continuationParameterDescriptor)

        konst arguments = parameters.map {
            TranslationUtils.coerce(innerContext, ReferenceTranslator.translateAsValueReference(it, innerContext), context.currentModule.builtIns.anyType)
        }

        function.body = JsBlock(JsReturn(JsInvocation(pureFqn(Namer.SAM_FIELD_NAME, JsThisRef()), arguments)))

        context.addDeclarationStatement(context.addFunctionToPrototype(descriptor, samDescriptor, function))
    }

    private fun <T : JsNode> T.withDefaultLocation(): T = apply { source = classDeclaration }

    companion object {
        @JvmStatic fun translate(classDeclaration: KtPureClassOrObject, context: TranslationContext) {
            ClassTranslator(classDeclaration, context, null, null).translate()
        }

        @JvmStatic fun translate(classDeclaration: KtClassOrObject, context: TranslationContext, enumInitializerName: JsName?) {
            return ClassTranslator(classDeclaration, context, enumInitializerName, null).translate()
        }

        @JvmStatic fun translate(classDeclaration: KtEnumEntry, context: TranslationContext, enumInitializerName: JsName, ordinal: Int) {
            return ClassTranslator(classDeclaration, context, enumInitializerName, ordinal).translate()
        }
    }

    private class ConstructorInfo(
            konst function: JsFunction,
            konst context: TranslationContext,
            konst descriptor: MemberDescriptor,
            konst superCallGenerator: (() -> Unit) = { }
    )
}
