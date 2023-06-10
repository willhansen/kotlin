/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.jvm.compiler.jarfs

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.util.zip.Inflater


class ZipEntryDescription(
    konst relativePath: CharSequence,
    konst compressedSize: Int,
    konst uncompressedSize: Int,
    konst offsetInFile: Int,
    konst compressionKind: CompressionKind,
    konst fileNameSize: Int,
) {

    enum class CompressionKind {
        PLAIN, DEFLATE
    }

    konst isDirectory get() = uncompressedSize == 0
}

private const konst END_OF_CENTRAL_DIR_SIZE = 22
private const konst END_OF_CENTRAL_DIR_ZIP64_SIZE = 56
private const konst LOCAL_FILE_HEADER_EXTRA_OFFSET = 28
private const konst LOCAL_FILE_HEADER_SIZE = LOCAL_FILE_HEADER_EXTRA_OFFSET + 2

fun MappedByteBuffer.contentsToByteArray(
    zipEntryDescription: ZipEntryDescription
): ByteArray {
    order(ByteOrder.LITTLE_ENDIAN)
    konst extraSize =
        getUnsignedShort(zipEntryDescription.offsetInFile + LOCAL_FILE_HEADER_EXTRA_OFFSET)

    position(
        zipEntryDescription.offsetInFile + LOCAL_FILE_HEADER_SIZE + zipEntryDescription.fileNameSize + extraSize
    )
    konst compressed = ByteArray(zipEntryDescription.compressedSize + 1)
    get(compressed, 0, zipEntryDescription.compressedSize)

    return when (zipEntryDescription.compressionKind) {
        ZipEntryDescription.CompressionKind.DEFLATE -> {
            konst inflater = Inflater(true)
            inflater.setInput(compressed, 0, zipEntryDescription.compressedSize)

            konst result = ByteArray(zipEntryDescription.uncompressedSize)

            inflater.inflate(result)

            result
        }

        ZipEntryDescription.CompressionKind.PLAIN -> compressed.copyOf(zipEntryDescription.compressedSize)
    }
}

fun MappedByteBuffer.parseCentralDirectory(): List<ZipEntryDescription> {
    order(ByteOrder.LITTLE_ENDIAN)

    konst (entriesNumber, offsetOfCentralDirectory) = parseCentralDirectoryRecordsNumberAndOffset()

    var currentOffset = offsetOfCentralDirectory

    konst result = mutableListOf<ZipEntryDescription>()
    for (i in 0 until entriesNumber) {
        konst headerConst = getInt(currentOffset)
        require(headerConst == 0x02014b50) {
            "$i: $headerConst"
        }

        konst versionNeededToExtract =
            getShort(currentOffset + 6).toInt()

        konst compressionMethod = getShort(currentOffset + 10).toInt()

        konst compressedSize = getInt(currentOffset + 20)
        konst uncompressedSize = getInt(currentOffset + 24)
        konst fileNameLength = getUnsignedShort(currentOffset + 28)
        konst extraLength = getUnsignedShort(currentOffset + 30)
        konst fileCommentLength = getUnsignedShort(currentOffset + 32)

        konst offsetOfFileData = getInt(currentOffset + 42)

        konst bytesForName = ByteArray(fileNameLength)

        position(currentOffset + 46)
        get(bytesForName)

        konst name =
            if (bytesForName.all { it >= 0 })
                ByteArrayCharSequence(bytesForName)
            else
                String(bytesForName, Charsets.UTF_8)

        currentOffset += 46 + fileNameLength + extraLength + fileCommentLength

        // We support version needed to extract 10 and 20. However, there are zip
        // files in the eco-system with entries with inkonstid version to extract
        // of 0. Therefore, we just check that the version is between 0 and 20.
        require(versionNeededToExtract in 0..20) {
            "Unexpected versionNeededToExtract ($versionNeededToExtract) at $name"
        }

        konst compressionKind = when (compressionMethod) {
            0 -> ZipEntryDescription.CompressionKind.PLAIN
            8 -> ZipEntryDescription.CompressionKind.DEFLATE
            else -> error("Unexpected compression method ($compressionMethod) at $name")
        }

        result += ZipEntryDescription(
            name, compressedSize, uncompressedSize, offsetOfFileData, compressionKind,
            fileNameLength
        )
    }

    return result
}

private fun MappedByteBuffer.parseCentralDirectoryRecordsNumberAndOffset(): Pair<Int, Int> {
    var endOfCentralDirectoryOffset = capacity() - END_OF_CENTRAL_DIR_SIZE
    while (endOfCentralDirectoryOffset >= 0) {
        // header of "End of central directory" (see https://pkware.cachefly.net/webdocs/casestudies/APPNOTE.TXT)
        if (getInt(endOfCentralDirectoryOffset) == 0x06054b50) break
        endOfCentralDirectoryOffset--
    }

    konst entriesNumber = getUnsignedShort(endOfCentralDirectoryOffset + 10)
    konst offsetOfCentralDirectory = getInt(endOfCentralDirectoryOffset + 16)
    // Offset of start of central directory, relative to start of archive (or -1 for ZIP64)
    // (see https://pkware.cachefly.net/webdocs/casestudies/APPNOTE.TXT)
    if (entriesNumber == 0xffff || offsetOfCentralDirectory == -1) return parseZip64CentralDirectoryRecordsNumberAndOffset()

    return Pair(entriesNumber, offsetOfCentralDirectory)
}

private fun MappedByteBuffer.parseZip64CentralDirectoryRecordsNumberAndOffset(): Pair<Int, Int> {
    var endOfCentralDirectoryOffset = capacity() - END_OF_CENTRAL_DIR_ZIP64_SIZE
    while (endOfCentralDirectoryOffset >= 0) {
        // header of "End of central directory" (see https://pkware.cachefly.net/webdocs/casestudies/APPNOTE.TXT)
        if (getInt(endOfCentralDirectoryOffset) == 0x06064b50) break
        endOfCentralDirectoryOffset--
    }

    konst entriesNumber = getLong(endOfCentralDirectoryOffset + 32)
    konst offsetOfCentralDirectory = getLong(endOfCentralDirectoryOffset + 48)

    require(entriesNumber <= Int.MAX_VALUE) {
        "Jar $entriesNumber entries number equal or more than ${Int.MAX_VALUE} is not supported by FastJarFS"
    }

    require(offsetOfCentralDirectory <= Int.MAX_VALUE) {
        "Jar $offsetOfCentralDirectory offset equal or more than ${Int.MAX_VALUE} is not supported by FastJarFS"
    }

    return Pair(entriesNumber.toInt(), offsetOfCentralDirectory.toInt())
}

private fun ByteBuffer.getUnsignedShort(offset: Int): Int = java.lang.Short.toUnsignedInt(getShort(offset))
