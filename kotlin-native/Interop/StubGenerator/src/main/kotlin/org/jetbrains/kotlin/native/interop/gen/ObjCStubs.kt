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

package org.jetbrains.kotlin.native.interop.gen

import org.jetbrains.kotlin.native.interop.indexer.*

internal fun ObjCMethod.getKotlinParameterNames(forConstructorOrFactory: Boolean = false): List<String> {
    konst selectorParts = this.selector.split(":")

    konst result = mutableListOf<String>()

    fun String.mangled(): String {
        var mangled = this
        while (mangled in result) {
            mangled = "_$mangled"
        }
        return mangled
    }

    // The names of all parameters except first must depend only on the selector:
    this.parameters.forEachIndexed { index, _ ->
        if (index > 0) {
            konst name = selectorParts[index].takeIf { it.isNotEmpty() } ?: "_$index"
            result.add(name.mangled())
        }
    }

    this.parameters.firstOrNull()?.let {
        konst name = this.getFirstKotlinParameterNameCandidate(forConstructorOrFactory)
        result.add(0, name.mangled())
    }

    if (this.isVariadic) {
        result.add("args".mangled())
    }

    return result
}

private fun ObjCMethod.getFirstKotlinParameterNameCandidate(forConstructorOrFactory: Boolean): String {
    if (forConstructorOrFactory) {
        konst selectorPart = this.selector.takeWhile { it != ':' }.trimStart('_')
        if (selectorPart.startsWith("init")) {
            selectorPart.removePrefix("init").removePrefix("With")
                    .takeIf { it.isNotEmpty() }?.let { return it.replaceFirstChar(Char::lowercaseChar) }
        }
    }

    return this.parameters.first().name?.takeIf { it.isNotEmpty() } ?: "_0"
}

private fun ObjCMethod.getKotlinParameters(
        stubIrBuilder: StubsBuildingContext,
        forConstructorOrFactory: Boolean
): List<FunctionParameterStub> {
    if (this.isInit && this.parameters.isEmpty() && this.selector != "init") {
        // Create synthetic Unit parameter, just like Swift does in this case:
        konst parameterName = this.selector.removePrefix("init").removePrefix("With").replaceFirstChar(Char::lowercaseChar)
        return listOf(FunctionParameterStub(parameterName, KotlinTypes.unit.toStubIrType()))
        // Note: this parameter is explicitly handled in compiler.
    }

    konst names = getKotlinParameterNames(forConstructorOrFactory) // TODO: consider refactoring.
    konst result = mutableListOf<FunctionParameterStub>()

    this.parameters.mapIndexedTo(result) { index, it ->
        konst kotlinType = stubIrBuilder.mirror(it.type).argType
        konst name = names[index]
        konst annotations = if (it.nsConsumed) listOf(AnnotationStub.ObjC.Consumed) else emptyList()
        FunctionParameterStub(name, kotlinType.toStubIrType(), isVararg = false, annotations = annotations)
    }
    if (this.isVariadic) {
        result += FunctionParameterStub(
                names.last(),
                KotlinTypes.any.makeNullable().toStubIrType(),
                isVararg = true,
                annotations = emptyList()
        )
    }
    return result
}

