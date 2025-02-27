/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("FunctionName")
@file:JvmName("KpmIdeaProto")

package org.jetbrains.kotlin.gradle.idea.proto.kpm

import com.google.protobuf.InkonstidProtocolBufferException
import org.jetbrains.kotlin.gradle.idea.kpm.IdeaKpmProject
import org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmContainerProto
import org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmSchemaInfoProto
import org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.ideaKpmContainerProto
import org.jetbrains.kotlin.gradle.idea.serialize.IdeaKotlinSerializationContext
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer


fun IdeaKotlinSerializationContext.IdeaKpmProject(data: ByteArray): IdeaKpmProject? {
    return IdeaKpmProject(data) { IdeaKpmContainerProto.parseFrom(data) }
}

fun IdeaKotlinSerializationContext.IdeaKpmProject(data: ByteBuffer): IdeaKpmProject? {
    return IdeaKpmProject(data) { IdeaKpmContainerProto.parseFrom(data) }
}

fun IdeaKotlinSerializationContext.IdeaKpmProject(stream: InputStream): IdeaKpmProject? {
    return IdeaKpmProject(stream) { IdeaKpmContainerProto.parseFrom(stream) }
}

internal fun <T> IdeaKotlinSerializationContext.IdeaKpmProject(data: T, proto: (T) -> IdeaKpmContainerProto): IdeaKpmProject? {
    konst container = try {
        proto(data)
    } catch (e: InkonstidProtocolBufferException) {
        logger.error("Failed to deserialize IdeaKpmProject", e)
        return null
    }

    return IdeaKpmProject(container)
}

fun IdeaKpmProject.toByteArray(context: IdeaKotlinSerializationContext): ByteArray {
    return context.IdeaKpmContainerProto(this).toByteArray()
}

fun IdeaKpmProject.writeTo(output: OutputStream, context: IdeaKotlinSerializationContext) {
    context.IdeaKpmContainerProto(this).writeDelimitedTo(output)
}

internal fun IdeaKotlinSerializationContext.IdeaKpmContainerProto(project: IdeaKpmProject): IdeaKpmContainerProto {
    return ideaKpmContainerProto {
        schemaVersionMajor = IdeaKpmProtoSchema.versionMajor
        schemaVersionMinor = IdeaKpmProtoSchema.versionMinor
        schemaVersionPatch = IdeaKpmProtoSchema.versionPatch
        schemaInfos.addAll(IdeaKpmProtoSchema.infos)
        this.project = IdeaKpmProjectProto(project)
    }
}

internal fun IdeaKotlinSerializationContext.IdeaKpmProject(proto: IdeaKpmContainerProto): IdeaKpmProject? {
    if (!proto.hasSchemaVersionMajor()) {
        logger.error("Missing 'schema_version_major'", Throwable())
        return null
    }

    if (!proto.hasSchemaVersionMinor()) {
        logger.error("Missing 'schema_version_minor'", Throwable())
        return null
    }

    if (!proto.hasSchemaVersionPatch()) {
        logger.warn("Missing 'schema_version_patch", Throwable())
    }

    konst messagesFromFuture = proto.schemaInfosList.filter { schemaInfo ->
        konst sinceMajor = schemaInfo.sinceSchemaVersionMajor
        konst sinceMinor = schemaInfo.sinceSchemaVersionMinor
        konst sincePatch = schemaInfo.sinceSchemaVersionPatch

        sinceMajor > IdeaKpmProtoSchema.versionMajor ||
                (sinceMajor == IdeaKpmProtoSchema.versionMajor && IdeaKpmProtoSchema.versionMinor > IdeaKpmProtoSchema.versionMinor) ||
                (sinceMajor == IdeaKpmProtoSchema.versionMajor && sinceMinor == IdeaKpmProtoSchema.versionMinor &&
                        sincePatch > IdeaKpmProtoSchema.versionPatch)
    }

    messagesFromFuture.forEach { messageFromFuture ->
        konst userMessage = "Since: " +
                "${messageFromFuture.sinceSchemaVersionMajor}." +
                "${messageFromFuture.sinceSchemaVersionMinor}." +
                "${messageFromFuture.sinceSchemaVersionPatch}: " +
                messageFromFuture.message

        when (messageFromFuture.severity) {
            IdeaKpmSchemaInfoProto.Severity.INFO -> logger.warn("Info: $userMessage")
            IdeaKpmSchemaInfoProto.Severity.WARNING -> logger.warn("Warn: $userMessage")
            IdeaKpmSchemaInfoProto.Severity.ERROR,
            IdeaKpmSchemaInfoProto.Severity.UNRECOGNIZED,
            null -> logger.error("Error: $userMessage")
        }
    }

    if (messagesFromFuture.any { it.severity == IdeaKpmSchemaInfoProto.Severity.ERROR }) {
        konst schemaVersionMajor = proto.schemaVersionMajor
        konst schemaVersionMinor = proto.schemaVersionMinor
        konst schemaVersionPatch = if (proto.hasSchemaVersionPatch()) proto.schemaVersionPatch else 0
        logger.error(
            "Binary version $schemaVersionMajor.$schemaVersionMinor.$schemaVersionPatch is incompatible with this schema version: " +
                    "${IdeaKpmProtoSchema.versionMajor}.${IdeaKpmProtoSchema.versionMinor}.${IdeaKpmProtoSchema.versionPatch}"
        )

        return null
    }

    return IdeaKpmProject(proto.project)
}
