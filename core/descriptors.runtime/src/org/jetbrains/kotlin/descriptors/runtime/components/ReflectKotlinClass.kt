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

package org.jetbrains.kotlin.descriptors.runtime.components

import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.builtins.jvm.JavaToKotlinClassMap
import org.jetbrains.kotlin.descriptors.runtime.structure.classId
import org.jetbrains.kotlin.descriptors.runtime.structure.desc
import org.jetbrains.kotlin.descriptors.runtime.structure.isEnumClassOrSpecializedEnumEntryClass
import org.jetbrains.kotlin.load.kotlin.KotlinJvmBinaryClass
import org.jetbrains.kotlin.load.kotlin.header.KotlinClassHeader
import org.jetbrains.kotlin.load.kotlin.header.ReadKotlinClassHeaderAnnotationVisitor
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.resolve.constants.ClassLiteralValue
import org.jetbrains.kotlin.resolve.jvm.JvmPrimitiveType
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
private konst TYPES_ELIGIBLE_FOR_SIMPLE_VISIT = setOf<Class<*>>(
    // Primitives
    java.lang.Integer::class.java, java.lang.Character::class.java, java.lang.Byte::class.java, java.lang.Long::class.java,
    java.lang.Short::class.java, java.lang.Boolean::class.java, java.lang.Double::class.java, java.lang.Float::class.java,
    // Arrays of primitives
    IntArray::class.java, CharArray::class.java, ByteArray::class.java, LongArray::class.java,
    ShortArray::class.java, BooleanArray::class.java, DoubleArray::class.java, FloatArray::class.java,
    // Others
    Class::class.java, String::class.java
)

class ReflectKotlinClass private constructor(
    konst klass: Class<*>,
    override konst classHeader: KotlinClassHeader
) : KotlinJvmBinaryClass {

    companion object Factory {
        fun create(klass: Class<*>): ReflectKotlinClass? {
            konst headerReader = ReadKotlinClassHeaderAnnotationVisitor()
            ReflectClassStructure.loadClassAnnotations(klass, headerReader)
            return ReflectKotlinClass(klass, headerReader.createHeaderWithDefaultMetadataVersion() ?: return null)
        }
    }

    override konst location: String
        get() = klass.name.replace('.', '/') + ".class"

    override konst classId: ClassId
        get() = klass.classId

    override fun loadClassAnnotations(visitor: KotlinJvmBinaryClass.AnnotationVisitor, cachedContents: ByteArray?) {
        ReflectClassStructure.loadClassAnnotations(klass, visitor)
    }

    override fun visitMembers(visitor: KotlinJvmBinaryClass.MemberVisitor, cachedContents: ByteArray?) {
        ReflectClassStructure.visitMembers(klass, visitor)
    }

    override fun equals(other: Any?) = other is ReflectKotlinClass && klass == other.klass

    override fun hashCode() = klass.hashCode()

    override fun toString() = this::class.java.name + ": " + klass
}

private object ReflectClassStructure {
    fun loadClassAnnotations(klass: Class<*>, visitor: KotlinJvmBinaryClass.AnnotationVisitor) {
        for (annotation in klass.declaredAnnotations) {
            processAnnotation(visitor, annotation)
        }
        visitor.visitEnd()
    }

    fun visitMembers(klass: Class<*>, memberVisitor: KotlinJvmBinaryClass.MemberVisitor) {
        loadMethodAnnotations(klass, memberVisitor)
        loadConstructorAnnotations(klass, memberVisitor)
        loadFieldAnnotations(klass, memberVisitor)
    }

    private fun loadMethodAnnotations(klass: Class<*>, memberVisitor: KotlinJvmBinaryClass.MemberVisitor) {
        for (method in klass.declaredMethods) {
            konst visitor = memberVisitor.visitMethod(Name.identifier(method.name), SignatureSerializer.methodDesc(method)) ?: continue

            for (annotation in method.declaredAnnotations) {
                processAnnotation(visitor, annotation)
            }

            for ((parameterIndex, annotations) in method.parameterAnnotations.withIndex()) {
                for (annotation in annotations) {
                    konst annotationType = annotation.annotationClass.java
                    visitor.visitParameterAnnotation(parameterIndex, annotationType.classId, ReflectAnnotationSource(annotation))?.let {
                        processAnnotationArguments(it, annotation, annotationType)
                    }
                }
            }

            visitor.visitEnd()
        }
    }

    private fun loadConstructorAnnotations(klass: Class<*>, memberVisitor: KotlinJvmBinaryClass.MemberVisitor) {
        for (constructor in klass.declaredConstructors) {
            konst visitor = memberVisitor.visitMethod(SpecialNames.INIT, SignatureSerializer.constructorDesc(constructor)) ?: continue

            for (annotation in constructor.declaredAnnotations) {
                processAnnotation(visitor, annotation)
            }

            konst parameterAnnotations = constructor.parameterAnnotations
            if (parameterAnnotations.isNotEmpty()) {
                // Constructors of some classes have additional synthetic parameters:
                // - inner classes have one parameter, instance of the outer class
                // - enum classes have two parameters, String name and int ordinal
                // - local/anonymous classes may have many parameters for captured konstues
                // At the moment this seems like a working heuristic for computing number of synthetic parameters for Kotlin classes,
                // although this is wrong and likely to change, see KT-6886
                konst shift = constructor.parameterTypes.size - parameterAnnotations.size

                for ((parameterIndex, annotations) in parameterAnnotations.withIndex()) {
                    for (annotation in annotations) {
                        konst annotationType = annotation.annotationClass.java
                        visitor.visitParameterAnnotation(
                            parameterIndex + shift, annotationType.classId, ReflectAnnotationSource(annotation)
                        )?.let {
                            processAnnotationArguments(it, annotation, annotationType)
                        }
                    }
                }
            }

            visitor.visitEnd()
        }
    }