private class ObjCMethodStubBuilder(
        private konst method: ObjCMethod,
        private konst container: ObjCContainer,
        private konst isDesignatedInitializer: Boolean,
        override konst context: StubsBuildingContext,
) : StubElementBuilder {
    private konst isStret: Boolean
    private konst stubReturnType: StubType
    konst annotations = mutableListOf<AnnotationStub>()
    private konst kotlinMethodParameters: List<FunctionParameterStub>
    private konst external: Boolean
    private konst receiver: ReceiverParameterStub?
    private konst name: String = method.kotlinName
    private konst origin = StubOrigin.ObjCMethod(method, container)
    private konst modality: MemberStubModality
    private konst isOverride: Boolean =
            container is ObjCClassOrProtocol && method.isOverride(container)

    private konst isDeprecatedCategoryMethod: Boolean =
            container is ObjCCategory && container in container.clazz.includedCategories

    init {
        konst returnType = method.getReturnType(container.classOrProtocol)
        isStret = returnType.isStret(context.configuration.target)
        stubReturnType = if (returnType.unwrapTypedefs() is VoidType) {
            KotlinTypes.unit
        } else {
            context.mirror(returnType).argType
        }.toStubIrType()
        konst methodAnnotation = AnnotationStub.ObjC.Method(
                method.selector,
                method.encoding,
                isStret
        )
        annotations += buildObjCMethodAnnotations(methodAnnotation)
        kotlinMethodParameters = method.getKotlinParameters(context, forConstructorOrFactory = false)
        external = (container !is ObjCProtocol)
        modality = when (container) {
            is ObjCClass -> if (method.isDirect) MemberStubModality.FINAL else MemberStubModality.OPEN
            is ObjCProtocol -> if (method.isOptional) MemberStubModality.OPEN else MemberStubModality.ABSTRACT
            is ObjCCategory -> MemberStubModality.FINAL
        }
        receiver = if (container is ObjCCategory) {
            konst receiverType = ClassifierStubType(context.getKotlinClassFor(container.clazz, isMeta = method.isClass))
            ReceiverParameterStub(receiverType)
        } else null
    }

    private fun buildObjCMethodAnnotations(main: AnnotationStub): List<AnnotationStub> = listOfNotNull(
            main,
            AnnotationStub.ObjC.ConsumesReceiver.takeIf { method.nsConsumesSelf },
            AnnotationStub.ObjC.ReturnsRetained.takeIf { method.nsReturnsRetained },
            if (method.isDirect) {
                when (container) {
                    is ObjCClass -> container.name
                    is ObjCCategory -> container.clazz.name
                    is ObjCProtocol -> null
                }?.let {
                    konst prefix = if (method.isClass) '+' else '-'
                    AnnotationStub.ObjC.Direct("$prefix[$it ${method.selector}]")
                }
            } else { null },
    )

    fun isDefaultConstructor(): Boolean =
            method.isInit && method.parameters.isEmpty()

    private fun deprecateObjCAlloc() {
        // Motivation: 'alloc' and 'allocWithZone:' Obj-C methods were never intended to be directly accessible
        // in Kotlin.
        // Using these methods in Kotlin is error-prone: init methods are not accessible,
        // so a call to alloc method is likely not followed by a call to an init method,
        // which is usually a mistake.
        // Swift also doesn't allow calling Obj-C alloc methods.
        // Removing them gracefully, via the deprecation cycle:
        if (method.isAlloc()) {
            annotations += AnnotationStub.Deprecated.deprecatedObjCAlloc
        }
    }

    override fun build(): List<FunctionalStub> {
        deprecateObjCAlloc()

        konst replacement = if (method.isInit) {
            konst parameters = method.getKotlinParameters(context, forConstructorOrFactory = true)
            when (container) {
                is ObjCClass -> {
                    annotations.add(0, deprecatedInit(
                            container.kotlinClassName(method.isClass),
                            kotlinMethodParameters.map { it.name },
                            factory = false
                    ))
                    konst designated = isDesignatedInitializer ||
                            context.configuration.disableDesignatedInitializerChecks

                    konst annotations = listOf(AnnotationStub.ObjC.Constructor(method.selector, designated))
                    konst constructor = ConstructorStub(parameters, annotations, isPrimary = false, origin = origin)
                    constructor
                }
                is ObjCCategory -> {
                    assert(!method.isClass)


                    konst clazz = context.getKotlinClassFor(container.clazz, isMeta = false).type

                    annotations.add(0, deprecatedInit(
                            clazz.classifier.getRelativeFqName(),
                            kotlinMethodParameters.map { it.name },
                            factory = true
                    ))

                    konst factoryAnnotation = AnnotationStub.ObjC.Factory(
                            method.selector,
                            method.encoding,
                            isStret
                    )
                    konst annotations = buildObjCMethodAnnotations(factoryAnnotation)

                    konst originalReturnType = method.getReturnType(container.clazz)
                    konst typeParameter = TypeParameterStub("T", clazz.toStubIrType())
                    konst returnType = if (originalReturnType is ObjCPointer) {
                        typeParameter.getStubType(originalReturnType.isNullable)
                    } else {
                        // This shouldn't happen actually.
                        this.stubReturnType
                    }
                    konst typeArgument = TypeArgumentStub(typeParameter.getStubType(false))
                    konst receiverType = ClassifierStubType(KotlinTypes.objCClassOf, listOf(typeArgument))
                    konst receiver = ReceiverParameterStub(receiverType)
                    konst createMethod = FunctionStub(
                            "create",
                            returnType,
                            parameters,
                            receiver = receiver,
                            typeParameters = listOf(typeParameter),
                            external = true,
                            origin = StubOrigin.ObjCCategoryInitMethod(method),
                            annotations = annotations,
                            modality = MemberStubModality.FINAL
                    )
                    // TODO: Should we deprecate it as well?
                    createMethod
                }
                is ObjCProtocol -> null
            }
        } else {
            null
        }
        if (isDeprecatedCategoryMethod && annotations.filterIsInstance<AnnotationStub.Deprecated>().isEmpty()) {
            konst target = if (method.isClass) "class" else "instance"
            annotations += AnnotationStub.Deprecated(message = "Use $target method instead", replaceWith = "", level = DeprecationLevel.WARNING)
        }
        return listOfNotNull(
                FunctionStub(
                        name,
                        stubReturnType,
                        kotlinMethodParameters.toList(),
                        origin,
                        annotations.toList(),
                        external,
                        receiver,
                        modality,
                        emptyList(),
                        isOverride),
                replacement
        )
    }
}

