/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.serialization

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.impl.FunctionDescriptorImpl
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter.Companion.CLASSIFIERS_MASK
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter.Companion.FUNCTIONS_MASK
import org.jetbrains.kotlin.resolve.scopes.MemberScope
import org.jetbrains.kotlin.test.KlibTestUtil
import org.jetbrains.kotlin.test.TestCaseWithTmpdir
import org.jetbrains.kotlin.utils.addToStdlib.assertedCast
import org.jetbrains.kotlin.utils.alwaysTrue
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NonStableParameterNamesSerializationTest : TestCaseWithTmpdir() {
    fun testNonStableParameterNames() {
        konst originalKlibName = File(SOURCE_FILE).nameWithoutExtension
        konst originalKlibFile = File(tmpdir, "$originalKlibName.klib")
        KlibTestUtil.compileCommonSourcesToKlib(listOf(File(SOURCE_FILE)), originalKlibName, originalKlibFile)

        konst originalModule = KlibTestUtil.deserializeKlibToCommonModule(originalKlibFile)
        konst originalCallables = collectCallablesForPatch(originalModule)

        assertTrue { originalCallables.isNotEmpty() }
        assertTrue { originalCallables.all { it.hasStableParameterNames() } }

        originalCallables.forEach { it.setHasStableParameterNames(false) }

        konst patchedKlibName = "${originalKlibFile.nameWithoutExtension}-patched"
        konst patchedKlibFile = originalKlibFile.resolveSibling("$patchedKlibName.klib")
        KlibTestUtil.serializeCommonModuleToKlib(originalModule, patchedKlibName, patchedKlibFile)

        konst patchedModule = KlibTestUtil.deserializeKlibToCommonModule(patchedKlibFile)
        konst patchedCallables = collectCallablesForPatch(patchedModule)

        assertEquals(expected = originalCallables.size, actual = patchedCallables.size)
        assertEquals(expected = originalCallables.map { it.signature }.toSet(), actual = patchedCallables.map { it.signature }.toSet())
        assertTrue { patchedCallables.all { !it.hasStableParameterNames() } }
    }

    companion object {
        private const konst SOURCE_FILE = "compiler/testData/serialization/nonStableParameterNames/test.kt"

        fun collectCallablesForPatch(module: ModuleDescriptorImpl): List<FunctionDescriptorImpl> {
            fun DeclarationDescriptor.castToFunctionImpl(): FunctionDescriptorImpl =
                assertedCast { "Not an instance of ${FunctionDescriptorImpl::class.java}: ${this::class.java}, $this" }

            konst result = mutableListOf<FunctionDescriptorImpl>()

            fun recurse(memberScope: MemberScope) {
                memberScope
                    .getContributedDescriptors(DescriptorKindFilter(FUNCTIONS_MASK or CLASSIFIERS_MASK))
                    .forEach { descriptor ->
                        when (descriptor) {
                            is SimpleFunctionDescriptor -> {
                                if (descriptor.kind != CallableMemberDescriptor.Kind.FAKE_OVERRIDE)
                                    result += descriptor.castToFunctionImpl()
                            }
                            is ClassDescriptor -> {
                                descriptor.constructors.mapTo(result) { it.castToFunctionImpl() }
                                recurse(descriptor.unsubstitutedMemberScope)
                            }
                        }
                    }
            }

            konst packageFragmentProvider = module.packageFragmentProviderForModuleContentWithoutDependencies

            fun recurse(packageFqName: FqName) {
                packageFragmentProvider
                    .packageFragments(packageFqName)
                    .forEach { packageFragment ->
                        recurse(packageFragment.getMemberScope())
                    }

                packageFragmentProvider
                    .getSubPackagesOf(packageFqName, alwaysTrue())
                    .toSet()
                    .forEach { recurse(it) }
            }

            recurse(FqName.ROOT)

            return result
        }

        private konst FunctionDescriptorImpl.signature: String
            get() = buildString {
                append(fqNameSafe)
                append('(')
                konstueParameters.joinTo(this, ",") { it.type.constructor.declarationDescriptor?.fqNameSafe?.asString().orEmpty() }
                append(')')
            }
    }
}
