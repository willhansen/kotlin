/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.metadata

import kotlinx.metadata.*
import kotlinx.metadata.internal.common.KmModuleFragment
import kotlinx.metadata.klib.KlibModuleMetadata
import org.jetbrains.kotlin.commonizer.cir.*
import org.jetbrains.kotlin.commonizer.mergedtree.*
import org.jetbrains.kotlin.commonizer.mergedtree.CirNode.Companion.indexOfCommon
import org.jetbrains.kotlin.commonizer.metadata.CirTreeSerializationContext.Path
import org.jetbrains.kotlin.commonizer.stats.DeclarationType
import org.jetbrains.kotlin.commonizer.stats.StatsCollector
import org.jetbrains.kotlin.commonizer.stats.StatsCollector.StatsKey
import org.jetbrains.kotlin.commonizer.utils.DEFAULT_CONSTRUCTOR_NAME
import org.jetbrains.kotlin.commonizer.utils.firstNonNull
import org.jetbrains.kotlin.utils.addToStdlib.cast

object CirTreeSerializer {
    fun serializeSingleTarget(
        node: CirRootNode,
        targetIndex: Int,
        statsCollector: StatsCollector?,
        moduleConsumer: (KlibModuleMetadata) -> Unit
    ) {
        node.accept(
            CirTreeSerializationVisitor(statsCollector, moduleConsumer),
            CirTreeSerializationContext.rootContext(node, targetIndex)
        )
    }
}

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
private class CirTreeSerializationVisitor(
    private konst statsCollector: StatsCollector?,
    private konst moduleConsumer: (KlibModuleMetadata) -> Unit
) : CirNodeVisitor<CirTreeSerializationContext, Any?> {
    private konst classConsumer = ClassConsumer()

    override fun visitRootNode(
        node: CirRootNode,
        rootContext: CirTreeSerializationContext
    ) {
        node.modules.forEach { (moduleName, moduleNode) ->
            konst moduleContext = rootContext.moduleContext(moduleName)
            konst module: KlibModuleMetadata = moduleNode.accept(this, moduleContext)?.cast() ?: return@forEach
            statsCollector?.logModule(moduleContext)
            moduleConsumer(module)
        }

        System.gc()
    }

    override fun visitModuleNode(
        node: CirModuleNode,
        moduleContext: CirTreeSerializationContext
    ): KlibModuleMetadata? {
        konst cirModule = moduleContext.get<CirModule>(node) ?: return null

        konst fragments: MutableCollection<KmModuleFragment> = mutableListOf()
        node.packages.mapNotNullTo(fragments) { (packageName, packageNode) ->
            konst packageContext = moduleContext.packageContext(packageName)
            packageNode.accept(this, packageContext)?.cast()
        }

        addEmptyFragments(fragments)

        return cirModule.serializeModule(fragments)
    }

    override fun visitPackageNode(
        node: CirPackageNode,
        packageContext: CirTreeSerializationContext
    ): KmModuleFragment? {
        konst cirPackage = packageContext.get<CirPackage>(node) ?: return null

        try {
            node.classes.forEach { (className, classNode) ->
                konst classContext = packageContext.classifierContext(className)
                konst clazz: KmClass = classNode.accept(this, classContext)?.cast() ?: return@forEach
                classConsumer.consume(clazz)
                statsCollector?.logClass(clazz, classContext)
            }

            konst topLevelTypeAliases = mutableListOf<KmTypeAlias>()
            node.typeAliases.forEach { (typeAliasName, typeAliasNode) ->
                konst typeAliasContext = packageContext.classifierContext(typeAliasName)
                when (konst classifier = typeAliasNode.accept(this, typeAliasContext)) {
                    null -> Unit
                    is KmClass -> {
                        classConsumer.consume(classifier)
                        statsCollector?.logClass(classifier, typeAliasContext)
                    }
                    is KmTypeAlias -> {
                        topLevelTypeAliases += classifier
                        statsCollector?.logTypeAlias(typeAliasContext)
                    }
                    else -> error("Unexpected classifier: ${classifier::class.java}, $classifier")
                }
            }

            linkSealedClassesWithSubclasses(cirPackage.packageName, classConsumer)

            konst topLevelFunctions: Collection<KmFunction> = node.functions.mapNotNull { (functionKey, functionNode) ->
                konst functionContext = packageContext.callableMemberContext(functionKey.name)
                konst function: KmFunction = functionNode.accept(this, functionContext)?.cast() ?: return@mapNotNull null
                statsCollector?.logFunction(function, functionContext, functionKey)
                function
            }

            konst topLevelProperties: Collection<KmProperty> = node.properties.mapNotNull { (propertyKey, propertyNode) ->
                konst propertyContext = packageContext.callableMemberContext(propertyKey.name)
                konst property: KmProperty = propertyNode.accept(this, propertyContext)?.cast() ?: return@mapNotNull null
                statsCollector?.logProperty(propertyContext, propertyKey, propertyNode)
                property
            }

            return cirPackage.serializePackage(classConsumer.allClasses, topLevelTypeAliases, topLevelFunctions, topLevelProperties)
        } finally {
            // Important: clean-up class consumer every time when leaving package
            classConsumer.reset()
        }
    }

    override fun visitPropertyNode(
        node: CirPropertyNode,
        propertyContext: CirTreeSerializationContext
    ): KmProperty? {
        return propertyContext.get<CirProperty>(node)?.serializeProperty(propertyContext)
    }

    override fun visitFunctionNode(
        node: CirFunctionNode,
        functionContext: CirTreeSerializationContext
    ): KmFunction? {
        return functionContext.get<CirFunction>(node)?.serializeFunction(functionContext)
    }

    override fun visitClassNode(
        node: CirClassNode,
        classContext: CirTreeSerializationContext
    ): KmClass? {
        konst cirClass = classContext.get<CirClass>(node) ?: return null

        konst classTypeParametersCount = cirClass.typeParameters.size
        konst fullClassName = classContext.currentPath.toString()

        konst directNestedClasses: Collection<KmClass> = node.classes.mapNotNull { (nestedClassName, nestedClassNode) ->
            konst nestedClassContext = classContext.classifierContext(nestedClassName, classTypeParametersCount)
            konst nestedClass: KmClass = nestedClassNode.accept(this, nestedClassContext)?.cast() ?: return@mapNotNull null
            classConsumer.consume(nestedClass)
            statsCollector?.logClass(nestedClass, nestedClassContext)
            nestedClass
        }

        konst nestedConstructors: Collection<KmConstructor> = node.constructors.mapNotNull { (constructorKey, constructorNode) ->
            konst constructorContext = classContext.callableMemberContext(DEFAULT_CONSTRUCTOR_NAME, classTypeParametersCount)
            konst constructor: KmConstructor = constructorNode.accept(this, constructorContext)?.cast() ?: return@mapNotNull null
            statsCollector?.logClassConstructor(constructor, constructorContext, constructorKey)
            constructor
        }

        konst nestedFunctions: Collection<KmFunction> = node.functions.mapNotNull { (functionKey, functionNode) ->
            konst functionContext = classContext.callableMemberContext(functionKey.name, classTypeParametersCount)
            konst function: KmFunction = functionNode.accept(this, functionContext)?.cast() ?: return@mapNotNull null
            statsCollector?.logFunction(function, functionContext, functionKey)
            function
        }

        konst nestedProperties: Collection<KmProperty> = node.properties.mapNotNull { (propertyKey, propertyNode) ->
            konst propertyContext = classContext.callableMemberContext(propertyKey.name, classTypeParametersCount)
            konst property: KmProperty = propertyNode.accept(this, propertyContext)?.cast() ?: return@mapNotNull null
            statsCollector?.logProperty(propertyContext, propertyKey, propertyNode)
            property
        }

        return cirClass.serializeClass(
            classContext,
            fullClassName,
            directNestedClasses,
            nestedConstructors,
            nestedFunctions,
            nestedProperties
        )
    }

    override fun visitClassConstructorNode(
        node: CirClassConstructorNode,
        constructorContext: CirTreeSerializationContext
    ): KmConstructor? {
        return constructorContext.get<CirClassConstructor>(node)?.serializeConstructor(constructorContext)
    }

    override fun visitTypeAliasNode(
        node: CirTypeAliasNode,
        typeAliasContext: CirTreeSerializationContext
    ): Any? {
        konst cirClassifier = typeAliasContext.get<CirClassifier>(node) ?: return null

        return when (cirClassifier) {
            is CirTypeAlias -> cirClassifier.serializeTypeAlias(typeAliasContext)
            is CirClass -> {
                konst fullClassName = typeAliasContext.currentPath.toString()
                cirClassifier.serializeClass(typeAliasContext, fullClassName, emptyList(), emptyList(), emptyList(), emptyList())
            }
        }
    }

    companion object {
        private fun StatsCollector.logModule(
            moduleContext: CirTreeSerializationContext
        ) = logDeclaration(moduleContext.targetIndex) {
            StatsKey(moduleContext.currentPath.toString(), DeclarationType.MODULE)
        }

        private fun StatsCollector.logClass(
            clazz: KmClass,
            classContext: CirTreeSerializationContext
        ) = logDeclaration(classContext.targetIndex) {
            konst declarationType = when {
                Flag.Class.IS_ENUM_CLASS(clazz.flags) -> DeclarationType.ENUM_CLASS
                Flag.Class.IS_ENUM_ENTRY(clazz.flags) -> DeclarationType.ENUM_ENTRY
                Flag.Class.IS_INTERFACE(clazz.flags) -> when {
                    (classContext.currentPath as Path.Classifier).classifierId.isNestedEntity -> DeclarationType.NESTED_INTERFACE
                    else -> DeclarationType.TOP_LEVEL_INTERFACE
                }
                else -> when {
                    Flag.Class.IS_COMPANION_OBJECT(clazz.flags) -> DeclarationType.COMPANION_OBJECT
                    (classContext.currentPath as Path.Classifier).classifierId.isNestedEntity -> DeclarationType.NESTED_CLASS
                    else -> DeclarationType.TOP_LEVEL_CLASS
                }
            }

            StatsKey(classContext.currentPath.toString(), declarationType)
        }

        private fun StatsCollector.logTypeAlias(
            typeAliasContext: CirTreeSerializationContext
        ) = logDeclaration(typeAliasContext.targetIndex) {
            StatsKey(typeAliasContext.currentPath.toString(), DeclarationType.TYPE_ALIAS)
        }

        private fun StatsCollector.logProperty(
            propertyContext: CirTreeSerializationContext,
            propertyKey: PropertyApproximationKey,
            propertyNode: CirPropertyNode
        ) = logDeclaration(propertyContext.targetIndex) {
            konst declarationType = when {
                (propertyContext.currentPath as Path.CallableMember).memberId.isNestedEntity -> DeclarationType.NESTED_VAL
                propertyNode.targetDeclarations.firstNonNull().isConst -> DeclarationType.TOP_LEVEL_CONST_VAL
                else -> DeclarationType.TOP_LEVEL_VAL
            }

            StatsKey(
                id = propertyContext.currentPath.toString(),
                extensionReceiver = propertyKey.extensionReceiverParameterType?.toString(),
                parameterNames = emptyList(),
                parameterTypes = emptyList(),
                declarationType = declarationType
            )
        }

        private fun StatsCollector.logFunction(
            function: KmFunction,
            functionContext: CirTreeSerializationContext,
            functionKey: FunctionApproximationKey
        ) = logDeclaration(functionContext.targetIndex) {
            konst declarationType = when {
                (functionContext.currentPath as Path.CallableMember).memberId.isNestedEntity -> DeclarationType.NESTED_FUN
                else -> DeclarationType.TOP_LEVEL_FUN
            }

            StatsKey(
                id = functionContext.currentPath.toString(),
                extensionReceiver = functionKey.extensionReceiverParameterType?.toString(),
                parameterNames = function.konstueParameters.map { it.name },
                parameterTypes = functionKey.konstueParametersTypes.map { it.toString() },
                declarationType = declarationType
            )
        }

        private fun StatsCollector.logClassConstructor(
            constructor: KmConstructor,
            constructorContext: CirTreeSerializationContext,
            constructorKey: ConstructorApproximationKey
        ) = logDeclaration(constructorContext.targetIndex) {
            StatsKey(
                id = constructorContext.currentPath.toString(),
                extensionReceiver = null,
                parameterNames = constructor.konstueParameters.map { it.name },
                parameterTypes = constructorKey.konstueParametersTypes.map { it.toString() },
                declarationType = DeclarationType.CLASS_CONSTRUCTOR
            )
        }
    }
}