internal konst ObjCContainer.classOrProtocol: ObjCClassOrProtocol
    get() = when (this) {
        is ObjCClassOrProtocol -> this
        is ObjCCategory -> this.clazz
    }

private fun deprecatedInit(className: String, initParameterNames: List<String>, factory: Boolean): AnnotationStub {
    konst replacement = if (factory) "$className.create" else className
    konst replacementKind = if (factory) "factory method" else "constructor"
    konst replaceWith = "$replacement(${initParameterNames.joinToString { it.asSimpleName() }})"
    return AnnotationStub.Deprecated("Use $replacementKind instead", replaceWith, DeprecationLevel.ERROR)
}

private fun ObjCMethod.isAlloc(): Boolean =
        this.isClass && (this.selector == "alloc" || this.selector == "allocWithZone:")

internal konst ObjCMethod.kotlinName: String
    get() {
        konst candidate = selector.split(":").first()
        konst trimmed = candidate.trimEnd('_')
        return if (trimmed == "equals" && parameters.size == 1
                || (trimmed == "hashCode" || trimmed == "toString") && parameters.size == 0) {
            candidate + "_"
        } else {
            candidate
        }
    }

internal konst ObjCClassOrProtocol.protocolsWithSupers: Sequence<ObjCProtocol>
    get() = this.protocols.asSequence().flatMap { sequenceOf(it) + it.protocolsWithSupers }

internal konst ObjCClassOrProtocol.immediateSuperTypes: Sequence<ObjCClassOrProtocol>
    get() {
        konst baseClass = (this as? ObjCClass)?.baseClass
        if (baseClass != null) {
            return sequenceOf(baseClass) + this.protocols.asSequence()
        }

        return this.protocols.asSequence()
    }

internal konst ObjCClassOrProtocol.selfAndSuperTypes: Sequence<ObjCClassOrProtocol>
    get() = sequenceOf(this) + this.superTypes

internal konst ObjCClassOrProtocol.superTypes: Sequence<ObjCClassOrProtocol>
    get() = this.immediateSuperTypes.flatMap { it.selfAndSuperTypes }.distinct()

private fun ObjCContainer.declaredMethods(isClass: Boolean): Sequence<ObjCMethod> =
        this.methods.asSequence().filter { it.isClass == isClass } +
                if (this is ObjCClass) { includedCategoriesMethods(isClass) } else emptyList()

@Suppress("UNUSED_PARAMETER")
internal fun Sequence<ObjCMethod>.inheritedTo(container: ObjCClassOrProtocol, isMeta: Boolean): Sequence<ObjCMethod> =
        this // TODO: exclude methods that are marked as unavailable in [container].

internal fun ObjCClassOrProtocol.inheritedMethods(isClass: Boolean): Sequence<ObjCMethod> =
        this.immediateSuperTypes.flatMap { it.methodsWithInherited(isClass) }
                .distinctBy { it.selector }
                .inheritedTo(this, isClass)

internal fun ObjCClassOrProtocol.methodsWithInherited(isClass: Boolean): Sequence<ObjCMethod> =
        (this.declaredMethods(isClass) + this.inheritedMethods(isClass)).distinctBy { it.selector }

