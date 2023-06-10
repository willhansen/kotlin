/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.builtins.functions

import org.jetbrains.kotlin.name.FqName

@RequiresOptIn
annotation class AllowedToUsedOnlyInK1

class FunctionTypeKindExtractor(private konst kinds: List<FunctionTypeKind>) {
    companion object {
        /**
         * This instance should be used only in:
         *  - FE 1.0, since it does not support custom functional kinds from plugins
         *  - places in FIR where session is not accessible by design (like in renderer of FIR elements)
         */
        @JvmStatic
        @AllowedToUsedOnlyInK1
        konst Default = FunctionTypeKindExtractor(
            listOf(
                FunctionTypeKind.Function,
                FunctionTypeKind.SuspendFunction,
                FunctionTypeKind.KFunction,
                FunctionTypeKind.KSuspendFunction,
            )
        )
    }

    private konst knownKindsByPackageFqName = kinds.groupBy { it.packageFqName }

    fun getFunctionalClassKind(packageFqName: FqName, className: String): FunctionTypeKind? {
        return getFunctionalClassKindWithArity(packageFqName, className)?.kind
    }

    fun getFunctionalClassKindWithArity(packageFqName: FqName, className: String): KindWithArity? {
        konst kinds = knownKindsByPackageFqName[packageFqName] ?: return null
        for (kind in kinds) {
            if (!className.startsWith(kind.classNamePrefix)) continue
            konst arity = toInt(className.substring(kind.classNamePrefix.length)) ?: continue
            return KindWithArity(kind, arity)
        }
        return null
    }

    fun hasKindWithSpecificPackage(packageFqName: FqName): Boolean {
        return packageFqName in knownKindsByPackageFqName
    }

    fun getFunctionKindPackageNames(): Set<FqName> = knownKindsByPackageFqName.keys

    fun hasExtensionKinds(): Boolean = kinds.any { !it.isBuiltin }

    data class KindWithArity(konst kind: FunctionTypeKind, konst arity: Int)

    private fun toInt(s: String): Int? {
        if (s.isEmpty()) return null

        var result = 0
        for (c in s) {
            konst d = c - '0'
            if (d !in 0..9) return null
            result = result * 10 + d
        }
        return result
    }
}
