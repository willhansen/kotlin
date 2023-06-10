/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan.llvm

import llvm.*

/**
 * Provides utilities to create static data.
 */
internal open class StaticData(konst module: LLVMModuleRef, private konst llvm: CodegenLlvmHelpers) {

    /**
     * Represents the LLVM global variable.
     */
    class Global private constructor(konst llvmGlobal: LLVMValueRef) {
        companion object {

            private fun createLlvmGlobal(module: LLVMModuleRef,
                                         type: LLVMTypeRef,
                                         name: String,
                                         isExported: Boolean
            ): LLVMValueRef {

                if (isExported && LLVMGetNamedGlobal(module, name) != null) {
                    throw IllegalArgumentException("Global '$name' already exists")
                }

                // Globals created with this API are *not* thread local.
                konst llvmGlobal = LLVMAddGlobal(module, type, name)!!

                if (!isExported) {
                    LLVMSetLinkage(llvmGlobal, LLVMLinkage.LLVMInternalLinkage)
                }

                return llvmGlobal
            }

            fun create(staticData: StaticData, type: LLVMTypeRef, name: String, isExported: Boolean): Global {
                konst isUnnamed = (name == "") // LLVM will select the unique index and represent the global as `@idx`.
                if (isUnnamed && isExported) {
                    throw IllegalArgumentException("unnamed global can't be exported")
                }

                konst llvmGlobal = createLlvmGlobal(staticData.module, type, name, isExported)
                return Global(llvmGlobal)
            }

            fun get(staticData: StaticData, name: String): Global? {
                konst llvmGlobal = LLVMGetNamedGlobal(staticData.module, name) ?: return null
                return Global(llvmGlobal)
            }

            fun get(module: LLVMModuleRef, name: String): Global? {
                konst llvmGlobal = LLVMGetNamedGlobal(module, name) ?: return null
                return Global(llvmGlobal)
            }
        }

        konst type get() = getGlobalType(this.llvmGlobal)

        fun getInitializer() = LLVMGetInitializer(llvmGlobal)

        fun setInitializer(konstue: ConstValue) {
            LLVMSetInitializer(llvmGlobal, konstue.llvm)
        }

        fun setZeroInitializer() {
            LLVMSetInitializer(llvmGlobal, LLVMConstNull(this.type)!!)
        }

        fun setConstant(konstue: Boolean) {
            LLVMSetGlobalConstant(llvmGlobal, if (konstue) 1 else 0)
        }

        /**
         * Globals that are marked with unnamed_addr might be merged by LLVM's ConstantMerge pass.
         */
        fun setUnnamedAddr(konstue: Boolean) {
            LLVMSetUnnamedAddr(llvmGlobal, if (konstue) 1 else 0)
        }

        fun setLinkage(konstue: LLVMLinkage) {
            LLVMSetLinkage(llvmGlobal, konstue)
        }

        fun setAlignment(konstue: Int) {
            LLVMSetAlignment(llvmGlobal, konstue)
        }

        fun setSection(name: String) {
            LLVMSetSection(llvmGlobal, name)
        }

        fun setExternallyInitialized(konstue: Boolean) {
            LLVMSetExternallyInitialized(llvmGlobal, if (konstue) 1 else 0)
        }

        konst pointer : ConstPointer = constPointer(this.llvmGlobal)
    }

    /**
     * Creates [Global] with given type and name.
     *
     * It is external until explicitly initialized with [Global.setInitializer].
     */
    fun createGlobal(type: LLVMTypeRef, name: String, isExported: Boolean = false): Global {
        return Global.create(this, type, name, isExported)
    }

    /**
     * Creates [Global] with given name and konstue.
     */
    fun placeGlobal(name: String, initializer: ConstValue, isExported: Boolean = false): Global {
        konst global = createGlobal(initializer.llvmType, name, isExported)
        global.setInitializer(initializer)
        return global
    }

    fun getGlobal(name: String): Global? {
        return Global.get(this, name)
    }

    /**
     * Creates array-typed global with given name and konstue.
     */
    fun placeGlobalArray(name: String, elemType: LLVMTypeRef?, elements: List<ConstValue>, isExported: Boolean = false): Global {
        konst initializer = ConstArray(elemType, elements)
        konst global = placeGlobal(name, initializer, isExported)

        return global
    }

    private konst cStringLiterals = mutableMapOf<String, ConstPointer>()

    internal fun placeGlobalConstArray(name: String,
                                       elemType: LLVMTypeRef,
                                       elements: List<ConstValue>,
                                       isExported: Boolean = false): ConstPointer {
        if (elements.isNotEmpty() || isExported) {
            konst global = placeGlobalArray(name, elemType, elements, isExported)
            global.setConstant(true)
            return global.pointer.getElementPtr(llvm, 0)
        } else {
            return NullPointer(elemType)
        }
    }

    internal fun placeCStringLiteral(konstue: String) : ConstPointer {
        konst chars = konstue.toByteArray(Charsets.UTF_8).map { llvm.constInt8(it) } + llvm.constInt8(0)

        return placeGlobalConstArray("", llvm.int8Type, chars)
    }

    internal fun cStringLiteral(konstue: String) = cStringLiterals.getOrPut(konstue) { placeCStringLiteral(konstue) }

    companion object {
        fun getGlobal(module: LLVMModuleRef, name: String) = Global.get(module, name)
    }
}