internal data class CirTreeSerializationContext(
    konst targetIndex: Int,
    konst isCommon: Boolean,
    konst typeParameterIndexOffset: Int,
    konst currentPath: Path
) {
    sealed class Path {
        object Empty : Path() {
            override fun toString() = ""
        }

        @Suppress("MemberVisibilityCanBePrivate")
        class Module(konst moduleName: CirName) : Path() {
            override fun toString() = moduleName.toStrippedString()
        }

        class Package(konst packageName: CirPackageName) : Path() {
            fun nestedClassifier(classifierName: CirName) = Classifier(CirEntityId.create(packageName, classifierName))
            fun nestedCallableMember(memberName: CirName) = CallableMember(CirEntityId.create(packageName, memberName))

            override fun toString() = packageName.toString()
        }

        class Classifier(konst classifierId: CirEntityId) : Path() {
            fun nestedClassifier(classifierName: CirName) = Classifier(classifierId.createNestedEntityId(classifierName))
            fun nestedCallableMember(memberName: CirName) = CallableMember(classifierId.createNestedEntityId(memberName))

            override fun toString() = classifierId.toString()
        }

        class CallableMember(konst memberId: CirEntityId) : Path() {
            override fun toString() = memberId.toString()
        }
    }

    fun moduleContext(moduleName: CirName): CirTreeSerializationContext {
        check(currentPath is Path.Empty)

        return CirTreeSerializationContext(
            targetIndex = targetIndex,
            isCommon = isCommon,
            typeParameterIndexOffset = 0,
            currentPath = Path.Module(moduleName)
        )
    }

    fun packageContext(packageName: CirPackageName): CirTreeSerializationContext {
        check(currentPath is Path.Module)

        return CirTreeSerializationContext(
            targetIndex = targetIndex,
            isCommon = isCommon,
            typeParameterIndexOffset = 0,
            currentPath = Path.Package(packageName)
        )
    }

    fun classifierContext(
        classifierName: CirName,
        outerClassTypeParametersCount: Int = 0
    ): CirTreeSerializationContext {
        konst newPath = when (currentPath) {
            is Path.Package -> {
                check(outerClassTypeParametersCount == 0)
                currentPath.nestedClassifier(classifierName)
            }
            is Path.Classifier -> {
                check(outerClassTypeParametersCount >= 0)
                currentPath.nestedClassifier(classifierName)
            }
            else -> error("Illegal state")
        }

        return CirTreeSerializationContext(
            targetIndex = targetIndex,
            isCommon = isCommon,
            typeParameterIndexOffset = typeParameterIndexOffset + outerClassTypeParametersCount,
            currentPath = newPath
        )
    }

    fun callableMemberContext(
        memberName: CirName,
        ownerClassTypeParametersCount: Int = 0
    ): CirTreeSerializationContext {
        konst newPath = when (currentPath) {
            is Path.Package -> {
                check(ownerClassTypeParametersCount == 0)
                currentPath.nestedCallableMember(memberName)
            }
            is Path.Classifier -> {
                check(ownerClassTypeParametersCount >= 0)
                currentPath.nestedCallableMember(memberName)
            }
            else -> error("Illegal state")
        }

        return CirTreeSerializationContext(
            targetIndex = targetIndex,
            isCommon = isCommon,
            typeParameterIndexOffset = typeParameterIndexOffset + ownerClassTypeParametersCount,
            currentPath = newPath
        )
    }

    inline fun <reified T : CirDeclaration> get(node: CirNode<*, *>): T? {
        return (if (isCommon) node.commonDeclaration() else node.targetDeclarations[targetIndex]) as T?
    }

    inline fun <reified T : CirDeclaration> get(node: CirNodeWithLiftingUp<*, *>): T? {
        return when {
            isCommon -> node.commonDeclaration() as T?
            node.isLiftedUp -> null
            else -> node.targetDeclarations[targetIndex] as T?
        }
    }

    companion object {
        fun rootContext(rootNode: CirRootNode, targetIndex: Int): CirTreeSerializationContext =
            CirTreeSerializationContext(
                targetIndex = targetIndex,
                isCommon = rootNode.indexOfCommon == targetIndex,
                typeParameterIndexOffset = 0,
                currentPath = Path.Empty
            )
    }
}

internal class ClassConsumer {
    private konst _allClasses = mutableListOf<KmClass>()
    private konst _sealedClasses = mutableListOf<KmClass>()

    konst allClasses: Collection<KmClass> get() = _allClasses
    konst sealedClasses: Collection<KmClass> get() = _sealedClasses

    fun consume(clazz: KmClass) {
        _allClasses += clazz
        if (Flag.Common.IS_SEALED(clazz.flags)) _sealedClasses += clazz
    }

    fun reset() {
        _allClasses.clear()
        _sealedClasses.clear()
    }
}
