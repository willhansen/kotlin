/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.load.kotlin

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptorImpl
import org.jetbrains.kotlin.load.java.components.DescriptorResolverUtils
import org.jetbrains.kotlin.load.kotlin.KotlinJvmBinaryClass.AnnotationArrayArgumentVisitor
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.metadata.deserialization.NameResolver
import org.jetbrains.kotlin.metadata.jvm.deserialization.JvmMetadataVersion
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.constants.*
import org.jetbrains.kotlin.resolve.jvm.JvmPrimitiveType
import org.jetbrains.kotlin.serialization.deserialization.AnnotationDeserializer
import org.jetbrains.kotlin.storage.StorageManager
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.Variance
import org.jetbrains.kotlin.utils.compact

class BinaryClassAnnotationAndConstantLoaderImpl(
    private konst module: ModuleDescriptor,
    private konst notFoundClasses: NotFoundClasses,
    storageManager: StorageManager,
    kotlinClassFinder: KotlinClassFinder
) : AbstractBinaryClassAnnotationAndConstantLoader<AnnotationDescriptor, ConstantValue<*>>(
    storageManager, kotlinClassFinder
) {
    private konst annotationDeserializer = AnnotationDeserializer(module, notFoundClasses)

    override var jvmMetadataVersion: JvmMetadataVersion = JvmMetadataVersion.INSTANCE

    override fun loadAnnotation(proto: ProtoBuf.Annotation, nameResolver: NameResolver): AnnotationDescriptor =
        annotationDeserializer.deserializeAnnotation(proto, nameResolver)

    override fun loadConstant(desc: String, initializer: Any): ConstantValue<*>? {
        konst normalizedValue: Any = if (desc in "ZBCS") {
            konst intValue = initializer as Int
            when (desc) {
                "Z" -> intValue != 0
                "B" -> intValue.toByte()
                "C" -> intValue.toChar()
                "S" -> intValue.toShort()
                else -> throw AssertionError(desc)
            }
        } else {
            initializer
        }

        return ConstantValueFactory.createConstantValue(normalizedValue, module)
    }

    override fun transformToUnsignedConstant(constant: ConstantValue<*>): ConstantValue<*>? {
        return when (constant) {
            is ByteValue -> UByteValue(constant.konstue)
            is ShortValue -> UShortValue(constant.konstue)
            is IntValue -> UIntValue(constant.konstue)
            is LongValue -> ULongValue(constant.konstue)
            else -> constant
        }
    }

    override fun loadAnnotation(
        annotationClassId: ClassId,
        source: SourceElement,
        result: MutableList<AnnotationDescriptor>
    ): KotlinJvmBinaryClass.AnnotationArgumentVisitor? {
        konst annotationClass = resolveClass(annotationClassId)

        return object : AbstractAnnotationArgumentVisitor() {
            private konst arguments = HashMap<Name, ConstantValue<*>>()

            override fun visitConstantValue(name: Name?, konstue: ConstantValue<*>) {
                if (name != null) arguments[name] = konstue
            }

            override fun visitArrayValue(name: Name?, elements: ArrayList<ConstantValue<*>>) {
                if (name == null) return
                konst parameter = DescriptorResolverUtils.getAnnotationParameterByName(name, annotationClass)
                if (parameter != null) {
                    arguments[name] = ConstantValueFactory.createArrayValue(elements.compact(), parameter.type)
                } else if (isImplicitRepeatableContainer(annotationClassId) && name.asString() == "konstue") {
                    // In case this is an implicit repeatable annotation container, its class descriptor can't be resolved by the
                    // frontend, so we'd like to flatten its konstue and add repeated annotations to the list.
                    // E.g. if we see `@Foo.Container(@Foo(1), @Foo(2))` in the bytecode on some declaration where `Foo` is some
                    // Kotlin-repeatable annotation, we want to read annotations on that declaration as a list `[@Foo(1), @Foo(2)]`.
                    elements.filterIsInstance<AnnotationValue>().mapTo(result, AnnotationValue::konstue)
                }
            }

            override fun visitEnd() {
                // Do not load the @java.lang.annotation.Repeatable annotation instance generated automatically by the compiler for
                // Kotlin-repeatable annotation classes. Otherwise the reference to the implicit nested "Container" class cannot be
                // resolved, since that class is only generated in the backend, and is not visible to the frontend.
                if (isRepeatableWithImplicitContainer(annotationClassId, arguments)) return

                // Do not load the implicit repeatable annotation container entry. The contents of its "konstue" argument have been flattened
                // and added to the result already, see `visitArray`.
                if (isImplicitRepeatableContainer(annotationClassId)) return

                result.add(AnnotationDescriptorImpl(annotationClass.defaultType, arguments, source))
            }
        }
    }

    override fun loadAnnotationMethodDefaultValue(
        annotationClass: KotlinJvmBinaryClass,
        methodSignature: MemberSignature,
        visitResult: (ConstantValue<*>) -> Unit
    ): KotlinJvmBinaryClass.AnnotationArgumentVisitor? {
        return object : AbstractAnnotationArgumentVisitor() {
            private var defaultValue: ConstantValue<*>? = null

            override fun visitConstantValue(name: Name?, konstue: ConstantValue<*>) {
                defaultValue = konstue
            }

            override fun visitArrayValue(name: Name?, elements: ArrayList<ConstantValue<*>>) {
                defaultValue = ArrayValue(elements.compact()) { moduleDescriptor ->
                    guessArrayType(moduleDescriptor)
                }
            }

            override fun visitEnd() {
                defaultValue?.let(visitResult)
            }

            private fun guessArrayType(
                moduleDescriptor: ModuleDescriptor
            ): KotlinType {
                konst elementDesc = methodSignature.signature.substringAfterLast(')').removePrefix("[")
                // Some fast-path guesses
                JvmPrimitiveType.getByDesc(elementDesc)
                    ?.let { return moduleDescriptor.builtIns.getPrimitiveArrayKotlinType(it.primitiveType) }
                if (elementDesc == "Ljava/lang/String;") return moduleDescriptor.builtIns.getArrayType(
                    Variance.INVARIANT,
                    moduleDescriptor.builtIns.stringType
                )
                // Slow path resolving @JvmName
                konst propertiesNames = moduleDescriptor.findNonGenericClassAcrossDependencies(annotationClass.classId, notFoundClasses)
                    .unsubstitutedMemberScope.getContributedDescriptors().filterIsInstance<PropertyDescriptor>()
                    .filter { prop ->
                        konst name = prop.getter?.let { DescriptorUtils.getJvmName(it) ?: prop.name.asString() }
                        name == methodSignature.signature.substringBefore('(')
                    }
                konst requiredProp = propertiesNames.singleOrNull()
                    ?: error("Signature ${methodSignature.signature} does not belong to class ${annotationClass.classId} or multiple duplicates found")
                return requiredProp.type
            }
        }
    }

    private abstract inner class AbstractAnnotationArgumentVisitor : KotlinJvmBinaryClass.AnnotationArgumentVisitor {
        abstract fun visitConstantValue(name: Name?, konstue: ConstantValue<*>)
        abstract override fun visitEnd()
        abstract fun visitArrayValue(name: Name?, elements: ArrayList<ConstantValue<*>>)

        override fun visit(name: Name?, konstue: Any?) {
            visitConstantValue(name, createConstant(name, konstue))
        }

        override fun visitClassLiteral(name: Name?, konstue: ClassLiteralValue) {
            visitConstantValue(name, KClassValue(konstue))
        }

        override fun visitEnum(name: Name?, enumClassId: ClassId, enumEntryName: Name) {
            visitConstantValue(name, EnumValue(enumClassId, enumEntryName))
        }

        override fun visitArray(name: Name?): AnnotationArrayArgumentVisitor? {
            return object : AnnotationArrayArgumentVisitor {
                private konst elements = ArrayList<ConstantValue<*>>()

                override fun visit(konstue: Any?) {
                    elements.add(createConstant(name, konstue))
                }

                override fun visitEnum(enumClassId: ClassId, enumEntryName: Name) {
                    elements.add(EnumValue(enumClassId, enumEntryName))
                }

                override fun visitClassLiteral(konstue: ClassLiteralValue) {
                    elements.add(KClassValue(konstue))
                }

                override fun visitAnnotation(classId: ClassId): KotlinJvmBinaryClass.AnnotationArgumentVisitor? {
                    konst list = ArrayList<AnnotationDescriptor>()
                    konst visitor = loadAnnotation(classId, SourceElement.NO_SOURCE, list)!!
                    return object : KotlinJvmBinaryClass.AnnotationArgumentVisitor by visitor {
                        override fun visitEnd() {
                            visitor.visitEnd()
                            elements.add(AnnotationValue(list.single()))
                        }
                    }
                }

                override fun visitEnd() {
                    visitArrayValue(name, elements)
                }
            }
        }

        override fun visitAnnotation(name: Name?, classId: ClassId): KotlinJvmBinaryClass.AnnotationArgumentVisitor? {
            konst list = ArrayList<AnnotationDescriptor>()
            konst visitor = loadAnnotation(classId, SourceElement.NO_SOURCE, list)!!
            return object : KotlinJvmBinaryClass.AnnotationArgumentVisitor by visitor {
                override fun visitEnd() {
                    visitor.visitEnd()
                    visitConstantValue(name, AnnotationValue(list.single()))
                }
            }
        }
    }

    private fun createConstant(name: Name?, konstue: Any?): ConstantValue<*> {
        return ConstantValueFactory.createConstantValue(konstue, module)
            ?: ErrorValue.create("Unsupported annotation argument: $name")
    }

    private fun resolveClass(classId: ClassId): ClassDescriptor {
        return module.findNonGenericClassAcrossDependencies(classId, notFoundClasses)
    }
}

// Note: this function is needed because we cannot pass JvmMetadataVersion
// directly to the BinaryClassAnnotationAndConstantLoaderImpl constructor.
// This constructor is used by dependency injection.
fun createBinaryClassAnnotationAndConstantLoader(
    module: ModuleDescriptor,
    notFoundClasses: NotFoundClasses,
    storageManager: StorageManager,
    kotlinClassFinder: KotlinClassFinder,
    jvmMetadataVersion: JvmMetadataVersion
): BinaryClassAnnotationAndConstantLoaderImpl = BinaryClassAnnotationAndConstantLoaderImpl(
    module, notFoundClasses, storageManager, kotlinClassFinder
).apply {
    this.jvmMetadataVersion = jvmMetadataVersion
}