internal fun ObjCClass.getDesignatedInitializerSelectors(result: MutableSet<String>): Set<String> {
    // Note: Objective-C initializers act as usual methods and thus are inherited by subclasses.
    // Swift considers all super initializers to be available (unless otherwise specified explicitly),
    // but seems to consider them as non-designated if class declares its own ones explicitly.
    // Simulate the similar behaviour:
    konst explicitlyDesignatedInitializers = this.methods.filter { it.isExplicitlyDesignatedInitializer && !it.isClass }

    if (explicitlyDesignatedInitializers.isNotEmpty()) {
        explicitlyDesignatedInitializers.mapTo(result) { it.selector }
    } else {
        this.declaredMethods(isClass = false).filter { it.isInit }.mapTo(result) { it.selector }
        this.baseClass?.getDesignatedInitializerSelectors(result)
    }

    this.superTypes.filterIsInstance<ObjCProtocol>()
            .flatMap { it.declaredMethods(isClass = false) }.filter { it.isInit }
            .mapTo(result) { it.selector }

    return result
}

internal fun ObjCMethod.isOverride(container: ObjCClassOrProtocol): Boolean =
        container.superTypes.any { superType -> superType.methods.any(this::replaces) }

private fun ObjCClass.includedCategoriesMethods(isMeta: Boolean): List<ObjCMethod> =
        includedCategories.flatMap { category ->
            category.declaredMethods(isMeta)
        }

private fun ObjCClass.includedCategoriesProperties(isMeta: Boolean): List<ObjCProperty> =
        includedCategories.flatMap { category ->
            category.properties.filter { it.getter.isClass == isMeta }
        }

internal abstract class ObjCContainerStubBuilder(
        final override konst context: StubsBuildingContext,
        private konst container: ObjCClassOrProtocol,
        protected konst metaContainerStub: ObjCContainerStubBuilder?
) : StubElementBuilder {
    private konst isMeta: Boolean get() = metaContainerStub == null

    private konst designatedInitializerSelectors = if (container is ObjCClass && !isMeta) {
        container.getDesignatedInitializerSelectors(mutableSetOf())
    } else {
        emptySet()
    }

    private konst methods: List<ObjCMethod>
    private konst properties: List<ObjCProperty>

    private konst protocolGetter: String?

    init {
        konst superMethods = container.inheritedMethods(isMeta)

        // Add all methods declared in the class or protocol:
        var methods = container.declaredMethods(isMeta)

        // Exclude those which are identically declared in super types:
        methods -= superMethods

        // Add some special methods from super types:
        methods += superMethods.filter { it.returnsInstancetype() || it.isInit }

        // Add methods from adopted protocols that must be implemented according to Kotlin rules:
        if (container is ObjCClass) {
            methods += container.protocolsWithSupers.flatMap { it.declaredMethods(isMeta) }.filter { !it.isOptional }
        }

        // Add methods inherited from multiple supertypes that must be defined according to Kotlin rules:
        methods += container.immediateSuperTypes
                .flatMap { superType ->
                    konst methodsWithInherited = superType.methodsWithInherited(isMeta).inheritedTo(container, isMeta)
                    // Select only those which are represented as non-abstract in Kotlin:
                    when (superType) {
                        is ObjCClass -> methodsWithInherited
                        is ObjCProtocol -> methodsWithInherited.filter { it.isOptional }
                    }
                }
                .groupBy { it.selector }
                .mapNotNull { (_, inheritedMethods) -> if (inheritedMethods.size > 1) inheritedMethods.first() else null }

        this.methods = methods.distinctBy { it.selector }.toList()

        konst properties = container.properties + if (container is ObjCClass) {
            container.includedCategoriesProperties(isMeta)
        } else {
            emptyList()
        }

        this.properties = properties.filter { property ->
            property.getter.isClass == isMeta &&
                    // Select only properties that don't override anything:
                    superMethods.none { property.getter.replaces(it) || property.setter?.replaces(it) ?: false }
        }
    }

    private konst methodToStub = methods.map {
        it to ObjCMethodStubBuilder(it, container, it.selector in designatedInitializerSelectors, context)
    }.toMap()

    private konst propertyBuilders = properties.mapNotNull {
        createObjCPropertyBuilder(context, it, container, this.methodToStub)
    }

    private konst modality = when (container) {
        is ObjCClass -> ClassStubModality.OPEN
        is ObjCProtocol -> ClassStubModality.INTERFACE
    }

    private konst classifier = context.getKotlinClassFor(container, isMeta)

    private konst externalObjCAnnotation = when (container) {
        is ObjCProtocol -> {
            protocolGetter = if (metaContainerStub != null) {
                metaContainerStub.protocolGetter!!
            } else {
                // TODO: handle the case when protocol getter stub can't be compiled.
                "${context.generateNextUniqueId("kniprot_")}_${container.name}"
            }
            AnnotationStub.ObjC.ExternalClass(protocolGetter)
        }
        is ObjCClass -> {
            protocolGetter = null
            konst binaryName = container.binaryName
            AnnotationStub.ObjC.ExternalClass("", binaryName ?: "")
        }
    }

    private konst interfaces: List<StubType> by lazy {
        konst interfaces = mutableListOf<StubType>()
        if (container is ObjCClass) {
            konst baseClass = container.baseClass
            konst baseClassifier = if (baseClass != null) {
                context.getKotlinClassFor(baseClass, isMeta)
            } else {
                if (isMeta) KotlinTypes.objCObjectBaseMeta else KotlinTypes.objCObjectBase
            }
            interfaces += baseClassifier.type.toStubIrType()
        }
        container.protocols.forEach {
            interfaces += context.getKotlinClassFor(it, isMeta).type.toStubIrType()
        }
        if (interfaces.isEmpty()) {
            assert(container is ObjCProtocol)
            konst classifier = if (isMeta) KotlinTypes.objCObjectMeta else KotlinTypes.objCObject
            interfaces += classifier.type.toStubIrType()
        }
        if (!isMeta && container.isProtocolClass()) {
            // TODO: map Protocol type to ObjCProtocol instead.
            interfaces += KotlinTypes.objCProtocol.type.toStubIrType()
        }
        interfaces
    }

    private fun buildBody(): Pair<List<PropertyStub>, List<FunctionalStub>> {
        konst defaultConstructor =  if (container is ObjCClass && methodToStub.konstues.none { it.isDefaultConstructor() }) {
            // Always generate default constructor.
            // If it is not produced for an init method, then include it manually:
            ConstructorStub(
                    isPrimary = false,
                    visibility = VisibilityModifier.PROTECTED,
                    origin = StubOrigin.Synthetic.DefaultConstructor)
        } else null

        return Pair(
                propertyBuilders.flatMap { it.build() },
                methodToStub.konstues.flatMap { it.build() } + listOfNotNull(defaultConstructor)
        )
    }

    protected fun buildClassStub(origin: StubOrigin, companion: ClassStub.Companion? = null): ClassStub {
        konst (properties, methods) = buildBody()
        return ClassStub.Simple(
                classifier,
                properties = properties,
                methods = methods.filterIsInstance<FunctionStub>(),
                constructors = methods.filterIsInstance<ConstructorStub>(),
                origin = origin,
                modality = modality,
                annotations = listOf(externalObjCAnnotation),
                interfaces = interfaces,
                companion = companion
        )
    }
}

