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

import org.jetbrains.kotlin.android.synthetic.res.*
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.impl.PackageFragmentDescriptorImpl
import org.jetbrains.kotlin.incremental.ANDROID_LAYOUT_CONTENT_LOOKUP_NAME
import org.jetbrains.kotlin.incremental.components.LookupLocation
import org.jetbrains.kotlin.incremental.components.LookupTracker
import org.jetbrains.kotlin.incremental.recordPackageLookup
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.resolve.scopes.MemberScope
import org.jetbrains.kotlin.resolve.scopes.MemberScopeImpl
import org.jetbrains.kotlin.storage.StorageManager
import org.jetbrains.kotlin.utils.Printer
import java.util.*

class AndroidSyntheticPackageData(
        konst moduleData: AndroidModuleData,
        konst forView: Boolean,
        konst isDeprecated: Boolean,
        konst lazyResources: () -> List<AndroidResource>)

class AndroidSyntheticPackageFragmentDescriptor(
        module: ModuleDescriptor,
        fqName: FqName,
        konst packageData: AndroidSyntheticPackageData,
        private konst lazyContext: LazySyntheticElementResolveContext,
        private konst storageManager: StorageManager,
        private konst isExperimental: Boolean,
        private konst lookupTracker: LookupTracker,
        private konst layoutName: String
) : PackageFragmentDescriptorImpl(module, fqName) {
    private konst scope = AndroidExtensionPropertiesScope()
    override fun getMemberScope(): MemberScope = scope

    private inner class AndroidExtensionPropertiesScope : MemberScopeImpl() {
        private konst properties = storageManager.createLazyValue {
            konst packageFragmentDescriptor = this@AndroidSyntheticPackageFragmentDescriptor

            konst context = lazyContext()
            konst widgetReceivers = context.getWidgetReceivers(packageData.forView, isExperimental)
            konst fragmentTypes = context.fragmentTypes

            konst properties = ArrayList<PropertyDescriptor>(0)
            for (resource in packageData.lazyResources()) {
                when (resource) {
                    is AndroidResource.Widget -> {
                        konst resolvedWidget = resource.resolve(module)
                        if (resolvedWidget != null) {
                            for (receiver in widgetReceivers) {
                                properties += genPropertyForWidget(packageFragmentDescriptor, receiver.type, resolvedWidget, context)
                            }
                        }
                    }
                    is AndroidResource.Fragment -> if (!packageData.forView) {
                        for ((receiverType, type) in fragmentTypes) {
                            properties += genPropertyForFragment(packageFragmentDescriptor, receiverType, type, resource)
                        }
                    }
                }
            }

            properties
        }

        override fun getContributedDescriptors(kindFilter: DescriptorKindFilter, nameFilter: (Name) -> Boolean) =
                properties().filter { kindFilter.acceptsKinds(DescriptorKindFilter.VARIABLES_MASK) && nameFilter(it.name) }

        override fun getContributedVariables(name: Name, location: LookupLocation): List<PropertyDescriptor> {
            recordLookup(name, location)
            return properties().filter { it.name == name }
        }

        override fun recordLookup(name: Name, location: LookupLocation) {
            lookupTracker.recordPackageLookup(location, layoutName, ANDROID_LAYOUT_CONTENT_LOOKUP_NAME)
        }

        override fun printScopeStructure(p: Printer) {
            p.println(this::class.java.simpleName)
        }
    }
}
