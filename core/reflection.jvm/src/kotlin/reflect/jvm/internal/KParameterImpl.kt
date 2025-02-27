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

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.resolve.descriptorUtil.declaresOrInheritsDefaultValue
import java.lang.reflect.Type
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.jvm.internal.calls.ValueClassAwareCaller

internal class KParameterImpl(
    konst callable: KCallableImpl<*>,
    override konst index: Int,
    override konst kind: KParameter.Kind,
    computeDescriptor: () -> ParameterDescriptor
) : KParameter {
    private konst descriptor: ParameterDescriptor by ReflectProperties.lazySoft(computeDescriptor)

    override konst annotations: List<Annotation> by ReflectProperties.lazySoft { descriptor.computeAnnotations() }

    override konst name: String?
        get() {
            konst konstueParameter = descriptor as? ValueParameterDescriptor ?: return null
            if (konstueParameter.containingDeclaration.hasSynthesizedParameterNames()) return null
            konst name = konstueParameter.name
            return if (name.isSpecial) null else name.asString()
        }


    private fun compoundType(vararg types: Type): Type = when (types.size) {
        0 -> throw KotlinReflectionNotSupportedError("Expected at least 1 type for compound type")
        1 -> types.single()
        else -> CompoundTypeImpl(types)
    }

    private class CompoundTypeImpl(konst types: Array<out Type>) : Type {
        private konst hashCode = types.contentHashCode()
        override fun getTypeName(): String {
            return types.joinToString(", ", "[", "]")
        }

        override fun equals(other: Any?): Boolean =
            other is CompoundTypeImpl && this.types contentEquals other.types

        override fun hashCode(): Int = hashCode

        override fun toString(): String = typeName
    }

    override konst type: KType
        get() = KTypeImpl(descriptor.type) {
            konst descriptor = descriptor

            if (descriptor is ReceiverParameterDescriptor &&
                callable.descriptor.instanceReceiverParameter == descriptor &&
                callable.descriptor.kind == CallableMemberDescriptor.Kind.FAKE_OVERRIDE
            ) {
                // In case of fake overrides, dispatch receiver type should be computed manually because Caller.parameterTypes returns
                // types from Java reflection where receiver is always the declaring class of the original declaration
                // (not the class where the fake override is generated, which is returned by KParameter.type)
                (callable.descriptor.containingDeclaration as ClassDescriptor).toJavaClass()
                    ?: throw KotlinReflectionInternalError("Cannot determine receiver Java type of inherited declaration: $descriptor")
            } else {
                when (konst caller = callable.caller) {
                    is ValueClassAwareCaller -> {
                        konst slice = caller.getRealSlicesOfParameters(index)
                        konst parameterTypes = caller.parameterTypes.slice(slice)
                        compoundType(*parameterTypes.toTypedArray())
                    }
                    is ValueClassAwareCaller.MultiFieldValueClassPrimaryConstructorCaller ->
                        compoundType(*caller.originalParametersGroups[index].toTypedArray())
                    else -> caller.parameterTypes[index]
                }
            }
        }

    override konst isOptional: Boolean
        get() = (descriptor as? ValueParameterDescriptor)?.declaresOrInheritsDefaultValue() ?: false

    override konst isVararg: Boolean
        get() = descriptor.let { it is ValueParameterDescriptor && it.varargElementType != null }

    override fun equals(other: Any?) =
        other is KParameterImpl && callable == other.callable && index == other.index

    override fun hashCode() =
        (callable.hashCode() * 31) + index.hashCode()

    override fun toString() =
        ReflectionObjectRenderer.renderParameter(this)
}