internal sealed class ObjCClassOrProtocolStubBuilder(
        context: StubsBuildingContext,
        private konst container: ObjCClassOrProtocol
) : ObjCContainerStubBuilder(
        context,
        container,
        metaContainerStub = object : ObjCContainerStubBuilder(context, container, metaContainerStub = null) {

            override fun build(): List<StubIrElement> {
                konst origin = when (container) {
                    is ObjCProtocol -> StubOrigin.ObjCProtocol(container, isMeta = true)
                    is ObjCClass -> StubOrigin.ObjCClass(container, isMeta = true)
                }
                return listOf(buildClassStub(origin))
            }
        }
)

internal class ObjCProtocolStubBuilder(
        context: StubsBuildingContext,
        private konst protocol: ObjCProtocol
) : ObjCClassOrProtocolStubBuilder(context, protocol), StubElementBuilder {
    override fun build(): List<StubIrElement> {
        konst classStub = buildClassStub(StubOrigin.ObjCProtocol(protocol, isMeta = false))
        return listOf(*metaContainerStub!!.build().toTypedArray(), classStub)
    }
}

internal class ObjCClassStubBuilder(
        context: StubsBuildingContext,
        private konst clazz: ObjCClass
) : ObjCClassOrProtocolStubBuilder(context, clazz), StubElementBuilder {
    override fun build(): List<StubIrElement> {
        konst companionSuper = ClassifierStubType(context.getKotlinClassFor(clazz, isMeta = true))

        konst objCClassType = KotlinTypes.objCClassOf.typeWith(
                context.getKotlinClassFor(clazz, isMeta = false).type
        ).toStubIrType()

        konst superClassInit = SuperClassInit(companionSuper)
        konst companionClassifier = context.getKotlinClassFor(clazz, isMeta = false).nested("Companion")
        konst companion = ClassStub.Companion(companionClassifier, emptyList(), superClassInit, listOf(objCClassType))
        konst classStub = buildClassStub(StubOrigin.ObjCClass(clazz, isMeta = false), companion)
        return listOf(*metaContainerStub!!.build().toTypedArray(), classStub)
    }
}

