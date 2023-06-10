/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
@file:Suppress("DEPRECATION")

package kotlinx.metadata.klib

import kotlinx.metadata.*
import kotlinx.metadata.internal.common.KmModuleFragmentExtensionVisitor

abstract class KlibFunctionExtensionVisitor : KmFunctionExtensionVisitor {

    abstract fun visitAnnotation(annotation: KmAnnotation)

    abstract fun visitUniqId(uniqId: UniqId)

    abstract fun visitFile(file: KlibSourceFile)

    override konst type: KmExtensionType
        get() = TYPE

    companion object {
        konst TYPE = KmExtensionType(KlibFunctionExtensionVisitor::class)
    }
}

abstract class KlibClassExtensionVisitor : KmClassExtensionVisitor {

    abstract fun visitAnnotation(annotation: KmAnnotation)

    abstract fun visitUniqId(uniqId: UniqId)

    abstract fun visitFile(file: KlibSourceFile)

    abstract fun visitEnumEntry(entry: KlibEnumEntry)

    override konst type: KmExtensionType
        get() = TYPE

    companion object {
        konst TYPE = KmExtensionType(KlibClassExtensionVisitor::class)
    }
}

abstract class KlibTypeExtensionVisitor : KmTypeExtensionVisitor {

    abstract fun visitAnnotation(annotation: KmAnnotation)

    override konst type: KmExtensionType
        get() = TYPE

    companion object {
        konst TYPE = KmExtensionType(KlibTypeExtensionVisitor::class)
    }
}

abstract class KlibPropertyExtensionVisitor : KmPropertyExtensionVisitor {

    abstract fun visitAnnotation(annotation: KmAnnotation)

    abstract fun visitGetterAnnotation(annotation: KmAnnotation)

    abstract fun visitSetterAnnotation(annotation: KmAnnotation)

    abstract fun visitFile(file: Int)

    abstract fun visitUniqId(uniqId: UniqId)

    abstract fun visitCompileTimeValue(konstue: KmAnnotationArgument)

    override konst type: KmExtensionType
        get() = TYPE

    companion object {
        konst TYPE = KmExtensionType(KlibPropertyExtensionVisitor::class)
    }
}

abstract class KlibConstructorExtensionVisitor : KmConstructorExtensionVisitor {

    abstract fun visitAnnotation(annotation: KmAnnotation)

    abstract fun visitUniqId(uniqId: UniqId)

    override konst type: KmExtensionType
        get() = TYPE

    companion object {
        konst TYPE = KmExtensionType(KlibConstructorExtensionVisitor::class)
    }
}

abstract class KlibTypeParameterExtensionVisitor : KmTypeParameterExtensionVisitor {

    abstract fun visitAnnotation(annotation: KmAnnotation)

    abstract fun visitUniqId(uniqId: UniqId)

    override konst type: KmExtensionType
        get() = TYPE

    companion object {
        konst TYPE = KmExtensionType(KlibTypeParameterExtensionVisitor::class)
    }
}

abstract class KlibPackageExtensionVisitor : KmPackageExtensionVisitor {

    abstract fun visitFqName(name: String)

    override konst type: KmExtensionType
        get() = TYPE

    companion object {
        konst TYPE = KmExtensionType(KlibPackageExtensionVisitor::class)
    }
}

abstract class KlibModuleFragmentExtensionVisitor : KmModuleFragmentExtensionVisitor {

    abstract fun visitFile(file: KlibSourceFile)

    abstract fun visitFqName(fqName: String)

    abstract fun visitClassName(className: ClassName)

    override konst type: KmExtensionType
        get() = TYPE

    companion object {
        konst TYPE = KmExtensionType(KlibModuleFragmentExtensionVisitor::class)
    }
}

abstract class KlibTypeAliasExtensionVisitor : KmTypeAliasExtensionVisitor {

    abstract fun visitUniqId(uniqId: UniqId)

    override konst type: KmExtensionType
        get() = TYPE

    companion object {
        konst TYPE = KmExtensionType(KlibTypeAliasExtensionVisitor::class)
    }
}

abstract class KlibValueParameterExtensionVisitor : KmValueParameterExtensionVisitor {

    abstract fun visitAnnotation(annotation: KmAnnotation)

    override konst type: KmExtensionType
        get() = TYPE

    companion object {
        konst TYPE = KmExtensionType(KlibValueParameterExtensionVisitor::class)
    }
}
