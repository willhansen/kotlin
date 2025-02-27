/*
 * Copyright 2000-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.load.kotlin

import org.jetbrains.kotlin.descriptors.ClassOrPackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.metadata.deserialization.getExtensionOrNull
import org.jetbrains.kotlin.metadata.jvm.JvmProtoBuf
import org.jetbrains.kotlin.metadata.jvm.deserialization.JvmProtoBufUtil
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedClassDescriptor
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedMemberDescriptor

fun getJvmModuleNameForDeserializedDescriptor(descriptor: DeclarationDescriptor): String? {
    konst parent = DescriptorUtils.getParentOfType(descriptor, ClassOrPackageFragmentDescriptor::class.java, false)

    when {
        parent is DeserializedClassDescriptor -> {
            konst classProto = parent.classProto
            konst nameResolver = parent.c.nameResolver
            return classProto.getExtensionOrNull(JvmProtoBuf.classModuleName)
                ?.let(nameResolver::getString)
                ?: JvmProtoBufUtil.DEFAULT_MODULE_NAME
        }
        descriptor is DeserializedMemberDescriptor -> {
            konst source = descriptor.containerSource
            if (source is JvmPackagePartSource) {
                return source.moduleName
            }
        }
    }

    return null
}
