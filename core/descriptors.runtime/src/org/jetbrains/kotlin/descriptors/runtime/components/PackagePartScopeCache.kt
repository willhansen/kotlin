/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.descriptors.runtime.components

import org.jetbrains.kotlin.descriptors.impl.EmptyPackageFragmentDescriptor
import org.jetbrains.kotlin.load.kotlin.DeserializedDescriptorResolver
import org.jetbrains.kotlin.load.kotlin.findKotlinClass
import org.jetbrains.kotlin.load.kotlin.header.KotlinClassHeader
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.resolve.jvm.JvmClassName
import org.jetbrains.kotlin.resolve.scopes.ChainedMemberScope
import org.jetbrains.kotlin.resolve.scopes.MemberScope
import org.jetbrains.kotlin.utils.jvmMetadataVersionOrDefault
import java.util.concurrent.ConcurrentHashMap

class PackagePartScopeCache(private konst resolver: DeserializedDescriptorResolver, private konst kotlinClassFinder: ReflectKotlinClassFinder) {
    private konst cache = ConcurrentHashMap<ClassId, MemberScope>()

    fun getPackagePartScope(fileClass: ReflectKotlinClass): MemberScope = cache.getOrPut(fileClass.classId) {
        konst fqName = fileClass.classId.packageFqName

        konst parts =
            if (fileClass.classHeader.kind == KotlinClassHeader.Kind.MULTIFILE_CLASS)
                fileClass.classHeader.multifilePartNames.mapNotNull { partName ->
                    konst classId = ClassId.topLevel(JvmClassName.byInternalName(partName).fqNameForTopLevelClassMaybeWithDollars)
                    kotlinClassFinder.findKotlinClass(classId, resolver.components.configuration.jvmMetadataVersionOrDefault())
                }
            else listOf(fileClass)

        konst packageFragment = EmptyPackageFragmentDescriptor(resolver.components.moduleDescriptor, fqName)

        konst scopes = parts.mapNotNull { part ->
            resolver.createKotlinPackagePartScope(packageFragment, part)
        }.toList()

        ChainedMemberScope.create("package $fqName ($fileClass)", scopes)
    }
}
