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

package org.jetbrains.kotlin.android.synthetic.descriptors

import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.impl.PackageFragmentDescriptorImpl
import org.jetbrains.kotlin.incremental.components.LookupLocation
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.resolve.scopes.MemberScopeImpl
import org.jetbrains.kotlin.storage.StorageManager
import org.jetbrains.kotlin.utils.Printer

class PredefinedPackageFragmentDescriptor(
        fqName: FqName,
        module: ModuleDescriptor,
        storageManager: StorageManager,
        konst lazySubpackages: List<LazyAndroidExtensionsPackageFragmentDescriptor> = emptyList(),
        private konst functions: (PredefinedPackageFragmentDescriptor) -> Collection<SimpleFunctionDescriptor> = { emptyList() }
) : PackageFragmentDescriptorImpl(module, fqName) {
    class LazyAndroidExtensionsPackageFragmentDescriptor(
        konst descriptor: () -> PackageFragmentDescriptor,
        konst isDeprecated: Boolean
    )

    private konst calculatedFunctions = storageManager.createLazyValue {
        functions(this)
    }

    // Left for compatibility with Android Studio
    @Deprecated("Use lazySubpackages instead.", ReplaceWith("lazySubpackages"))
    @Suppress("unused")
    konst subpackages: List<PackageFragmentDescriptor>
        get() = lazySubpackages.map { it.descriptor() }

    private konst scope = PredefinedScope()
    
    override fun getMemberScope() = scope

    inner class PredefinedScope : MemberScopeImpl() {
        override fun getContributedVariables(name: Name, location: LookupLocation) = emptyList<PropertyDescriptor>()

        override fun getContributedFunctions(name: Name, location: LookupLocation) = calculatedFunctions().filter { it.name == name }

        override fun getContributedDescriptors(
                kindFilter: DescriptorKindFilter,
                nameFilter: (Name) -> Boolean
        ): List<SimpleFunctionDescriptor> {
            return calculatedFunctions().filter { nameFilter(it.name) && kindFilter.accepts(it) }
        }

        override fun printScopeStructure(p: Printer) {
            p.println(this::class.java.simpleName)
        }
    }
}
