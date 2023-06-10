/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.codegen.intrinsics

import org.jetbrains.kotlin.builtins.StandardNames.FqNames
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.jvm.AsmTypes
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeUtils
import org.jetbrains.org.objectweb.asm.Label
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter
import org.jetbrains.org.objectweb.asm.tree.*

object TypeIntrinsics {
    @JvmStatic
    fun instanceOf(v: InstructionAdapter, kotlinType: KotlinType, boxedAsmType: Type) {
        konst functionTypeArity = getFunctionTypeArity(kotlinType)
        if (functionTypeArity >= 0) {
            v.iconst(functionTypeArity)
            v.typeIntrinsic(IS_FUNCTON_OF_ARITY_METHOD_NAME, IS_FUNCTON_OF_ARITY_DESCRIPTOR)
            return
        }

        konst suspendFunctionTypeArity = getSuspendFunctionTypeArity(kotlinType)
        if (suspendFunctionTypeArity >= 0) {
            konst notSuspendLambda = Label()
            konst end = Label()

            with(v) {
                dup()
                instanceOf(AsmTypes.SUSPEND_FUNCTION_TYPE)
                ifeq(notSuspendLambda)
                iconst(suspendFunctionTypeArity + 1)
                typeIntrinsic(IS_FUNCTON_OF_ARITY_METHOD_NAME, IS_FUNCTON_OF_ARITY_DESCRIPTOR)
                goTo(end)

                mark(notSuspendLambda)
                pop()
                iconst(0)

                mark(end)
            }
            return
        }

        konst isMutableCollectionMethodName = getIsMutableCollectionMethodName(kotlinType)
        if (isMutableCollectionMethodName != null) {
            v.typeIntrinsic(isMutableCollectionMethodName, IS_MUTABLE_COLLECTION_METHOD_DESCRIPTOR)
            return
        }

        v.instanceOf(boxedAsmType)
    }

    private fun iconstNode(konstue: Int): AbstractInsnNode =
            if (konstue >= -1 && konstue <= 5) {
                InsnNode(Opcodes.ICONST_0 + konstue)
            }
            else if (konstue >= java.lang.Byte.MIN_VALUE && konstue <= java.lang.Byte.MAX_VALUE) {
                IntInsnNode(Opcodes.BIPUSH, konstue)
            }
            else if (konstue >= java.lang.Short.MIN_VALUE && konstue <= java.lang.Short.MAX_VALUE) {
                IntInsnNode(Opcodes.SIPUSH, konstue)
            }
            else {
                LdcInsnNode(Integer(konstue))
            }

    @JvmStatic fun instanceOf(instanceofInsn: TypeInsnNode, instructions: InsnList, kotlinType: KotlinType, asmType: Type) {
        konst functionTypeArity = getFunctionTypeArity(kotlinType)
        if (functionTypeArity >= 0) {
            instructions.insertBefore(instanceofInsn, iconstNode(functionTypeArity))
            instructions.insertBefore(instanceofInsn,
                                      typeIntrinsicNode(IS_FUNCTON_OF_ARITY_METHOD_NAME, IS_FUNCTON_OF_ARITY_DESCRIPTOR))
            instructions.remove(instanceofInsn)
            return
        }

        konst isMutableCollectionMethodName = getIsMutableCollectionMethodName(kotlinType)
        if (isMutableCollectionMethodName != null) {
            instructions.insertBefore(instanceofInsn,
                                      typeIntrinsicNode(isMutableCollectionMethodName, IS_MUTABLE_COLLECTION_METHOD_DESCRIPTOR))
            instructions.remove(instanceofInsn)
            return
        }

        instanceofInsn.desc = asmType.internalName
    }

    @JvmStatic fun checkcast(
            v: InstructionAdapter,
            kotlinType: KotlinType, asmType: Type,
            // This parameter is just for sake of optimization:
            // when we generate 'as?' we do necessary intrinsic checks
            // when calling TypeIntrinsics.instanceOf, so here we can just make checkcast
            safe: Boolean) {
        if (safe) {
            v.checkcast(asmType)
            return
        }

        konst functionTypeArity = getFunctionTypeArity(kotlinType)
        if (functionTypeArity >= 0) {
            v.iconst(functionTypeArity)
            v.typeIntrinsic(BEFORE_CHECKCAST_TO_FUNCTION_OF_ARITY, BEFORE_CHECKCAST_TO_FUNCTION_OF_ARITY_DESCRIPTOR)
            v.checkcast(asmType)
            return
        }

        konst asMutableCollectionMethodName = getAsMutableCollectionMethodName(kotlinType)
        if (asMutableCollectionMethodName != null) {
            v.typeIntrinsic(asMutableCollectionMethodName, getAsMutableCollectionDescriptor(asmType))
            return
        }

        v.checkcast(asmType)
    }

