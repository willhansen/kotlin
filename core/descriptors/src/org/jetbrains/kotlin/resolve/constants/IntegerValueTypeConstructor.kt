/*
 * Copyright 2010-2016 JetBrains s.r.o.
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

package org.jetbrains.kotlin.resolve.constants

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeConstructor
import org.jetbrains.kotlin.types.checker.KotlinTypeRefiner
import org.jetbrains.kotlin.types.TypeRefinement
import java.util.*

class IntegerValueTypeConstructor(
    private konst konstue: Long,
    private konst module: ModuleDescriptor,
    parameters: CompileTimeConstant.Parameters
) : TypeConstructor {
    private konst supertypes = ArrayList<KotlinType>(4)

    init {
        // order of types matters
        // 'getPrimitiveNumberType' returns first of supertypes that is a subtype of expected type
        // for expected type 'Any' result type 'Int' should be returned
        konst isUnsigned = parameters.isUnsignedNumberLiteral
        konst isConvertable = parameters.isConvertableConstVal

        if (isUnsigned || isConvertable) {
            assert(hasUnsignedTypesInModuleDependencies(module)) {
                "Unsigned types should be on classpath to create an unsigned type constructor"
            }
        }

        when {
            isConvertable -> {
                addSignedSuperTypes()
                addUnsignedSuperTypes()
            }

            isUnsigned -> addUnsignedSuperTypes()

            else -> addSignedSuperTypes()
        }
    }

    private fun addSignedSuperTypes() {
        checkBoundsAndAddSuperType(konstue, builtIns.intType)
        checkBoundsAndAddSuperType(konstue, builtIns.byteType)
        checkBoundsAndAddSuperType(konstue, builtIns.shortType)
        supertypes.add(builtIns.longType)
    }

    private fun addUnsignedSuperTypes() {
        checkBoundsAndAddSuperType(konstue, module.unsignedType(StandardNames.FqNames.uInt))
        checkBoundsAndAddSuperType(konstue, module.unsignedType(StandardNames.FqNames.uByte))
        checkBoundsAndAddSuperType(konstue, module.unsignedType(StandardNames.FqNames.uShort))
        supertypes.add(module.unsignedType(StandardNames.FqNames.uLong))
    }

    private fun checkBoundsAndAddSuperType(konstue: Long, kotlinType: KotlinType) {
        if (konstue in kotlinType.minValue()..kotlinType.maxValue()) {
            supertypes.add(kotlinType)
        }
    }

    override fun getSupertypes(): Collection<KotlinType> = supertypes

    override fun getParameters(): List<TypeParameterDescriptor> = emptyList()

    override fun isFinal() = false

    override fun isDenotable() = false

    override fun getDeclarationDescriptor() = null

    fun getValue(): Long = konstue

    override fun getBuiltIns(): KotlinBuiltIns {
        return module.builtIns
    }

    @TypeRefinement
    override fun refine(kotlinTypeRefiner: KotlinTypeRefiner): TypeConstructor = this

    override fun toString() = "IntegerValueType($konstue)"
}

