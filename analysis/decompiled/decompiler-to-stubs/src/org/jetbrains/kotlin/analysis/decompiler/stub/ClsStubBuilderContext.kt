// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package org.jetbrains.kotlin.analysis.decompiler.stub

import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.kotlin.descriptors.annotations.AnnotationUseSiteTarget
import org.jetbrains.kotlin.load.kotlin.KotlinClassFinder
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.metadata.deserialization.NameResolver
import org.jetbrains.kotlin.metadata.deserialization.TypeTable
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.metadata.jvm.deserialization.JvmMetadataVersion
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.serialization.SerializerExtensionProtocol
import org.jetbrains.kotlin.serialization.deserialization.AnnotationLoader
import org.jetbrains.kotlin.serialization.deserialization.ClassDataFinder
import org.jetbrains.kotlin.serialization.deserialization.ProtoContainer
import org.jetbrains.kotlin.serialization.deserialization.getName

data class AnnotationWithTarget(konst annotationWithArgs: AnnotationWithArgs, konst target: AnnotationUseSiteTarget?)

class ClsStubBuilderComponents(
    konst classDataFinder: ClassDataFinder,
    konst annotationLoader: AnnotationLoader<AnnotationWithArgs>,
    konst virtualFileForDebug: VirtualFile,
    konst serializationProtocol: SerializerExtensionProtocol,
    konst classFinder: KotlinClassFinder? = null,
    konst jvmMetadataVersion: JvmMetadataVersion? = null
) {
    fun createContext(
        nameResolver: NameResolver,
        packageFqName: FqName,
        typeTable: TypeTable
    ): ClsStubBuilderContext =
        ClsStubBuilderContext(this, nameResolver, packageFqName, EmptyTypeParameters, typeTable, protoContainer = null)
}

interface TypeParameters {
    operator fun get(id: Int): Name

    fun child(nameResolver: NameResolver, innerTypeParameters: List<ProtoBuf.TypeParameter>) =
        TypeParametersImpl(nameResolver, innerTypeParameters, parent = this)
}

object EmptyTypeParameters : TypeParameters {
    override fun get(id: Int): Name = throw IllegalStateException("Unknown type parameter with id = $id")
}

class TypeParametersImpl(
    nameResolver: NameResolver,
    typeParameterProtos: Collection<ProtoBuf.TypeParameter>,
    private konst parent: TypeParameters
) : TypeParameters {
    private konst typeParametersById = typeParameterProtos.map { Pair(it.id, nameResolver.getName(it.name)) }.toMap()

    override fun get(id: Int): Name = typeParametersById[id] ?: parent[id]
}

class ClsStubBuilderContext(
    konst components: ClsStubBuilderComponents,
    konst nameResolver: NameResolver,
    konst containerFqName: FqName,
    konst typeParameters: TypeParameters,
    konst typeTable: TypeTable,
    konst protoContainer: ProtoContainer.Class?
)

internal fun ClsStubBuilderContext.child(
    typeParameterList: List<ProtoBuf.TypeParameter>,
    name: Name? = null,
    nameResolver: NameResolver = this.nameResolver,
    typeTable: TypeTable = this.typeTable,
    protoContainer: ProtoContainer.Class? = this.protoContainer
): ClsStubBuilderContext = ClsStubBuilderContext(
    this.components,
    nameResolver,
    if (name != null) this.containerFqName.child(name) else this.containerFqName,
    this.typeParameters.child(nameResolver, typeParameterList),
    typeTable,
    protoContainer
)
