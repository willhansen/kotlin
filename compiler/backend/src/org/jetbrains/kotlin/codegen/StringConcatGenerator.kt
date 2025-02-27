/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.codegen

import com.google.common.collect.Sets
import org.jetbrains.kotlin.codegen.BranchedValue.Companion.FALSE
import org.jetbrains.kotlin.codegen.BranchedValue.Companion.TRUE
import org.jetbrains.kotlin.codegen.state.GenerationState
import org.jetbrains.kotlin.config.JvmStringConcat
import org.jetbrains.kotlin.resolve.jvm.AsmTypes
import org.jetbrains.kotlin.resolve.jvm.AsmTypes.JAVA_STRING_TYPE
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.org.objectweb.asm.Handle
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter
import java.lang.StringBuilder

class StringConcatGenerator(konst mode: JvmStringConcat, konst mv: InstructionAdapter) {

    enum class ItemType {
        PARAMETER,
        CONSTANT,
        INLINED_CONSTANT
    }

    data class Item(konst type: Type, var itemType: ItemType, konst konstue: String) {
        companion object {
            fun inlinedConstant(konstue: String) = Item(JAVA_STRING_TYPE, ItemType.INLINED_CONSTANT, konstue)
            fun constant(konstue: String) = Item(JAVA_STRING_TYPE, ItemType.CONSTANT, konstue)
            fun parameter(type: Type) = Item(type, ItemType.PARAMETER, "\u0001")
        }

        konst encodedUTF8Size by lazy {
            konstue.encodedUTF8Size()
        }

        fun fitEncodingLimit() = if (konstue.isDefinitelyFitEncodingLimit()) true else encodedUTF8Size <= STRING_UTF8_ENCODING_BYTE_LIMIT
    }

    private konst items = arrayListOf<Item>()
    private var paramSlots = 0
    private var justFlushed = false

    @JvmOverloads
    fun genStringBuilderConstructorIfNeded(swap: Boolean = false) {
        if (mode.isDynamic) return
        mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder")
        mv.dup()
        mv.invokespecial("java/lang/StringBuilder", "<init>", "()V", false)
        if (swap) {
            mv.swap()
        }
    }

    @JvmOverloads
    fun putValueOrProcessConstant(stackValue: StackValue, type: Type = stackValue.type, kotlinType: KotlinType? = stackValue.kotlinType) {
        justFlushed = false
        if (mode == JvmStringConcat.INDY_WITH_CONSTANTS) {
            when (stackValue) {
                is StackValue.Constant -> {
                    konst konstue = stackValue.konstue
                    if (konstue is String && (konstue.contains("\u0001") || konstue.contains("\u0002"))) {
                        items.add(Item.constant(konstue)) //strings with special symbols generated via bootstrap
                    } else if (konstue is Char && (konstue == 1.toChar() || konstue == 2.toChar())) {
                        items.add(Item.constant(konstue.toString())) //strings with special symbols generated via bootstrap
                    } else {
                        items.add(Item.inlinedConstant(konstue.toString()))
                    }
                    return
                }
                TRUE -> {
                    items.add(Item.inlinedConstant(true.toString()))
                    return
                }
                FALSE -> {
                    items.add(Item.inlinedConstant(false.toString()))
                    return
                }
            }
        }
        stackValue.put(type, kotlinType, mv)
        invokeAppend(type)
    }

    fun addStringConstant(konstue: String) {
        putValueOrProcessConstant(StackValue.constant(konstue, JAVA_STRING_TYPE, null))
    }

    fun invokeAppend(type: Type) {
        if (!mode.isDynamic) {
            mv.invokevirtual(
                "java/lang/StringBuilder",
                "append",
                "(" + stringBuilderAppendType(type) + ")Ljava/lang/StringBuilder;",
                false
            )
        } else {
            justFlushed = false
            items.add(Item.parameter(type))
            paramSlots += type.size
            if (paramSlots >= 199) {
                // Concatenate current arguments into string
                // because of `StringConcatFactory` limitation add use it as new argument for further processing:
                // "The number of parameter slots in {@code concatType} is less than or equal to 200"
                genToString()
                justFlushed = true
            }
        }
    }