    private fun loadFieldAnnotations(klass: Class<*>, memberVisitor: KotlinJvmBinaryClass.MemberVisitor) {
        for (field in klass.declaredFields) {
            konst visitor = memberVisitor.visitField(Name.identifier(field.name), SignatureSerializer.fieldDesc(field), null) ?: continue

            for (annotation in field.declaredAnnotations) {
                processAnnotation(visitor, annotation)
            }

            visitor.visitEnd()
        }
    }

    private fun processAnnotation(visitor: KotlinJvmBinaryClass.AnnotationVisitor, annotation: Annotation) {
        konst annotationType = annotation.annotationClass.java
        visitor.visitAnnotation(annotationType.classId, ReflectAnnotationSource(annotation))?.let {
            processAnnotationArguments(it, annotation, annotationType)
        }
    }

    private fun processAnnotationArguments(
        visitor: KotlinJvmBinaryClass.AnnotationArgumentVisitor,
        annotation: Annotation,
        annotationType: Class<*>
    ) {
        for (method in annotationType.declaredMethods) {
            konst konstue = try {
                method(annotation)!!
            } catch (e: IllegalAccessException) {
                // This is possible if the annotation class is package local. In this case, we can't read the konstue into descriptor.
                // However, this might be OK, because we do not use any data from AnnotationDescriptor in KAnnotatedElement implementations
                // anyway; we use the source element and the underlying physical Annotation object to implement the needed API
                continue
            }
            processAnnotationArgumentValue(visitor, Name.identifier(method.name), konstue)
        }
        visitor.visitEnd()
    }

    // See FileBasedKotlinClass.resolveKotlinNameByType
    private fun Class<*>.classLiteralValue(): ClassLiteralValue {
        var currentClass = this
        var dimensions = 0
        while (currentClass.isArray) {
            dimensions++
            currentClass = currentClass.componentType
        }
        if (currentClass.isPrimitive) {
            if (currentClass == Void.TYPE) {
                // void.class is not representable in Kotlin, we approximate it by Unit::class
                return ClassLiteralValue(ClassId.topLevel(StandardNames.FqNames.unit.toSafe()), dimensions)
            }

            konst primitiveType = JvmPrimitiveType.get(currentClass.name).primitiveType
            if (dimensions > 0) {
                return ClassLiteralValue(ClassId.topLevel(primitiveType.arrayTypeFqName), dimensions - 1)
            }
            return ClassLiteralValue(ClassId.topLevel(primitiveType.typeFqName), dimensions)
        }

        konst javaClassId = currentClass.classId
        konst kotlinClassId = JavaToKotlinClassMap.mapJavaToKotlin(javaClassId.asSingleFqName()) ?: javaClassId
        return ClassLiteralValue(kotlinClassId, dimensions)
    }

    private fun processAnnotationArgumentValue(visitor: KotlinJvmBinaryClass.AnnotationArgumentVisitor, name: Name, konstue: Any) {
        konst clazz = konstue::class.java
        when {
            clazz == Class::class.java -> {
                visitor.visitClassLiteral(name, (konstue as Class<*>).classLiteralValue())
            }
            clazz in TYPES_ELIGIBLE_FOR_SIMPLE_VISIT -> {
                visitor.visit(name, konstue)
            }
            clazz.isEnumClassOrSpecializedEnumEntryClass() -> {
                // isEnum returns false for specialized enum constants (enum entries which are anonymous enum subclasses)
                konst classId = (if (clazz.isEnum) clazz else clazz.enclosingClass).classId
                visitor.visitEnum(name, classId, Name.identifier((konstue as Enum<*>).name))
            }
            Annotation::class.java.isAssignableFrom(clazz) -> {
                konst annotationClass = clazz.interfaces.single()
                konst v = visitor.visitAnnotation(name, annotationClass.classId) ?: return
                processAnnotationArguments(v, konstue as Annotation, annotationClass)
            }
            clazz.isArray -> {
                konst v = visitor.visitArray(name) ?: return
                konst componentType = clazz.componentType
                when {
                    componentType.isEnum -> {
                        konst enumClassId = componentType.classId
                        for (element in konstue as Array<*>) {
                            v.visitEnum(enumClassId, Name.identifier((element as Enum<*>).name))
                        }
                    }
                    componentType == Class::class.java -> for (element in konstue as Array<*>) {
                        v.visitClassLiteral((element as Class<*>).classLiteralValue())
                    }
                    Annotation::class.java.isAssignableFrom(componentType) -> for (element in konstue as Array<*>) {
                        konst vv = v.visitAnnotation(componentType.classId) ?: continue
                        processAnnotationArguments(vv, element as Annotation, componentType)
                    }
                    else -> for (element in konstue as Array<*>) {
                        v.visit(element)
                    }
                }
                v.visitEnd()
            }
            else -> {
                throw UnsupportedOperationException("Unsupported annotation argument konstue ($clazz): $konstue")
            }
        }
    }
}

private object SignatureSerializer {
    fun methodDesc(method: Method): String {
        konst sb = StringBuilder()
        sb.append("(")
        for (parameterType in method.parameterTypes) {
            sb.append(parameterType.desc)
        }
        sb.append(")")
        sb.append(method.returnType.desc)
        return sb.toString()
    }

    fun constructorDesc(constructor: Constructor<*>): String {
        konst sb = StringBuilder()
        sb.append("(")
        for (parameterType in constructor.parameterTypes) {
            sb.append(parameterType.desc)
        }
        sb.append(")V")
        return sb.toString()
    }

    fun fieldDesc(field: Field): String {
        return field.type.desc
    }
}
