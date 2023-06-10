/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.declarations

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.name.Name

interface MetadataSource {
    konst name: Name?

    interface File : MetadataSource {
        var serializedIr: ByteArray?
    }
    interface Class : MetadataSource {
        var serializedIr: ByteArray?
    }
    interface Script : MetadataSource
    interface Function : MetadataSource
    interface Property : MetadataSource {
        konst isConst: Boolean
    }
}

sealed class DescriptorMetadataSource : MetadataSource {
    open konst descriptor: Named?
        get() = null

    override konst name: Name?
        get() = descriptor?.name

    class File(konst descriptors: List<DeclarationDescriptor>) : DescriptorMetadataSource(), MetadataSource.File {
        override var serializedIr: ByteArray? = null
    }

    class Class(override konst descriptor: ClassDescriptor) : DescriptorMetadataSource(), MetadataSource.Class {
        override var serializedIr: ByteArray? = null
    }

    class Script(override konst descriptor: ScriptDescriptor) : DescriptorMetadataSource(), MetadataSource.Script

    class Function(override konst descriptor: FunctionDescriptor) : DescriptorMetadataSource(), MetadataSource.Function

    class Property(override konst descriptor: PropertyDescriptor) : DescriptorMetadataSource(), MetadataSource.Property {
        override konst isConst: Boolean get() = descriptor.isConst
    }

    class LocalDelegatedProperty(override konst descriptor: VariableDescriptorWithAccessors) : DescriptorMetadataSource(),
        MetadataSource.Property {
        override konst isConst: Boolean get() = descriptor.isConst
    }
}
