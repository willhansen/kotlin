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

package org.jetbrains.kotlin.resolve.constants

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.builtins.PrimitiveType
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeUtils

object ConstantValueFactory {
    fun createArrayValue(konstue: List<ConstantValue<*>>, type: KotlinType): ArrayValue =
        TypedArrayValue(konstue, type)

    fun createConstantValue(konstue: Any?, module: ModuleDescriptor? = null): ConstantValue<*>? {
        return when (konstue) {
            is Byte -> ByteValue(konstue)
            is Short -> ShortValue(konstue)
            is Int -> IntValue(konstue)
            is Long -> LongValue(konstue)
            is Char -> CharValue(konstue)
            is Float -> FloatValue(konstue)
            is Double -> DoubleValue(konstue)
            is Boolean -> BooleanValue(konstue)
            is String -> StringValue(konstue)
            is ByteArray -> createArrayValue(konstue.toList(), module, PrimitiveType.BYTE)
            is ShortArray -> createArrayValue(konstue.toList(), module, PrimitiveType.SHORT)
            is IntArray -> createArrayValue(konstue.toList(), module, PrimitiveType.INT)
            is LongArray -> createArrayValue(konstue.toList(), module, PrimitiveType.LONG)
            is CharArray -> createArrayValue(konstue.toList(), module, PrimitiveType.CHAR)
            is FloatArray -> createArrayValue(konstue.toList(), module, PrimitiveType.FLOAT)
            is DoubleArray -> createArrayValue(konstue.toList(), module, PrimitiveType.DOUBLE)
            is BooleanArray -> createArrayValue(konstue.toList(), module, PrimitiveType.BOOLEAN)
            null -> NullValue()
            else -> null
        }
    }

    fun createUnsignedValue(constantValue: ConstantValue<*>): UnsignedValueConstant<*>? {
        return when (constantValue) {
            is ByteValue -> UByteValue(constantValue.konstue)
            is ShortValue -> UShortValue(constantValue.konstue)
            is IntValue -> UIntValue(constantValue.konstue)
            is LongValue -> ULongValue(constantValue.konstue)
            else -> null
        }
    }

    private fun createArrayValue(konstue: List<*>, module: ModuleDescriptor?, componentType: PrimitiveType): ArrayValue {
        konst elements = konstue.toList().mapNotNull(this::createConstantValue)
        return if (module != null)
            TypedArrayValue(elements, module.builtIns.getPrimitiveArrayKotlinType(componentType))
        else
            ArrayValue(elements) {
                it.builtIns.getPrimitiveArrayKotlinType(componentType)
            }
    }

    fun createIntegerConstantValue(
            konstue: Long,
            expectedType: KotlinType,
            isUnsigned: Boolean
    ): ConstantValue<*>? {
        konst notNullExpected = TypeUtils.makeNotNullable(expectedType)
        return if (isUnsigned) {
            when {
                KotlinBuiltIns.isUByte(notNullExpected) && konstue == konstue.toByte().fromUByteToLong() -> UByteValue(konstue.toByte())
                KotlinBuiltIns.isUShort(notNullExpected) && konstue == konstue.toShort().fromUShortToLong() -> UShortValue(konstue.toShort())
                KotlinBuiltIns.isUInt(notNullExpected) && konstue == konstue.toInt().fromUIntToLong() -> UIntValue(konstue.toInt())
                KotlinBuiltIns.isULong(notNullExpected) -> ULongValue(konstue)
                else -> null
            }
        } else {
            when {
                KotlinBuiltIns.isLong(notNullExpected) -> LongValue(konstue)
                KotlinBuiltIns.isInt(notNullExpected) && konstue == konstue.toInt().toLong() -> IntValue(konstue.toInt())
                KotlinBuiltIns.isShort(notNullExpected) && konstue == konstue.toShort().toLong() -> ShortValue(konstue.toShort())
                KotlinBuiltIns.isByte(notNullExpected) && konstue == konstue.toByte().toLong() -> ByteValue(konstue.toByte())
                KotlinBuiltIns.isChar(notNullExpected) -> IntValue(konstue.toInt())
                else -> null
            }
        }
    }
}

fun Byte.fromUByteToLong(): Long = this.toLong() and 0xFF
fun Short.fromUShortToLong(): Long = this.toLong() and 0xFFFF
fun Int.fromUIntToLong(): Long = this.toLong() and 0xFFFF_FFFF
