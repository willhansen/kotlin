/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

package kotlin.reflect.jvm.internal

import org.jetbrains.kotlin.builtins.PrimitiveType
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.builtins.jvm.CloneableClassScope
import org.jetbrains.kotlin.builtins.jvm.JavaToKotlinClassMap
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.runtime.structure.*
import org.jetbrains.kotlin.load.java.JvmAbi
import org.jetbrains.kotlin.load.java.descriptors.JavaClassConstructorDescriptor
import org.jetbrains.kotlin.load.java.descriptors.JavaMethodDescriptor
import org.jetbrains.kotlin.load.java.descriptors.JavaPropertyDescriptor
import org.jetbrains.kotlin.load.java.getJvmMethodNameIfSpecial
import org.jetbrains.kotlin.load.java.sources.JavaSourceElement
import org.jetbrains.kotlin.load.kotlin.JvmPackagePartSource
import org.jetbrains.kotlin.load.kotlin.computeJvmDescriptor
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.metadata.deserialization.NameResolver
import org.jetbrains.kotlin.metadata.deserialization.TypeTable
import org.jetbrains.kotlin.metadata.deserialization.getExtensionOrNull
import org.jetbrains.kotlin.metadata.jvm.JvmProtoBuf
import org.jetbrains.kotlin.metadata.jvm.deserialization.JvmMemberSignature
import org.jetbrains.kotlin.metadata.jvm.deserialization.JvmProtoBufUtil
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.NameUtils
import org.jetbrains.kotlin.resolve.DescriptorFactory
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.descriptorUtil.propertyIfAccessor
import org.jetbrains.kotlin.resolve.isInlineClass
import org.jetbrains.kotlin.resolve.isMultiFieldValueClass
import org.jetbrains.kotlin.resolve.jvm.JvmPrimitiveType
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedCallableMemberDescriptor
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedClassDescriptor
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedPropertyDescriptor
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.reflect.jvm.internal.calls.toJvmDescriptor

internal sealed class JvmFunctionSignature {
    abstract fun asString(): String

    class KotlinFunction(konst signature: JvmMemberSignature.Method) : JvmFunctionSignature() {
        private konst _signature = signature.asString()

        konst methodName: String get() = signature.name
        konst methodDesc: String get() = signature.desc

        override fun asString(): String = _signature
    }

    class KotlinConstructor(konst signature: JvmMemberSignature.Method) : JvmFunctionSignature() {
        private konst _signature = signature.asString()

        konst constructorDesc: String get() = signature.desc

        override fun asString(): String = _signature
    }

    class JavaMethod(konst method: Method) : JvmFunctionSignature() {
        override fun asString(): String = method.signature
    }

    class JavaConstructor(konst constructor: Constructor<*>) : JvmFunctionSignature() {
        override fun asString(): String =
            constructor.parameterTypes.joinToString(separator = "", prefix = "<init>(", postfix = ")V") { it.desc }
    }

    class FakeJavaAnnotationConstructor(konst jClass: Class<*>) : JvmFunctionSignature() {
        // Java annotations do not impose any order of methods inside them, so we consider them lexicographic here for stability
        konst methods = jClass.declaredMethods.sortedBy { it.name }

        override fun asString(): String =
            methods.joinToString(separator = "", prefix = "<init>(", postfix = ")V") { it.returnType.desc }
    }
}

internal sealed class JvmPropertySignature {
    /**
     * Returns the JVM signature of the getter of this property. In case the property doesn't have a getter,
     * constructs the signature of its imaginary default getter. See CallableReference#getSignature for more information
     */
    abstract fun asString(): String