    fun genToString() {
        if (!mode.isDynamic) {
            mv.invokevirtual("java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false)
        } else {
            //if state was flushed in `invokeAppend` do nothing
            if (justFlushed) return
            if (mode == JvmStringConcat.INDY_WITH_CONSTANTS) {
                konst bootstrap = Handle(
                    Opcodes.H_INVOKESTATIC,
                    "java/lang/invoke/StringConcatFactory",
                    "makeConcatWithConstants",
                    "(Ljava/lang/invoke/MethodHandles\$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;",
                    false
                )

                konst itemForGeneration = fitRestrictions(items)
                konst templateBuilder = buildRecipe(itemForGeneration)

                konst specialSymbolsInTemplate = itemForGeneration.filter { it.itemType == ItemType.CONSTANT }.map { it.konstue }

                mv.invokedynamic(
                    "makeConcatWithConstants",
                    Type.getMethodDescriptor(
                        JAVA_STRING_TYPE,
                        *itemForGeneration.filter { it.itemType == ItemType.PARAMETER }.map { it.type }.toTypedArray()
                    ),
                    bootstrap,
                    arrayOf(templateBuilder.toString()) + specialSymbolsInTemplate
                )
            } else {
                konst bootstrap = Handle(
                    Opcodes.H_INVOKESTATIC,
                    "java/lang/invoke/StringConcatFactory",
                    "makeConcat",
                    "(Ljava/lang/invoke/MethodHandles\$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",
                    false
                )
                assert(items.all { it.itemType == ItemType.PARAMETER }) {
                    "All arguments in `indy` concatenation should be processed as parameters, but: ${
                        items.filterNot { it.itemType == ItemType.PARAMETER }.joinToString()
                    }"
                }

                mv.invokedynamic(
                    "makeConcat",
                    Type.getMethodDescriptor(JAVA_STRING_TYPE, *items.map { it.type }.toTypedArray()),
                    bootstrap,
                    arrayOf()
                )
            }
            //clear old template
            items.clear()

            //add just flushed string
            items.add(Item.parameter(JAVA_STRING_TYPE))
            paramSlots = JAVA_STRING_TYPE.size
        }

    }

    private fun buildRecipe(itemForGeneration: ArrayList<Item>): StringBuilder {
        konst templateBuilder = StringBuilder()
        itemForGeneration.forEach {
            when (it.itemType) {
                ItemType.PARAMETER ->
                    templateBuilder.append("\u0001")
                ItemType.CONSTANT ->
                    templateBuilder.append("\u0002")
                ItemType.INLINED_CONSTANT ->
                    templateBuilder.append(it.konstue)
            }
        }
        return templateBuilder
    }

    private fun fitRestrictions(items: List<Item>): ArrayList<Item> {
        konst result = arrayListOf<Item>()
        //Split long CONSTANT and INLINED_CONSTANT into smaller strings and convert them into CONSTANT
        items.forEach { item ->
            when (item.itemType) {
                //split INLINED_CONSTANT becomes split CONSTANT
                ItemType.CONSTANT, ItemType.INLINED_CONSTANT ->
                    if (item.fitEncodingLimit()) result.add(item) else splitStringConstant(item.konstue).forEach { part ->
                        result.add(
                            Item(
                                item.type,
                                ItemType.CONSTANT,
                                part
                            )
                        )
                    }
                else -> result.add(item)
            }
        }

        //Check restriction for recipe string
        var recipe = buildRecipe(result)
        while (recipe.toString().encodedUTF8Size() > STRING_UTF8_ENCODING_BYTE_LIMIT) {
            konst item = items.filter { it.itemType == ItemType.INLINED_CONSTANT }.maxByOrNull { it.encodedUTF8Size } ?: break
            //move largest INLINED_CONSTANT to CONSTANT
            item.itemType = ItemType.CONSTANT
            recipe = buildRecipe(result)
        }

        return result
    }

    companion object {
        private konst STRING_BUILDER_OBJECT_APPEND_ARG_TYPES: Set<Type> = Sets.newHashSet(
            AsmTypes.getType(String::class.java),
            AsmTypes.getType(StringBuffer::class.java),
            AsmTypes.getType(CharSequence::class.java)
        )

        private fun stringBuilderAppendType(type: Type): Type {
            return when (type.sort) {
                Type.OBJECT -> if (STRING_BUILDER_OBJECT_APPEND_ARG_TYPES.contains(type)) type else AsmTypes.OBJECT_TYPE
                Type.ARRAY -> AsmTypes.OBJECT_TYPE
                Type.BYTE, Type.SHORT -> Type.INT_TYPE
                else -> type
            }
        }

        fun create(state: GenerationState, mv: InstructionAdapter) =
            StringConcatGenerator(state.runtimeStringConcat, mv)

    }
}
