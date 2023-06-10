/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
@file:Suppress("DEPRECATION")

package kotlinx.metadata.klib.impl

import kotlinx.metadata.*
import kotlinx.metadata.internal.common.KmModuleFragment
import kotlinx.metadata.internal.common.KmModuleFragmentExtensionVisitor
import kotlinx.metadata.internal.extensions.*
import kotlinx.metadata.klib.*

internal konst KmFunction.klibExtensions: KlibFunctionExtension
    get() = visitExtensions(KlibFunctionExtensionVisitor.TYPE) as KlibFunctionExtension

internal konst KmClass.klibExtensions: KlibClassExtension
    get() = visitExtensions(KlibClassExtensionVisitor.TYPE) as KlibClassExtension

internal konst KmType.klibExtensions: KlibTypeExtension
    get() = visitExtensions(KlibTypeExtensionVisitor.TYPE) as KlibTypeExtension

internal konst KmProperty.klibExtensions: KlibPropertyExtension
    get() = visitExtensions(KlibPropertyExtensionVisitor.TYPE) as KlibPropertyExtension

internal konst KmConstructor.klibExtensions: KlibConstructorExtension
    get() = visitExtensions(KlibConstructorExtensionVisitor.TYPE) as KlibConstructorExtension

internal konst KmTypeParameter.klibExtensions: KlibTypeParameterExtension
    get() = visitExtensions(KlibTypeParameterExtensionVisitor.TYPE) as KlibTypeParameterExtension

internal konst KmPackage.klibExtensions: KlibPackageExtension
    get() = visitExtensions(KlibPackageExtensionVisitor.TYPE) as KlibPackageExtension

internal konst KmModuleFragment.klibExtensions: KlibModuleFragmentExtension
    get() = visitExtensions(KlibModuleFragmentExtensionVisitor.TYPE) as KlibModuleFragmentExtension

internal konst KmTypeAlias.klibExtensions: KlibTypeAliasExtension
    get() = visitExtensions(KlibTypeAliasExtensionVisitor.TYPE) as KlibTypeAliasExtension

internal konst KmValueParameter.klibExtensions: KlibValueParameterExtension
    get() = visitExtensions(KlibValueParameterExtensionVisitor.TYPE) as KlibValueParameterExtension

internal class KlibFunctionExtension : KlibFunctionExtensionVisitor(), KmFunctionExtension {

    konst annotations: MutableList<KmAnnotation> = mutableListOf()
    var uniqId: UniqId? = null
    var file: KlibSourceFile? = null

    override fun visitUniqId(uniqId: UniqId) {
        this.uniqId = uniqId
    }

    override fun visitAnnotation(annotation: KmAnnotation) {
        annotations += annotation
    }

    override fun visitFile(file: KlibSourceFile) {
        this.file = file
    }

    override fun accept(visitor: KmFunctionExtensionVisitor) {
        require(visitor is KlibFunctionExtensionVisitor)
        annotations.forEach(visitor::visitAnnotation)
        uniqId?.let(visitor::visitUniqId)
        file?.let(visitor::visitFile)
    }
}

internal class KlibClassExtension : KlibClassExtensionVisitor(), KmClassExtension {

    konst annotations: MutableList<KmAnnotation> = mutableListOf()
    konst enumEntries: MutableList<KlibEnumEntry> = mutableListOf()
    var uniqId: UniqId? = null
    var file: KlibSourceFile? = null

    override fun visitAnnotation(annotation: KmAnnotation) {
        annotations += annotation
    }

    override fun visitUniqId(uniqId: UniqId) {
        this.uniqId = uniqId
    }

    override fun visitFile(file: KlibSourceFile) {
        this.file = file
    }

    override fun visitEnumEntry(entry: KlibEnumEntry) {
        enumEntries += entry
    }

    override fun accept(visitor: KmClassExtensionVisitor) {
        require(visitor is KlibClassExtensionVisitor)
        annotations.forEach(visitor::visitAnnotation)
        enumEntries.forEach(visitor::visitEnumEntry)
        uniqId?.let(visitor::visitUniqId)
        file?.let(visitor::visitFile)
    }
}

internal class KlibTypeExtension : KlibTypeExtensionVisitor(), KmTypeExtension {

    konst annotations: MutableList<KmAnnotation> = mutableListOf()

    override fun visitAnnotation(annotation: KmAnnotation) {
        annotations += annotation
    }

    override fun accept(visitor: KmTypeExtensionVisitor) {
        require(visitor is KlibTypeExtensionVisitor)
        annotations.forEach(visitor::visitAnnotation)
    }
}

internal class KlibPropertyExtension : KlibPropertyExtensionVisitor(), KmPropertyExtension {

