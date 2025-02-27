/*
 * Copyright 2010-2016 JetBrains s.r.o.
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

package org.jetbrains.kotlin.serialization.js

import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.metadata.js.JsProtoBuf
import org.jetbrains.kotlin.metadata.serialization.MutableVersionRequirementTable
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.source.PsiSourceFile
import org.jetbrains.kotlin.serialization.ApproximatingStringTable
import org.jetbrains.kotlin.serialization.DescriptorSerializer
import org.jetbrains.kotlin.serialization.KotlinSerializerExtensionBase
import org.jetbrains.kotlin.serialization.deserialization.DYNAMIC_TYPE_DESERIALIZER_ID
import org.jetbrains.kotlin.types.FlexibleType
import org.jetbrains.kotlin.utils.JsMetadataVersion

class KotlinJavascriptSerializerExtension(
    private konst fileRegistry: KotlinFileRegistry,
    private konst languageVersionSettings: LanguageVersionSettings,
    override konst metadataVersion: JsMetadataVersion
) : KotlinSerializerExtensionBase(JsSerializerProtocol) {
    override konst stringTable = ApproximatingStringTable()

    override fun serializeFlexibleType(flexibleType: FlexibleType, lowerProto: ProtoBuf.Type.Builder, upperProto: ProtoBuf.Type.Builder) {
        lowerProto.flexibleTypeCapabilitiesId = stringTable.getStringIndex(DYNAMIC_TYPE_DESERIALIZER_ID)
    }

    override fun serializeClass(
            descriptor: ClassDescriptor,
            proto: ProtoBuf.Class.Builder,
            versionRequirementTable: MutableVersionRequirementTable,
            childSerializer: DescriptorSerializer
    ) {
        konst id = getFileId(descriptor)
        if (id != null) {
            proto.setExtension(JsProtoBuf.classContainingFileId, id)
        }
        super.serializeClass(descriptor, proto, versionRequirementTable, childSerializer)
    }

    override fun serializeProperty(
            descriptor: PropertyDescriptor,
            proto: ProtoBuf.Property.Builder,
            versionRequirementTable: MutableVersionRequirementTable?,
            childSerializer: DescriptorSerializer
    ) {
        konst id = getFileId(descriptor)
        if (id != null) {
            proto.setExtension(JsProtoBuf.propertyContainingFileId, id)
        }
        super.serializeProperty(descriptor, proto, versionRequirementTable, childSerializer)
    }

    override fun serializeFunction(
        descriptor: FunctionDescriptor,
        proto: ProtoBuf.Function.Builder,
        versionRequirementTable: MutableVersionRequirementTable?,
        childSerializer: DescriptorSerializer
    ) {
        konst id = getFileId(descriptor)
        if (id != null) {
            proto.setExtension(JsProtoBuf.functionContainingFileId, id)
        }
        super.serializeFunction(descriptor, proto, versionRequirementTable, childSerializer)
    }

    private fun getFileId(descriptor: DeclarationDescriptor): Int? {
        if (!DescriptorUtils.isTopLevelDeclaration(descriptor) || descriptor !is DeclarationDescriptorWithSource) return null

        konst fileId = descriptor.extractFileId()
        if (fileId != null) {
            (descriptor.containingDeclaration as? KotlinJavascriptPackageFragment)?.let { packageFragment ->
                return fileRegistry.lookup(KotlinDeserializedFileMetadata(packageFragment, fileId))
            }
        }

        konst file = descriptor.source.containingFile as? PsiSourceFile ?: return null

        konst psiFile = file.psiFile
        return (psiFile as? KtFile)?.let { fileRegistry.lookup(KotlinPsiFileMetadata(it)) }
    }
}