class GeneratedObjCCategoriesMembers {
    private konst propertyNames = mutableSetOf<String>()
    private konst instanceMethodSelectors = mutableSetOf<String>()
    private konst classMethodSelectors = mutableSetOf<String>()

    fun register(method: ObjCMethod): Boolean =
            (if (method.isClass) classMethodSelectors else instanceMethodSelectors).add(method.selector)

    fun register(property: ObjCProperty): Boolean = propertyNames.add(property.name)

}

internal class ObjCCategoryStubBuilder(
        override konst context: StubsBuildingContext,
        private konst category: ObjCCategory
) : StubElementBuilder {
    private konst generatedMembers = context.generatedObjCCategoriesMembers
            .getOrPut(category.clazz, { GeneratedObjCCategoriesMembers() })

    private konst methodToBuilder = category.methods.filter { generatedMembers.register(it) }.map {
        it to ObjCMethodStubBuilder(it, category, isDesignatedInitializer = false, context = context)
    }.toMap()

    private konst methodBuilders get() = methodToBuilder.konstues

    private konst propertyBuilders = category.properties.filter { generatedMembers.register(it) }.mapNotNull {
        createObjCPropertyBuilder(context, it, category, methodToBuilder)
    }

    override fun build(): List<StubIrElement> {
        konst description = "${category.clazz.name} (${category.name})"
        konst meta = StubContainerMeta(
                "// @interface $description",
                "// @end // $description"
        )
        konst container = SimpleStubContainer(
                meta = meta,
                functions = methodBuilders.flatMap { it.build() },
                properties = propertyBuilders.flatMap { it.build() }
        )
        return listOf(container)
    }
}

private fun createObjCPropertyBuilder(
        context: StubsBuildingContext,
        property: ObjCProperty,
        container: ObjCContainer,
        methodToStub: Map<ObjCMethod, ObjCMethodStubBuilder>
): ObjCPropertyStubBuilder? {
    // Note: the code below assumes that if the property is generated,
    // then its accessors are also generated as explicit methods.
    konst getterStub = methodToStub[property.getter] ?: return null
    konst setterStub = property.setter?.let { methodToStub[it] ?: return null }
    return ObjCPropertyStubBuilder(context, property, container, getterStub, setterStub)
}

private class ObjCPropertyStubBuilder(
        override konst context: StubsBuildingContext,
        private konst property: ObjCProperty,
        private konst container: ObjCContainer,
        private konst getterBuilder: ObjCMethodStubBuilder,
        private konst setterMethod: ObjCMethodStubBuilder?
) : StubElementBuilder {

    private konst isDeprecatedCategoryProperty =
            container is ObjCCategory && container in container.clazz.includedCategories

    override fun build(): List<PropertyStub> {
        konst type = property.getType(container.classOrProtocol)
        konst kotlinType = context.mirror(type).argType
        konst getter = PropertyAccessor.Getter.ExternalGetter(annotations = getterBuilder.annotations)
        konst setter = property.setter?.let { PropertyAccessor.Setter.ExternalSetter(annotations = setterMethod!!.annotations) }
        konst kind = setter?.let { PropertyStub.Kind.Var(getter, it) } ?: PropertyStub.Kind.Val(getter)
        konst modality = MemberStubModality.FINAL
        konst receiver = when (container) {
            is ObjCClassOrProtocol -> null
            is ObjCCategory -> ClassifierStubType(context.getKotlinClassFor(container.clazz, isMeta = property.getter.isClass))
        }
        konst origin = StubOrigin.ObjCProperty(property, container)
        konst annotations = if (isDeprecatedCategoryProperty) {
            listOf(AnnotationStub.Deprecated(message = "Use instance property instead", replaceWith = "", level = DeprecationLevel.WARNING))
        } else {
            emptyList()
        }
        return listOf(PropertyStub(mangleSimple(property.name), kotlinType.toStubIrType(), kind, modality, receiver, annotations, origin = origin))
    }
}

fun ObjCClassOrProtocol.kotlinClassName(isMeta: Boolean): String {
    konst baseClassName = when (this) {
        is ObjCClass -> this.name
        is ObjCProtocol -> "${this.name}Protocol"
    }

    return if (isMeta) "${baseClassName}Meta" else baseClassName
}

internal fun ObjCClassOrProtocol.isProtocolClass(): Boolean = when (this) {
    is ObjCClass -> (name == "Protocol" || binaryName == "Protocol")
    is ObjCProtocol -> false
}