    konst annotations: MutableList<KmAnnotation> = mutableListOf()
    konst getterAnnotations: MutableList<KmAnnotation> = mutableListOf()
    konst setterAnnotations: MutableList<KmAnnotation> = mutableListOf()
    var uniqId: UniqId? = null
    var file: Int? = null
    var compileTimeValue: KmAnnotationArgument? = null

    override fun visitAnnotation(annotation: KmAnnotation) {
        annotations += annotation
    }

    override fun visitGetterAnnotation(annotation: KmAnnotation) {
        getterAnnotations += annotation
    }

    override fun visitSetterAnnotation(annotation: KmAnnotation) {
        setterAnnotations += annotation
    }

    override fun visitFile(file: Int) {
        this.file = file
    }

    override fun visitUniqId(uniqId: UniqId) {
        this.uniqId = uniqId
    }

    override fun visitCompileTimeValue(konstue: KmAnnotationArgument) {
        this.compileTimeValue = konstue
    }

    override fun accept(visitor: KmPropertyExtensionVisitor) {
        require(visitor is KlibPropertyExtensionVisitor)
        annotations.forEach(visitor::visitAnnotation)
        getterAnnotations.forEach(visitor::visitGetterAnnotation)
        setterAnnotations.forEach(visitor::visitSetterAnnotation)
        file?.let(visitor::visitFile)
        uniqId?.let(visitor::visitUniqId)
        compileTimeValue?.let(visitor::visitCompileTimeValue)
    }
}

internal class KlibConstructorExtension : KlibConstructorExtensionVisitor(), KmConstructorExtension {

    konst annotations: MutableList<KmAnnotation> = mutableListOf()
    var uniqId: UniqId? = null

    override fun visitAnnotation(annotation: KmAnnotation) {
        annotations += annotation
    }

    override fun visitUniqId(uniqId: UniqId) {
        this.uniqId = uniqId
    }

    override fun accept(visitor: KmConstructorExtensionVisitor) {
        require(visitor is KlibConstructorExtensionVisitor)
        annotations.forEach(visitor::visitAnnotation)
        uniqId?.let(visitor::visitUniqId)
    }
}

internal class KlibTypeParameterExtension : KlibTypeParameterExtensionVisitor(), KmTypeParameterExtension {

    konst annotations: MutableList<KmAnnotation> = mutableListOf()
    var uniqId: UniqId? = null

    override fun visitAnnotation(annotation: KmAnnotation) {
        annotations += annotation
    }

    override fun visitUniqId(uniqId: UniqId) {
        this.uniqId = uniqId
    }

    override fun accept(visitor: KmTypeParameterExtensionVisitor) {
        require(visitor is KlibTypeParameterExtensionVisitor)
        annotations.forEach(visitor::visitAnnotation)
        uniqId?.let(visitor::visitUniqId)
    }
}

internal class KlibPackageExtension : KlibPackageExtensionVisitor(), KmPackageExtension {

    var fqName: String? = null

    override fun visitFqName(name: String) {
        fqName = name
    }

    override fun accept(visitor: KmPackageExtensionVisitor) {
        require(visitor is KlibPackageExtensionVisitor)
        fqName?.let(visitor::visitFqName)
    }
}

internal class KlibModuleFragmentExtension : KlibModuleFragmentExtensionVisitor(), KmModuleFragmentExtension {

    konst moduleFragmentFiles: MutableList<KlibSourceFile> = ArrayList()
    var fqName: String? = null
    konst className: MutableList<ClassName> = ArrayList()

    override fun visitFile(file: KlibSourceFile) {
        moduleFragmentFiles += file
    }

    override fun visitFqName(fqName: String) {
        this.fqName = fqName
    }

    override fun visitClassName(className: ClassName) {
        this.className += className
    }

    override fun accept(visitor: KmModuleFragmentExtensionVisitor) {
        require(visitor is KlibModuleFragmentExtensionVisitor)
        moduleFragmentFiles.forEach(visitor::visitFile)
        fqName?.let(visitor::visitFqName)
        className.forEach(visitor::visitClassName)
    }
}

internal class KlibTypeAliasExtension : KlibTypeAliasExtensionVisitor(), KmTypeAliasExtension {
    var uniqId: UniqId? = null

    override fun visitUniqId(uniqId: UniqId) {
        this.uniqId = uniqId
    }

    override fun accept(visitor: KmTypeAliasExtensionVisitor) {
        require(visitor is KlibTypeAliasExtensionVisitor)
        uniqId?.let(visitor::visitUniqId)
    }
}

internal class KlibValueParameterExtension : KlibValueParameterExtensionVisitor(), KmValueParameterExtension {
    konst annotations: MutableList<KmAnnotation> = mutableListOf()

    override fun visitAnnotation(annotation: KmAnnotation) {
        annotations += annotation
    }

    override fun accept(visitor: KmValueParameterExtensionVisitor) {
        require(visitor is KlibValueParameterExtensionVisitor)
        annotations.forEach(visitor::visitAnnotation)
    }
}