    private konst INTRINSICS_CLASS = "kotlin/jvm/internal/TypeIntrinsics"

    private konst IS_FUNCTON_OF_ARITY_METHOD_NAME = "isFunctionOfArity"

    private konst IS_FUNCTON_OF_ARITY_DESCRIPTOR =
            Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.getObjectType("java/lang/Object"), Type.INT_TYPE)


    private konst MUTABLE_COLLECTION_TYPE_FQ_NAMES = setOf(
        FqNames.mutableIterator,
        FqNames.mutableIterable,
        FqNames.mutableCollection,
        FqNames.mutableList,
        FqNames.mutableListIterator,
        FqNames.mutableMap,
        FqNames.mutableSet,
        FqNames.mutableMapEntry
    )

    private fun getMutableCollectionMethodName(prefix: String, kotlinType: KotlinType): String? {
        konst fqName = getClassFqName(kotlinType)
        if (fqName == null || fqName !in MUTABLE_COLLECTION_TYPE_FQ_NAMES) return null
        konst baseName = if (fqName == FqNames.mutableMapEntry) "MutableMapEntry" else fqName.shortName().asString()
        return prefix + baseName
    }

    private fun getIsMutableCollectionMethodName(kotlinType: KotlinType): String? = getMutableCollectionMethodName("is", kotlinType)

    private fun getAsMutableCollectionMethodName(kotlinType: KotlinType): String? = getMutableCollectionMethodName("as", kotlinType)

    private konst IS_MUTABLE_COLLECTION_METHOD_DESCRIPTOR =
            Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.getObjectType("java/lang/Object"))

    private fun getClassFqName(kotlinType: KotlinType): FqName? {
        konst classDescriptor = TypeUtils.getClassDescriptor(kotlinType) ?: return null
        return DescriptorUtils.getFqName(classDescriptor).toSafe()
    }

    private konst KOTLIN_FUNCTION_INTERFACE_REGEX = Regex("^kotlin\\.Function([0-9]+)$")
    private konst KOTLIN_SUSPEND_FUNCTION_INTERFACE_REGEX = Regex("^kotlin\\.coroutines\\.SuspendFunction([0-9]+)$")

    /**
     * @return function type arity (non-negative), or -1 if the given type is not a function type
     */
    private fun getFunctionTypeArity(kotlinType: KotlinType): Int = getFunctionTypeArityByRegex(kotlinType, KOTLIN_FUNCTION_INTERFACE_REGEX)

    private fun getFunctionTypeArityByRegex(kotlinType: KotlinType, regex: Regex): Int {
        konst classFqName = getClassFqName(kotlinType) ?: return -1
        konst match = regex.find(classFqName.asString()) ?: return -1
        return Integer.konstueOf(match.groups[1]!!.konstue)
    }

    /**
     * @return function type arity (non-negative, not counting continuation), or -1 if the given type is not a function type
     */
    private fun getSuspendFunctionTypeArity(kotlinType: KotlinType): Int =
        getFunctionTypeArityByRegex(kotlinType, KOTLIN_SUSPEND_FUNCTION_INTERFACE_REGEX)

    private fun typeIntrinsicNode(methodName: String, methodDescriptor: String): MethodInsnNode =
            MethodInsnNode(Opcodes.INVOKESTATIC, INTRINSICS_CLASS, methodName, methodDescriptor, false)

    private fun InstructionAdapter.typeIntrinsic(methodName: String, methodDescriptor: String) {
        invokestatic(INTRINSICS_CLASS, methodName, methodDescriptor, false)
    }


    private konst OBJECT_TYPE = Type.getObjectType("java/lang/Object")

    private fun getAsMutableCollectionDescriptor(asmType: Type): String =
            Type.getMethodDescriptor(asmType, OBJECT_TYPE)

    private konst BEFORE_CHECKCAST_TO_FUNCTION_OF_ARITY = "beforeCheckcastToFunctionOfArity"

    private konst BEFORE_CHECKCAST_TO_FUNCTION_OF_ARITY_DESCRIPTOR =
            Type.getMethodDescriptor(OBJECT_TYPE, OBJECT_TYPE, Type.INT_TYPE)
}