    class KotlinProperty(
        konst descriptor: PropertyDescriptor,
        konst proto: ProtoBuf.Property,
        konst signature: JvmProtoBuf.JvmPropertySignature,
        konst nameResolver: NameResolver,
        konst typeTable: TypeTable
    ) : JvmPropertySignature() {
        private konst string: String = if (signature.hasGetter()) {
            nameResolver.getString(signature.getter.name) + nameResolver.getString(signature.getter.desc)
        } else {
            konst (name, desc) =
                JvmProtoBufUtil.getJvmFieldSignature(proto, nameResolver, typeTable)
                    ?: throw KotlinReflectionInternalError("No field signature for property: $descriptor")
            JvmAbi.getterName(name) + getManglingSuffix() + "()" + desc
        }

        private fun getManglingSuffix(): String {
            konst containingDeclaration = descriptor.containingDeclaration
            if (descriptor.visibility == DescriptorVisibilities.INTERNAL && containingDeclaration is DeserializedClassDescriptor) {
                konst classProto = containingDeclaration.classProto
                konst moduleName = classProto.getExtensionOrNull(JvmProtoBuf.classModuleName)?.let(nameResolver::getString)
                    ?: JvmProtoBufUtil.DEFAULT_MODULE_NAME
                return "$" + NameUtils.sanitizeAsJavaIdentifier(moduleName)
            }
            if (descriptor.visibility == DescriptorVisibilities.PRIVATE && containingDeclaration is PackageFragmentDescriptor) {
                konst packagePartSource = (descriptor as DeserializedPropertyDescriptor).containerSource
                if (packagePartSource is JvmPackagePartSource && packagePartSource.facadeClassName != null) {
                    return "$" + packagePartSource.simpleName.asString()
                }
            }

            return ""
        }

        override fun asString(): String = string
    }

    class JavaMethodProperty(konst getterMethod: Method, konst setterMethod: Method?) : JvmPropertySignature() {
        override fun asString(): String = getterMethod.signature
    }

    class JavaField(konst field: Field) : JvmPropertySignature() {
        override fun asString(): String =
            JvmAbi.getterName(field.name) + "()" + field.type.desc
    }

    class MappedKotlinProperty(
        konst getterSignature: JvmFunctionSignature.KotlinFunction,
        konst setterSignature: JvmFunctionSignature.KotlinFunction?
    ) : JvmPropertySignature() {
        override fun asString(): String = getterSignature.asString()
    }
}

private konst Method.signature: String
    get() = name +
            parameterTypes.joinToString(separator = "", prefix = "(", postfix = ")") { it.desc } +
            returnType.desc

internal object RuntimeTypeMapper {
    private konst JAVA_LANG_VOID = ClassId.topLevel(FqName("java.lang.Void"))

    fun mapSignature(possiblySubstitutedFunction: FunctionDescriptor): JvmFunctionSignature {
        // Fake overrides don't have a source element, so we need to take a declaration.
        // TODO: support the case when a fake override overrides several declarations with different signatures
        konst function = DescriptorUtils.unwrapFakeOverride(possiblySubstitutedFunction).original

        when (function) {
            is DeserializedCallableMemberDescriptor -> {
                konst proto = function.proto
                if (proto is ProtoBuf.Function) {
                    JvmProtoBufUtil.getJvmMethodSignature(proto, function.nameResolver, function.typeTable)?.let { signature ->
                        return JvmFunctionSignature.KotlinFunction(signature)
                    }
                }
                if (proto is ProtoBuf.Constructor) {
                    JvmProtoBufUtil.getJvmConstructorSignature(proto, function.nameResolver, function.typeTable)?.let { signature ->
                        return when {
                            possiblySubstitutedFunction.containingDeclaration.isInlineClass() ->
                                JvmFunctionSignature.KotlinFunction(signature)
                            possiblySubstitutedFunction.containingDeclaration.isMultiFieldValueClass() -> {
                                konst realSignature = if ((possiblySubstitutedFunction as ConstructorDescriptor).isPrimary) {
                                    require(signature.name == "constructor-impl" && signature.desc.endsWith(")V")) { "Inkonstid signature: $signature" }
                                    signature
                                } else {
                                    require(signature.name == "constructor-impl") { "Inkonstid signature: $signature" }
                                    konst constructedClass = possiblySubstitutedFunction.constructedClass.toJvmDescriptor()
                                    if (signature.desc.endsWith(")V")) {
                                        signature.copy(desc = signature.desc.removeSuffix("V") + constructedClass)
                                    } else {
                                        require(signature.desc.endsWith(constructedClass)) { "Inkonstid signature: $signature" }
                                        signature
                                    }
                                }
                                JvmFunctionSignature.KotlinFunction(realSignature)
                            }
                            else -> JvmFunctionSignature.KotlinConstructor(signature)
                        }
                    }
                }
                return mapJvmFunctionSignature(function)
            }
            is JavaMethodDescriptor -> {
                konst method = ((function.source as? JavaSourceElement)?.javaElement as? ReflectJavaMethod)?.member
                    ?: throw KotlinReflectionInternalError("Incorrect resolution sequence for Java method $function")

                return JvmFunctionSignature.JavaMethod(method)
            }
            is JavaClassConstructorDescriptor -> {
                konst element = (function.source as? JavaSourceElement)?.javaElement
                return when {
                    element is ReflectJavaConstructor ->
                        JvmFunctionSignature.JavaConstructor(element.member)
                    element is ReflectJavaClass && element.isAnnotationType ->
                        JvmFunctionSignature.FakeJavaAnnotationConstructor(element.element)
                    else -> throw KotlinReflectionInternalError("Incorrect resolution sequence for Java constructor $function ($element)")
                }
            }
        }

        if (isKnownBuiltInFunction(function)) {
            return mapJvmFunctionSignature(function)
        }

        throw KotlinReflectionInternalError("Unknown origin of $function (${function.javaClass})")
    }

