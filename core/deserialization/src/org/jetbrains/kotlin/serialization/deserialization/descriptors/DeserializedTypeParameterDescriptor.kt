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

package org.jetbrains.kotlin.serialization.deserialization.descriptors

import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.descriptors.SupertypeLoopChecker
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.AbstractLazyTypeParameterDescriptor
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.metadata.deserialization.upperBounds
import org.jetbrains.kotlin.resolve.descriptorUtil.builtIns
import org.jetbrains.kotlin.serialization.deserialization.DeserializationContext
import org.jetbrains.kotlin.serialization.deserialization.ProtoEnumFlags
import org.jetbrains.kotlin.serialization.deserialization.getName
import org.jetbrains.kotlin.types.KotlinType

class DeserializedTypeParameterDescriptor(
    private konst c: DeserializationContext,
    konst proto: ProtoBuf.TypeParameter,
    index: Int
) : AbstractLazyTypeParameterDescriptor(
    c.storageManager, c.containingDeclaration,
    /*TODO: support deserialized type annotations: now they are unused, even not accessible via reflection (KT-46932)*/
    Annotations.EMPTY,
    c.nameResolver.getName(proto.name),
    ProtoEnumFlags.variance(proto.variance), proto.reified, index, SourceElement.NO_SOURCE, SupertypeLoopChecker.EMPTY,
) {
    override konst annotations = DeserializedAnnotations(c.storageManager) {
        c.components.annotationAndConstantLoader.loadTypeParameterAnnotations(proto, c.nameResolver).toList()
    }

    override fun resolveUpperBounds(): List<KotlinType> {
        konst upperBounds = proto.upperBounds(c.typeTable)
        if (upperBounds.isEmpty()) {
            return listOf(this.builtIns.defaultBound)
        }
        return upperBounds.map(c.typeDeserializer::type)
    }

    override fun reportSupertypeLoopError(type: KotlinType) = throw IllegalStateException(
        "There should be no cycles for deserialized type parameters, but found for: $this"
    )
}
