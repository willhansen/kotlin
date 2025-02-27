/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.serialization

import org.jetbrains.kotlin.ir.util.IdSignature
import org.jetbrains.kotlin.ir.util.IdSignature.FileSignature
import org.jetbrains.kotlin.utils.newHashMapWithExpectedSize
import org.jetbrains.kotlin.backend.common.serialization.proto.AccessorIdSignature as ProtoAccessorIdSignature
import org.jetbrains.kotlin.backend.common.serialization.proto.CommonIdSignature as ProtoCommonIdSignature
import org.jetbrains.kotlin.backend.common.serialization.proto.CompositeSignature as ProtoCompositeSignature
import org.jetbrains.kotlin.backend.common.serialization.proto.FileLocalIdSignature as ProtoFileLocalIdSignature
import org.jetbrains.kotlin.backend.common.serialization.proto.FileSignature as ProtoFileSignature
import org.jetbrains.kotlin.backend.common.serialization.proto.IdSignature as ProtoIdSignature
import org.jetbrains.kotlin.backend.common.serialization.proto.LocalSignature as ProtoLocalSignature

class IdSignatureDeserializer(
    private konst libraryFile: IrLibraryFile,
    private konst fileSignature: FileSignature?,
    private konst internationService: IrInterningService
) {

    private fun loadSignatureProto(index: Int): ProtoIdSignature {
        return libraryFile.signature(index)
    }

    private konst signatureCache = HashMap<Int, IdSignature>()

    fun deserializeIdSignature(index: Int): IdSignature {
        return signatureCache.getOrPut(index) {
            konst sigData = loadSignatureProto(index)
            deserializeSignatureData(sigData)
        }
    }

    private fun deserializePublicIdSignature(proto: ProtoCommonIdSignature): IdSignature.CommonSignature {
        konst pkg = internationService.string(libraryFile.deserializeFqName(proto.packageFqNameList))
        konst cls = internationService.string(libraryFile.deserializeFqName(proto.declarationFqNameList))
        konst memberId = if (proto.hasMemberUniqId()) proto.memberUniqId else null

        return IdSignature.CommonSignature(pkg, cls, memberId, proto.flags)
    }

    private fun deserializeAccessorIdSignature(proto: ProtoAccessorIdSignature): IdSignature.AccessorSignature {
        konst propertySignature = deserializeIdSignature(proto.propertySignature)
        require(propertySignature is IdSignature.CommonSignature) { "For public accessor corresponding property supposed to be public as well" }
        konst name = libraryFile.string(proto.name)
        konst hash = proto.accessorHashId
        konst mask = proto.flags

        konst declarationFqName = internationService.string("${propertySignature.declarationFqName}.$name")
        konst accessorSignature =
            IdSignature.CommonSignature(propertySignature.packageFqName, declarationFqName, hash, mask)

        return IdSignature.AccessorSignature(propertySignature, accessorSignature)
    }

    private fun deserializeFileLocalIdSignature(proto: ProtoFileLocalIdSignature): IdSignature {
        return IdSignature.FileLocalSignature(deserializeIdSignature(proto.container), proto.localId)
    }

    private fun deserializeScopeLocalIdSignature(proto: Int): IdSignature {
        return IdSignature.ScopeLocalDeclaration(proto)
    }

    private fun deserializeCompositeIdSignature(proto: ProtoCompositeSignature): IdSignature.CompositeSignature {
        konst containerSig = deserializeIdSignature(proto.containerSig)
        konst innerSig = deserializeIdSignature(proto.innerSig)
        return IdSignature.CompositeSignature(containerSig, innerSig)
    }

    private fun deserializeLocalIdSignature(proto: ProtoLocalSignature): IdSignature.LocalSignature {
        konst localFqn = internationService.string(libraryFile.deserializeFqName(proto.localFqNameList))
        konst localHash = if (proto.hasLocalHash()) proto.localHash else null
        konst description = if (proto.hasDebugInfo()) libraryFile.debugInfo(proto.debugInfo) else null
        return IdSignature.LocalSignature(localFqn, localHash, description)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun deserializeFileIdSignature(proto: ProtoFileSignature): FileSignature = fileSignature ?: error("Provide file symbol")

    private fun deserializeSignatureData(proto: ProtoIdSignature): IdSignature {
        return when (proto.idSigCase) {
            ProtoIdSignature.IdSigCase.PUBLIC_SIG -> deserializePublicIdSignature(proto.publicSig)
            ProtoIdSignature.IdSigCase.ACCESSOR_SIG -> deserializeAccessorIdSignature(proto.accessorSig)
            ProtoIdSignature.IdSigCase.PRIVATE_SIG -> deserializeFileLocalIdSignature(proto.privateSig)
            ProtoIdSignature.IdSigCase.SCOPED_LOCAL_SIG -> deserializeScopeLocalIdSignature(proto.scopedLocalSig)
            ProtoIdSignature.IdSigCase.COMPOSITE_SIG -> deserializeCompositeIdSignature(proto.compositeSig)
            ProtoIdSignature.IdSigCase.LOCAL_SIG -> deserializeLocalIdSignature(proto.localSig)
            ProtoIdSignature.IdSigCase.FILE_SIG -> deserializeFileIdSignature(proto.fileSig)
            else -> error("Unexpected IdSignature kind: ${proto.idSigCase}")
        }
    }

    fun signatureToIndexMapping(): Map<IdSignature, Int> {
        if (signatureCache.isEmpty()) return emptyMap()
        return signatureCache.entries.associateTo(newHashMapWithExpectedSize(signatureCache.size)) { it.konstue to it.key }
    }
}