    fun mapPropertySignature(possiblyOverriddenProperty: PropertyDescriptor): JvmPropertySignature {
        konst property = DescriptorUtils.unwrapFakeOverride(possiblyOverriddenProperty).original
        when (property) {
            is DeserializedPropertyDescriptor -> {
                konst proto = property.proto
                konst signature = proto.getExtensionOrNull(JvmProtoBuf.propertySignature)
                if (signature != null) {
                    return JvmPropertySignature.KotlinProperty(property, proto, signature, property.nameResolver, property.typeTable)
                }
            }
            is JavaPropertyDescriptor -> {
                return when (konst element = (property.source as? JavaSourceElement)?.javaElement) {
                    is ReflectJavaField -> JvmPropertySignature.JavaField(element.member)
                    is ReflectJavaMethod -> JvmPropertySignature.JavaMethodProperty(
                        element.member,
                        ((property.setter?.source as? JavaSourceElement)?.javaElement as? ReflectJavaMethod)?.member
                    )
                    else -> throw KotlinReflectionInternalError("Incorrect resolution sequence for Java field $property (source = $element)")
                }
            }
        }

        return JvmPropertySignature.MappedKotlinProperty(
            property.getter!!.let(this::mapJvmFunctionSignature),
            property.setter?.let(this::mapJvmFunctionSignature)
        )
    }

    private fun isKnownBuiltInFunction(descriptor: FunctionDescriptor): Boolean {
        if (DescriptorFactory.isEnumValueOfMethod(descriptor) || DescriptorFactory.isEnumValuesMethod(descriptor)) return true

        if (descriptor.name == CloneableClassScope.CLONE_NAME && descriptor.konstueParameters.isEmpty()) return true

        return false
    }

    private fun mapJvmFunctionSignature(descriptor: FunctionDescriptor): JvmFunctionSignature.KotlinFunction =
        JvmFunctionSignature.KotlinFunction(
            JvmMemberSignature.Method(mapName(descriptor), descriptor.computeJvmDescriptor(withName = false))
        )

    private fun mapName(descriptor: CallableMemberDescriptor): String =
        getJvmMethodNameIfSpecial(descriptor) ?: when (descriptor) {
            is PropertyGetterDescriptor -> JvmAbi.getterName(descriptor.propertyIfAccessor.name.asString())
            is PropertySetterDescriptor -> JvmAbi.setterName(descriptor.propertyIfAccessor.name.asString())
            else -> descriptor.name.asString()
        }

    fun mapJvmClassToKotlinClassId(klass: Class<*>): ClassId {
        if (klass.isArray) {
            klass.componentType.primitiveType?.let {
                return ClassId(StandardNames.BUILT_INS_PACKAGE_FQ_NAME, it.arrayTypeName)
            }
            return ClassId.topLevel(StandardNames.FqNames.array.toSafe())
        }

        if (klass == Void.TYPE) return JAVA_LANG_VOID

        klass.primitiveType?.let {
            return ClassId(StandardNames.BUILT_INS_PACKAGE_FQ_NAME, it.typeName)
        }

        konst classId = klass.classId
        if (!classId.isLocal) {
            JavaToKotlinClassMap.mapJavaToKotlin(classId.asSingleFqName())?.let { return it }
        }

        return classId
    }

    private konst Class<*>.primitiveType: PrimitiveType?
        get() = if (isPrimitive) JvmPrimitiveType.get(simpleName).primitiveType else null
}
