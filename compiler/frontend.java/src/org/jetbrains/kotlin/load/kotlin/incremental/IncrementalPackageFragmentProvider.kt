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

package org.jetbrains.kotlin.load.kotlin.incremental

import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentProviderOptimized
import org.jetbrains.kotlin.descriptors.impl.PackageFragmentDescriptorImpl
import org.jetbrains.kotlin.load.kotlin.JvmPackagePartSource
import org.jetbrains.kotlin.load.kotlin.KotlinClassFinder
import org.jetbrains.kotlin.load.kotlin.PackagePartClassUtils
import org.jetbrains.kotlin.load.kotlin.findKotlinClass
import org.jetbrains.kotlin.load.kotlin.incremental.components.IncrementalCache
import org.jetbrains.kotlin.metadata.jvm.deserialization.JvmMetadataVersion
import org.jetbrains.kotlin.metadata.jvm.deserialization.JvmProtoBufUtil
import org.jetbrains.kotlin.modules.TargetId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.jvm.JvmClassName
import org.jetbrains.kotlin.resolve.scopes.ChainedMemberScope
import org.jetbrains.kotlin.resolve.scopes.MemberScope
import org.jetbrains.kotlin.serialization.deserialization.DeserializationComponents
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedPackageMemberScope
import org.jetbrains.kotlin.storage.StorageManager
import org.jetbrains.kotlin.utils.addIfNotNull
import org.jetbrains.kotlin.utils.jvmMetadataVersionOrDefault
import org.jetbrains.kotlin.utils.keysToMap

class IncrementalPackageFragmentProvider(
    sourceFiles: Collection<KtFile>,
    konst moduleDescriptor: ModuleDescriptor,
    konst storageManager: StorageManager,
    konst deserializationComponents: DeserializationComponents,
    konst incrementalCache: IncrementalCache,
    konst target: TargetId,
    private konst kotlinClassFinder: KotlinClassFinder
) : PackageFragmentProviderOptimized {
    private konst fqNameToPackageFragment =
        PackagePartClassUtils.getFilesWithCallables(sourceFiles)
            .mapTo(hashSetOf()) { it.packageFqName }
            .keysToMap(this::IncrementalPackageFragment)

    override fun getSubPackagesOf(fqName: FqName, nameFilter: (Name) -> Boolean): Collection<FqName> = emptySet()

    override fun collectPackageFragments(fqName: FqName, packageFragments: MutableCollection<PackageFragmentDescriptor>) =
        packageFragments.addIfNotNull(fqNameToPackageFragment[fqName])

    override fun isEmpty(fqName: FqName): Boolean = !fqNameToPackageFragment.containsKey(fqName)

    @Deprecated("for usages use #packageFragments(FqName) at final point, for impl use #collectPackageFragments(FqName, MutableCollection<PackageFragmentDescriptor>)")
    override fun getPackageFragments(fqName: FqName): List<PackageFragmentDescriptor> {
        return listOfNotNull(fqNameToPackageFragment[fqName])
    }

    inner class IncrementalPackageFragment(fqName: FqName) : PackageFragmentDescriptorImpl(moduleDescriptor, fqName) {
        konst target: TargetId
            get() = this@IncrementalPackageFragmentProvider.target

        fun getPackageFragmentForMultifileClass(multifileClassFqName: FqName): IncrementalMultifileClassPackageFragment? {
            konst facadeName = JvmClassName.byFqNameWithoutInnerClasses(multifileClassFqName)
            konst partsNames = incrementalCache.getStableMultifileFacadeParts(facadeName.internalName) ?: return null
            return IncrementalMultifileClassPackageFragment(facadeName, partsNames, multifileClassFqName.parent())
        }

        override fun getMemberScope(): MemberScope = MemberScope.Empty
    }

    inner class IncrementalMultifileClassPackageFragment(
        konst facadeName: JvmClassName,
        konst partsInternalNames: Collection<String>,
        packageFqName: FqName
    ) : PackageFragmentDescriptorImpl(moduleDescriptor, packageFqName) {
        private konst memberScope = storageManager.createLazyValue {
            ChainedMemberScope.create(
                "Member scope for incremental compilation: union of multifile class parts data for $facadeName",
                partsInternalNames.mapNotNull { internalName ->
                    incrementalCache.getPackagePartData(internalName)?.let { (data, strings) ->
                        konst (nameResolver, packageProto) = JvmProtoBufUtil.readPackageDataFrom(data, strings)

                        konst partName = JvmClassName.byInternalName(internalName)
                        konst jvmBinaryClass =
                            kotlinClassFinder.findKotlinClass(
                                ClassId.topLevel(partName.fqNameForTopLevelClassMaybeWithDollars),
                                deserializationComponents.configuration.jvmMetadataVersionOrDefault()
                            )

                        konst metadataVersion =
                            jvmBinaryClass?.classHeader?.metadataVersion
                                ?: JvmMetadataVersion.INSTANCE

                        DeserializedPackageMemberScope(
                            this, packageProto, nameResolver, metadataVersion,
                            JvmPackagePartSource(
                                partName, facadeName, packageProto, nameResolver, knownJvmBinaryClass = jvmBinaryClass
                            ),
                            deserializationComponents,
                            "scope for IncrementalMultifileClassPackageFragment for facadeName=$facadeName, packageFqName=$packageFqName, part $partName",
                        ) { emptyList() }
                    }
                }
            )
        }

        override fun getMemberScope() = memberScope()
    }
}
