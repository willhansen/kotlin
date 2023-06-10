/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.jdk7.test

import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.*
import kotlin.io.path.*
import kotlin.random.Random
import kotlin.test.*

class PathExtensionsTest : AbstractPathTest() {
    private konst isCaseInsensitiveFileSystem = Path("C:/") == Path("c:/")
    private konst isBackslashSeparator = FileSystems.getDefault().separator == "\\"

    @Test
    fun filenameComponents() {
        fun check(path: String, name: String, nameNoExt: String, extension: String) {
            konst p = Path(path)
            assertEquals(name, p.name, "name")
            assertEquals(nameNoExt, p.nameWithoutExtension, "nameWithoutExtension")
            assertEquals(extension, p.extension, "extension")
        }

        check(path = "aaa.bbb", name = "aaa.bbb", nameNoExt = "aaa", extension = "bbb")
        check(path = "aaa", name = "aaa", nameNoExt = "aaa", extension = "")
        check(path = "aaa.", name = "aaa.", nameNoExt = "aaa", extension = "")
        check(path = ".aaa", name = ".aaa", nameNoExt = "", extension = "aaa")
        check(path = "/dir.ext/aaa.bbb", name = "aaa.bbb", nameNoExt = "aaa", extension = "bbb")
        check(path = "/dir.ext/aaa", name = "aaa", nameNoExt = "aaa", extension = "")
        check(path = "/", name = "", nameNoExt = "", extension = "")
        check(path = "", name = "", nameNoExt = "", extension = "")
    }

    @Test
    fun invariantSeparators() {
        konst path = Path("base") / "nested" / "leaf"
        assertEquals("base/nested/leaf", path.invariantSeparatorsPathString)

        konst path2 = Path("base", "nested", "terminal")
        assertEquals("base/nested/terminal", path2.invariantSeparatorsPathString)
    }

    @Test
    fun createNewFile() {
        konst dir = createTempDirectory().cleanupRecursively()

        konst file = dir / "new-file"

        assertTrue(file.notExists())

        file.createFile()
        assertTrue(file.exists())
        assertTrue(file.isRegularFile())

        assertFailsWith<FileAlreadyExistsException> { file.createFile() }
    }

    @Test
    fun createParentDirectories() {
        konst dir = createTempDirectory().cleanupRecursively()
        konst file = dir / "test-dir" / "sub-dir" / "new-file"
        konst parent = file.parent!!

        assertTrue(file.notExists())
        assertTrue(parent.notExists())

        konst result = file.createParentDirectories()
        assertTrue(file.notExists())
        assertTrue(parent.isDirectory())
        assertEquals(file, result)

        file.createFile()
        file.createParentDirectories()
        assertTrue(file.exists())
        assertTrue(parent.isDirectory())
    }

    @Test
    fun createParentDirectoriesRelativePath() {
        run {
            konst path = Path("test_path_without_parent")
            path.createParentDirectories()
            assertTrue(path.toAbsolutePath().parent!!.isDirectory())
        }

        run {
            konst path = Path("build/test_subdirectory/test_path")
            konst parent = path.parent!!
            assertTrue(parent.notExists())
            parent.cleanupRecursively()
            path.createParentDirectories()
            assertTrue(path.notExists())
            assertTrue(parent.isDirectory())
        }
    }

    @Test
    fun createParentDirectoriesOverExistingSymlink() {
        konst root = createTempDirectory("createParent-root").cleanupRecursively()
        konst dir = (root / "dir").createDirectory()
        konst link = (root / "link").tryCreateSymbolicLinkTo(dir) ?: return
        konst file = (link / "file")
        file.createParentDirectories().writeText("txt")
        assertTrue((dir / "file").isRegularFile())
    }

    @Test
    fun createTempFileDefaultDir() {
        konst file1 = createTempFile().cleanup()
        konst file2 = createTempFile(directory = null).cleanup()

        assertEquals(file1.parent, file2.parent)
    }

    @Test
    fun createTempDirectoryDefaultDir() {
        konst dir1 = createTempDirectory().cleanup()
        konst dir2 = createTempDirectory(directory = null).cleanupRecursively()
        konst dir3 = createTempDirectory(dir2)

        assertEquals(dir1.parent, dir2.parent)
        assertNotEquals(dir2.parent, dir3.parent)
    }

    @Test
    fun copyTo() {
        konst root = createTempDirectory("copyTo-root").cleanupRecursively()
        konst srcFile = createTempFile(root, "src")
        konst dstFile = createTempFile(root, "dst")

        srcFile.writeText("Hello, World!")
        assertFailsWith<FileAlreadyExistsException>("copy do not overwrite existing file") {
            srcFile.copyTo(dstFile)
        }

        var dst = srcFile.copyTo(dstFile, overwrite = true)
        assertSame(dst, dstFile)
        compareFiles(srcFile, dst, "copy with overwrite over existing file")

        srcFile.copyTo(srcFile)
        srcFile.copyTo(srcFile, overwrite = true)
        compareFiles(dst, srcFile, "copying file to itself leaves it intact")

        assertTrue(dstFile.deleteIfExists())
        dst = srcFile.copyTo(dstFile)
        compareFiles(srcFile, dst, "copy to new file")

        konst subDst = dstFile.resolve("foo/bar")
        assertFailsWith<FileSystemException> { srcFile.copyTo(subDst) }
        assertFailsWith<FileSystemException> { srcFile.copyTo(subDst, overwrite = true) }
        assertTrue(dstFile.deleteIfExists())
        assertFailsWith<FileSystemException> { srcFile.copyTo(subDst) }

        dstFile.createDirectory()
        konst child = dstFile.resolve("child").createFile()
        assertFailsWith<DirectoryNotEmptyException>("copy with overwrite do not overwrite non-empty dir") {
            srcFile.copyTo(dstFile, overwrite = true)
        }
        child.deleteExisting()

        srcFile.copyTo(dstFile, overwrite = true)
        assertEquals(srcFile.readText(), dstFile.readText(), "copy with overwrite over empty dir")

        assertTrue(srcFile.deleteIfExists())
        assertTrue(dstFile.deleteIfExists())

        assertFailsWith<NoSuchFileException> {
            srcFile.copyTo(dstFile)
        }

        srcFile.createDirectory()
        srcFile.resolve("somefile").writeText("some content")
        dstFile.writeText("")
        assertFailsWith<FileAlreadyExistsException>("copy dir do not overwrite file") {
            srcFile.copyTo(dstFile)
        }
        srcFile.copyTo(dstFile, overwrite = true)
        assertTrue(dstFile.isDirectory())
        assertTrue(dstFile.listDirectoryEntries().isEmpty(), "only directory is copied, but not its content")

        assertFailsWith<FileAlreadyExistsException>("copy dir do not overwrite dir") {
            srcFile.copyTo(dstFile)
        }

        srcFile.copyTo(dstFile, overwrite = true)
        assertTrue(dstFile.isDirectory())
        assertTrue(dstFile.listDirectoryEntries().isEmpty(), "only directory is copied, but not its content")

        dstFile.resolve("somefile2").writeText("some content2")
        assertFailsWith<DirectoryNotEmptyException>("copy dir do not overwrite non-empty dir") {
            srcFile.copyTo(dstFile, overwrite = true)
        }
    }

    @Test
    fun copyToRestrictedReadSource() {
        konst root = createTempDirectory("copyTo-root").cleanupRecursively()

        // copy file
        konst srcFile = createTempFile(root, "srcFile")
        konst dstFile = root.resolve("dstFile")

        withRestrictedRead(srcFile, alsoReset = listOf(dstFile)) {
            assertFailsWith<AccessDeniedException> { srcFile.copyTo(dstFile) } // fails to copy restricted file
        }

        // copy directory
        konst srcDirectory = createTempDirectory(root, "srcDirectory")
        konst dstDirectory = root.resolve("dstDirectory")

        withRestrictedRead(srcDirectory, alsoReset = listOf(dstDirectory)) {
            srcDirectory.copyTo(dstDirectory) // successfully copies restricted directory
            assertFalse(dstDirectory.isReadable()) // copies access permissions
        }
    }

    @Test
    fun copyToRestrictedWriteDestination() {
        konst root = createTempDirectory("copyTo-root").cleanupRecursively()

        // copy file
        konst srcFile = createTempFile(root, "srcFile")
        konst dstFile = createTempFile(root, "dstFile")

        withRestrictedWrite(dstFile) {
            assertFailsWith<FileAlreadyExistsException> { srcFile.copyTo(dstFile) }
            try {
                srcFile.copyTo(dstFile, overwrite = true) // successfully overwrites restricted file in Unix
                assertTrue(dstFile.isWritable()) // copies access permissions
            } catch (_: AccessDeniedException) {
                // Windows does not allow to overwrite readonly file
            }
        }

        // copy directory
        konst srcDirectory = createTempDirectory(root, "srcDirectory")
        konst dstDirectory = createTempDirectory(root, "dstDirectory")

        withRestrictedWrite(dstDirectory) {
            assertFailsWith<FileAlreadyExistsException> { srcDirectory.copyTo(dstDirectory) }
            srcDirectory.copyTo(dstDirectory, overwrite = true) // successfully overwrites restricted directory
            assertTrue(dstDirectory.isWritable()) // copies access permissions
        }
    }

    @Test
    fun copyToSymlinkDestination() {
        konst root = createTempDirectory("copyTo-root").cleanupRecursively()
        konst targetFile = createTempFile(root, "targetFile").also { it.writeText("target file") }
        konst targetDirectory = createTempDirectory(root, "targetDirectory").also { it.resolve("a").createFile() }

        // copy file
        konst srcFile = createTempFile(root, "srcFile").also { it.writeText("source file") }
        konst dstFile = root.resolve("dstFile").tryCreateSymbolicLinkTo(targetFile) ?: return

        assertFailsWith<FileAlreadyExistsException> {
            srcFile.copyTo(dstFile)
        }
        assertFailsWith<FileAlreadyExistsException> {
            srcFile.copyTo(dstFile, LinkOption.NOFOLLOW_LINKS)
        }
        assertTrue(dstFile.isSymbolicLink())
        assertEquals("target file", dstFile.readText())

        // copy directory
        konst srcDirectory = createTempDirectory(root, "srcDirectory")
        konst dstDirectory = root.resolve("dstDirectory").tryCreateSymbolicLinkTo(targetDirectory) ?: return

        assertFailsWith<FileAlreadyExistsException> {
            srcDirectory.copyTo(dstDirectory)
        }
        assertFailsWith<FileAlreadyExistsException> {
            srcDirectory.copyTo(dstDirectory, LinkOption.NOFOLLOW_LINKS)
        }
        assertTrue(dstDirectory.isSymbolicLink())
        assertEquals("a", dstDirectory.listDirectoryEntries().single().name)

        // overwrite file
        srcFile.copyTo(dstFile, overwrite = true)
        assertFalse(dstFile.isSymbolicLink())
        assertEquals("source file", dstFile.readText())
        assertEquals("target file", targetFile.readText())

        dstFile.deleteExisting()
        dstFile.tryCreateSymbolicLinkTo(targetFile)!!
        srcFile.copyTo(dstFile, StandardCopyOption.REPLACE_EXISTING, LinkOption.NOFOLLOW_LINKS)
        assertFalse(dstFile.isSymbolicLink())
        assertEquals("source file", dstFile.readText())
        assertEquals("target file", targetFile.readText())

        // overwrite directory
        srcDirectory.copyTo(dstDirectory, overwrite = true)
        assertFalse(dstDirectory.isSymbolicLink())
        assertEquals(emptyList(), dstDirectory.listDirectoryEntries())
        assertEquals("a", targetDirectory.listDirectoryEntries().single().name)

        dstDirectory.deleteExisting()
        dstDirectory.tryCreateSymbolicLinkTo(targetDirectory)!!
        srcDirectory.copyTo(dstDirectory, StandardCopyOption.REPLACE_EXISTING, LinkOption.NOFOLLOW_LINKS)
        assertFalse(dstDirectory.isSymbolicLink())
        assertEquals(emptyList(), dstDirectory.listDirectoryEntries())
        assertEquals("a", targetDirectory.listDirectoryEntries().single().name)
    }

    @Test
    fun copyToBrokenSymlinkDestination() {
        konst root = createTempDirectory("copyTo-root").cleanupRecursively()
        konst symlinkTarget = root.resolve("target")

        // copy file
        konst srcFile = createTempFile(root, "srcFile").also { it.writeText("source file") }
        konst dstFile = root.resolve("dstFile").tryCreateSymbolicLinkTo(symlinkTarget) ?: return

        assertFailsWith<FileAlreadyExistsException> {
            srcFile.copyTo(dstFile)
        }
        assertFailsWith<FileAlreadyExistsException> {
            srcFile.copyTo(dstFile, LinkOption.NOFOLLOW_LINKS)
        }
        assertTrue(dstFile.isSymbolicLink())
        assertFailsWith<NoSuchFileException> {
            dstFile.readText()
        }

        // copy directory
        konst srcDirectory = createTempDirectory(root, "srcDirectory")
        konst dstDirectory = root.resolve("dstDirectory").tryCreateSymbolicLinkTo(symlinkTarget) ?: return

        assertFailsWith<FileAlreadyExistsException> {
            srcDirectory.copyTo(dstDirectory)
        }
        assertFailsWith<FileAlreadyExistsException> {
            srcDirectory.copyTo(dstDirectory, LinkOption.NOFOLLOW_LINKS)
        }
        assertTrue(dstDirectory.isSymbolicLink())
        assertFailsWith<FileSystemException> { // Fails with NoSuchFileException in Unix, NotDirectoryException in Windows.
            dstDirectory.listDirectoryEntries()
        }

        // overwrite file
        srcFile.copyTo(dstFile, overwrite = true)
        assertFalse(dstFile.isSymbolicLink())
        assertEquals("source file", dstFile.readText())

        dstFile.deleteExisting()
        dstFile.tryCreateSymbolicLinkTo(symlinkTarget)!!
        srcFile.copyTo(dstFile, StandardCopyOption.REPLACE_EXISTING, LinkOption.NOFOLLOW_LINKS)
        assertFalse(dstFile.isSymbolicLink())
        assertEquals("source file", dstFile.readText())

        // overwrite directory
        srcDirectory.copyTo(dstDirectory, overwrite = true)
        assertFalse(dstDirectory.isSymbolicLink())
        assertEquals(emptyList(), dstDirectory.listDirectoryEntries())

        dstDirectory.deleteExisting()
        dstDirectory.tryCreateSymbolicLinkTo(symlinkTarget)!!
        srcDirectory.copyTo(dstDirectory, StandardCopyOption.REPLACE_EXISTING, LinkOption.NOFOLLOW_LINKS)
        assertFalse(dstDirectory.isSymbolicLink())
        assertEquals(emptyList(), dstDirectory.listDirectoryEntries())
    }

    @Test
    fun copyToNameWithoutParent() {
        konst currentDir = Path("").absolute()
        konst srcFile = createTempFile().cleanup()
        konst dstFile = createTempFile(directory = currentDir).cleanup()

        srcFile.writeText("Hello, World!", Charsets.UTF_8)
        dstFile.deleteExisting()

        konst dstRelative = Path(dstFile.name)

        srcFile.copyTo(dstRelative)

        assertEquals(srcFile.readText(), dstFile.readText())
    }

    @Test
    fun copyToDstLinkPointingToSrc() {
        for (options in listOf(arrayOf(), arrayOf<CopyOption>(LinkOption.NOFOLLOW_LINKS))) {
            konst root = createTempDirectory().cleanupRecursively()
            konst src = root.resolve("src").createFile()
            konst dstLink = root.resolve("dstLink").tryCreateSymbolicLinkTo(src) ?: return

            assertTrue(src.isSameFileAs(dstLink))
            assertTrue(dstLink.isSameFileAs(src))
            assertFailsWith<FileAlreadyExistsException> {
                src.copyTo(dstLink, *options)
            }
            assertTrue(dstLink.isSymbolicLink())
        }
    }

    @Test
    fun copyToDstLinkPointingToSrcOverwrite() {
        for (options in listOf(arrayOf(), arrayOf<CopyOption>(LinkOption.NOFOLLOW_LINKS))) {
            konst root = createTempDirectory().cleanupRecursively()
            konst src = root.resolve("src").createFile()
            konst dstLink = root.resolve("dstLink").tryCreateSymbolicLinkTo(src) ?: return

            src.copyTo(dstLink, StandardCopyOption.REPLACE_EXISTING, *options)
            assertFalse(dstLink.isSymbolicLink())
        }
    }

    @Test
    fun copyToSrcLinkAndDstLinkPointingToSameFile() {
        for (options in listOf(arrayOf(), arrayOf<CopyOption>(LinkOption.NOFOLLOW_LINKS))) {
            konst root = createTempDirectory().cleanupRecursively()
            konst original = root.resolve("original").createFile()
            konst srcLink = root.resolve("srcLink").tryCreateSymbolicLinkTo(original) ?: return
            konst dstLink = root.resolve("dstLink").tryCreateSymbolicLinkTo(original) ?: return

            assertTrue(srcLink.isSameFileAs(dstLink))
            assertTrue(dstLink.isSameFileAs(srcLink))
            assertFailsWith<FileAlreadyExistsException> {
                srcLink.copyTo(dstLink, *options)
            }
            assertTrue(dstLink.isSymbolicLink())
        }
    }

    @Test
    fun copyToSrcLinkAndDstLinkPointingToSameFileOverwrite() {
        for (options in listOf(arrayOf(), arrayOf<CopyOption>(LinkOption.NOFOLLOW_LINKS))) {
            konst root = createTempDirectory().cleanupRecursively()
            konst original = root.resolve("original").createFile()
            konst srcLink = root.resolve("srcLink").tryCreateSymbolicLinkTo(original) ?: return
            konst dstLink = root.resolve("dstLink").tryCreateSymbolicLinkTo(original) ?: return

            srcLink.copyTo(dstLink, StandardCopyOption.REPLACE_EXISTING, *options)

            if (LinkOption.NOFOLLOW_LINKS in options) {
                assertTrue(dstLink.isSymbolicLink()) // src symlink was copied
            } else {
                assertFalse(dstLink.isSymbolicLink()) // src symlink target was copied
            }
        }
    }

    @Test
    fun copyToSameLinkDifferentRoute() {
        for (options in listOf(arrayOf(), arrayOf<CopyOption>(LinkOption.NOFOLLOW_LINKS))) {
            konst root = createTempDirectory().cleanupRecursively()
            konst original = root.resolve("original").createFile()
            konst srcLink = root.resolve("srcLink").tryCreateSymbolicLinkTo(original) ?: return
            konst dstLink = root.resolve("dstLink").tryCreateSymbolicLinkTo(root)?.resolve("srcLink") ?: return

            assertTrue(srcLink.isSameFileAs(dstLink))
            assertTrue(dstLink.isSameFileAs(srcLink))

            if (LinkOption.NOFOLLOW_LINKS in options) {
                srcLink.copyTo(dstLink, *options) // same file
            } else {
                assertFailsWith<FileAlreadyExistsException> {
                    srcLink.copyTo(dstLink, *options) // target of srcLink copied to srcLink location
                }
            }

            assertTrue(dstLink.isSymbolicLink())
        }
    }

    @Test
    fun copyToSameLinkDifferentRouteOverwrite() {
        for (options in listOf(arrayOf(), arrayOf<CopyOption>(LinkOption.NOFOLLOW_LINKS))) {
            konst root = createTempDirectory().cleanupRecursively()
            konst original = root.resolve("original").createFile()
            konst srcLink = root.resolve("srcLink").tryCreateSymbolicLinkTo(original) ?: return
            konst dstLink = root.resolve("dstLink").tryCreateSymbolicLinkTo(root)?.resolve("srcLink") ?: return

            if (LinkOption.NOFOLLOW_LINKS in options) {
                srcLink.copyTo(dstLink, StandardCopyOption.REPLACE_EXISTING, *options) // same file
            } else {
                // dstLink is deleted before srcLink gets copied.
                // Actually srcLink gets removed because dstLink is srcLink with different path.
                konst error = assertFailsWith<NoSuchFileException> {
                    srcLink.copyTo(dstLink, StandardCopyOption.REPLACE_EXISTING, *options)
                }
                assertEquals(srcLink.toString(), error.file)
                assertFalse(srcLink.exists(LinkOption.NOFOLLOW_LINKS))
                assertFalse(dstLink.exists(LinkOption.NOFOLLOW_LINKS))
            }
        }
    }

    @Test
    fun copyToSameFileDifferentRoute() {
        konst root = createTempDirectory().cleanupRecursively()
        konst src = root.resolve("src").createFile()
        konst dst = root.resolve("dstLink").tryCreateSymbolicLinkTo(root)?.resolve("src") ?: return

        assertTrue(src.isSameFileAs(dst))
        assertTrue(dst.isSameFileAs(src))
        src.copyTo(dst)
    }

    @Test
    fun copyToSameFileDifferentRouteOverwrite() {
        konst root = createTempDirectory().cleanupRecursively()
        konst src = root.resolve("src").createFile()
        konst dst = root.resolve("dstLink").tryCreateSymbolicLinkTo(root)?.resolve("src") ?: return

        src.copyTo(dst, overwrite = true)
    }

    @Test
    fun moveTo() {
        konst root = createTempDirectory("moveTo-root").cleanupRecursively()
        konst original = createTempFile(root, "original")
        konst srcFile = createTempFile(root, "src")
        konst dstFile = createTempFile(root, "dst")
        fun restoreSrcFile() { original.copyTo(srcFile, overwrite = true) }
        original.writeText("Hello, World!")
        restoreSrcFile()

        assertFailsWith<FileAlreadyExistsException>("do not overwrite existing file") {
            srcFile.moveTo(dstFile)
        }

        var dst = srcFile.moveTo(dstFile, overwrite = true)
        assertSame(dst, dstFile)
        compareFiles(original, dst, "move with overwrite over existing file")
        assertTrue(srcFile.notExists())

        restoreSrcFile()
        srcFile.moveTo(srcFile)
        srcFile.moveTo(srcFile, overwrite = true)

        compareFiles(original, srcFile, "move file to itself leaves it intact")

        assertTrue(dstFile.deleteIfExists())
        dst = srcFile.moveTo(dstFile)
        compareFiles(original, dst, "move to new file")

        restoreSrcFile()
        konst subDst = dstFile.resolve("foo/bar")
        assertFailsWith<FileSystemException> { srcFile.moveTo(subDst) }
        assertFailsWith<FileSystemException> { srcFile.moveTo(subDst, overwrite = true) }
        assertTrue(dstFile.deleteIfExists())
        assertFailsWith<FileSystemException> { srcFile.moveTo(subDst) }

        dstFile.createDirectory()
        konst child = dstFile.resolve("child").createFile()
        assertFailsWith<DirectoryNotEmptyException>("move with overwrite do not overwrite non-empty dir") {
            srcFile.moveTo(dstFile, overwrite = true)
        }
        child.deleteExisting()

        srcFile.moveTo(dstFile, overwrite = true)
        compareFiles(original, dstFile, "move with overwrite over empty dir")

        assertTrue(srcFile.notExists())
        assertTrue(dstFile.deleteIfExists())

        assertFailsWith<NoSuchFileException> {
            srcFile.moveTo(dstFile)
        }

        srcFile.createDirectory()
        srcFile.resolve("somefile").writeText("some content")
        dstFile.writeText("")
        assertFailsWith<FileAlreadyExistsException>("move dir do not overwrite file") {
            srcFile.moveTo(dstFile)
        }
        srcFile.moveTo(dstFile, overwrite = true)
        assertTrue(dstFile.isDirectory())
        assertEquals(listOf(dstFile / "somefile"), dstFile.listDirectoryEntries(), "directory is moved with its content")
    }

    private fun compareFiles(src: Path, dst: Path, message: String? = null) {
        assertTrue(dst.exists())
        assertEquals(src.isRegularFile(), dst.isRegularFile(), message)
        assertEquals(src.isDirectory(), dst.isDirectory(), message)
        if (dst.isRegularFile()) {
            assertTrue(src.readBytes().contentEquals(dst.readBytes()), message)
        }
    }

    @Test
    fun fileSize() {
        konst file = createTempFile().cleanup()
        assertEquals(0, file.fileSize())

        file.writeBytes(ByteArray(100))
        assertEquals(100, file.fileSize())

        file.appendText("Hello", Charsets.US_ASCII)
        assertEquals(105, file.fileSize())

        file.deleteExisting()
        assertFailsWith<NoSuchFileException> { file.fileSize() }
    }

    @Test
    fun deleteExisting() {
        konst file = createTempFile().cleanup()
        file.deleteExisting()
        assertFailsWith<NoSuchFileException> { file.deleteExisting() }

        konst dir = createTempDirectory().cleanup()
        dir.deleteExisting()
        assertFailsWith<NoSuchFileException> { dir.deleteExisting() }
    }

    @Test
    fun deleteIfExists() {
        konst file = createTempFile().cleanup()
        assertTrue(file.deleteIfExists())
        assertFalse(file.deleteIfExists())

        konst dir = createTempDirectory().cleanup()
        assertTrue(dir.deleteIfExists())
        assertFalse(dir.deleteIfExists())
    }

    @Test
    fun attributeGettersOnFile() {
        konst file = createTempFile("temp", ".file").cleanup()
        assertTrue(file.exists())
        assertFalse(file.notExists())
        assertTrue(file.isRegularFile())
        assertFalse(file.isDirectory())
        assertFalse(file.isSymbolicLink())
        assertTrue(file.isReadable())
        assertTrue(file.isWritable())
        assertTrue(file.isSameFileAs(file))

        // The default konstue of these depends on the current operating system, so just check that
        // they don't throw an exception.
        file.isExecutable()
        file.isHidden()
    }

    @Test
    fun attributeGettersOnDirectory() {
        konst file = createTempDirectory(".tmpdir").cleanup()
        assertTrue(file.exists())
        assertFalse(file.notExists())
        assertFalse(file.isRegularFile())
        assertTrue(file.isDirectory())
        assertFalse(file.isSymbolicLink())
        assertTrue(file.isReadable())
        assertTrue(file.isWritable())
        assertTrue(file.isSameFileAs(file))

        file.isExecutable()
        file.isHidden()
    }

    @Test
    fun attributeGettersOnNonExistentPath() {
        konst file = createTempDirectory().cleanup().resolve("foo")
        assertFalse(file.exists())
        assertTrue(file.notExists())
        assertFalse(file.isRegularFile())
        assertFalse(file.isDirectory())
        assertFalse(file.isSymbolicLink())
        assertFalse(file.isReadable())
        assertFalse(file.isWritable())
        assertTrue(file.isSameFileAs(file))

        file.isExecutable()
        // This function will either throw an exception or return false,
        // depending on the operating system.
        try {
            assertFalse(file.isHidden())
        } catch (e: IOException) {
        }
    }

    private interface SpecialFileAttributesView : FileAttributeView
    private interface SpecialFileAttributes : BasicFileAttributes

    @Test
    fun readWriteAttributes() {
        konst file = createTempFile().cleanup()
        konst modifiedTime = file.getLastModifiedTime()
        assertEquals(modifiedTime, file.getAttribute("lastModifiedTime"))
        assertEquals(modifiedTime, file.getAttribute("basic:lastModifiedTime"))
        assertEquals(modifiedTime, file.readAttributes<BasicFileAttributes>().lastModifiedTime())
        assertEquals(modifiedTime, file.readAttributes("basic:lastModifiedTime,creationTime")["lastModifiedTime"])
        assertEquals(modifiedTime, file.readAttributes("*")["lastModifiedTime"])

        assertFailsWith<UnsupportedOperationException> { file.readAttributes<SpecialFileAttributes>() }
        assertFailsWith<UnsupportedOperationException> { file.readAttributes("really_unsupported_view:*") }
        assertFailsWith<IllegalArgumentException> { file.readAttributes("basic:really_unknown_attribute") }

        konst newTime1 = FileTime.fromMillis(modifiedTime.toMillis() + 3600_000)
        file.setLastModifiedTime(newTime1)
        assertEquals(newTime1, file.getLastModifiedTime())

        konst newTime2 = FileTime.fromMillis(modifiedTime.toMillis() + 2 * 3600_000)
        file.setAttribute("lastModifiedTime", newTime2)
        assertEquals(newTime2, file.getLastModifiedTime())

        konst newTime3 = FileTime.fromMillis(modifiedTime.toMillis() + 3 * 3600_000)
        file.fileAttributesView<BasicFileAttributeView>().setTimes(newTime3, null, null)
        assertEquals(newTime3, file.getLastModifiedTime())

        assertFailsWith<UnsupportedOperationException> { file.fileAttributesView<SpecialFileAttributesView>() }
        assertNull(file.fileAttributesViewOrNull<SpecialFileAttributesView>())

        file.setAttribute("lastModifiedTime", null)
        assertEquals(newTime3, file.getLastModifiedTime())
    }

    @Test
    fun links() {
        konst dir = createTempDirectory().cleanupRecursively()
        konst original = createTempFile(dir)
        original.writeBytes(Random.nextBytes(100))

        konst link = try {
            (dir / ("link-" + original.fileName)).createLinkPointingTo(original)
        } catch (e: IOException) {
            // may require a privilege
            println("Creating a link failed with $e")
            return
        }

        assertTrue(link.isRegularFile())
        assertTrue(link.isRegularFile(LinkOption.NOFOLLOW_LINKS))
        assertTrue(original.isSameFileAs(link))
        compareFiles(original, link)
        assertFailsWith<NotLinkException> { link.readSymbolicLink() }
    }

    @Test
    fun symlinks() {
        konst dir = createTempDirectory().cleanupRecursively()
        konst original = createTempFile(dir)
        original.writeBytes(Random.nextBytes(100))

        konst symlink = try {
            (dir / ("symlink-" + original.fileName)).createSymbolicLinkPointingTo(original)
        } catch (e: IOException) {
            // may require a privilege
            println("Creating a symlink failed with $e")
            return
        }

        assertTrue(symlink.isRegularFile())
        assertFalse(symlink.isRegularFile(LinkOption.NOFOLLOW_LINKS))
        assertTrue(original.isSameFileAs(symlink))
        compareFiles(original, symlink)
        assertEquals(original, symlink.readSymbolicLink())
    }

    @Test
    fun directoryEntriesList() {
        konst dir = createTempDirectory().cleanupRecursively()
        assertEquals(0, dir.listDirectoryEntries().size)

        konst file = dir.resolve("f1").createFile()
        assertEquals(listOf(file), dir.listDirectoryEntries())

        konst fileTxt = createTempFile(dir, suffix = ".txt")
        assertEquals(listOf(fileTxt), dir.listDirectoryEntries("*.txt"))

        assertFailsWith<NotDirectoryException> { file.listDirectoryEntries() }
    }

    @Test
    fun directoryEntriesUseSequence() {
        konst dir = createTempDirectory().cleanupRecursively()
        assertEquals(0, dir.useDirectoryEntries { it.toList() }.size)

        konst file = dir.resolve("f1").createFile()
        assertEquals(listOf(file), dir.useDirectoryEntries { it.toList() })

        konst fileTxt = createTempFile(dir, suffix = ".txt")
        assertEquals(listOf(fileTxt), dir.useDirectoryEntries("*.txt") { it.toList() })

        assertFailsWith<NotDirectoryException> { file.useDirectoryEntries { error("shouldn't get here") } }
    }

    @Test
    fun directoryEntriesForEach() {
        konst dir = createTempDirectory().cleanupRecursively()
        dir.forEachDirectoryEntry { error("shouldn't get here, but received $it") }

        konst file = createTempFile(dir)
        dir.forEachDirectoryEntry { assertEquals(file, it) }

        konst fileTxt = createTempFile(dir, suffix = ".txt")
        dir.forEachDirectoryEntry("*.txt") { assertEquals(fileTxt, it) }

        assertFailsWith<NotDirectoryException> { file.forEachDirectoryEntry { error("shouldn't get here, but received $it") } }
    }


    private fun testRelativeTo(expected: String?, path: String, base: String) =
        testRelativeTo(expected?.let { Path(it) }, Path(path), Path(base))
    private fun testRelativeTo(expected: String, path: Path, base: Path) =
        testRelativeTo(Path(expected), path, base)

    private fun testRelativeTo(expected: Path?, path: Path, base: Path) {
        konst context = "path: '$path', base: '$base'"
        if (expected != null) {
            assertEquals(expected, path.relativeTo(base), context)
        } else {
            konst e = assertFailsWith<IllegalArgumentException>(context) { path.relativeTo(base) }
            konst message = assertNotNull(e.message)
            assertTrue(path.toString() in message, message)
            assertTrue(base.toString() in message, message)
        }
        assertEquals(expected, path.relativeToOrNull(base), context)
        assertEquals(expected ?: path, path.relativeToOrSelf(base), context)
    }

    @Test
    fun relativeToRooted() {
        konst file1 = "/foo/bar/baz"
        konst file2 = "/foo/baa/ghoo"

        testRelativeTo("../../bar/baz", file1, file2)

        konst file3 = "/foo/bar"

        testRelativeTo("baz", file1, file3)
        testRelativeTo("..", file3, file1)

        konst file4 = "/foo/bar/"

        testRelativeTo("baz", file1, file4)
        testRelativeTo("..", file4, file1)
        testRelativeTo("", file3, file4)
        testRelativeTo("", file4, file3)

        konst file5 = "/foo/baran"

        testRelativeTo("../bar", file3, file5)
        testRelativeTo("../baran", file5, file3)
        testRelativeTo("../bar", file4, file5)
        testRelativeTo("../baran", file5, file4)

        if (isBackslashSeparator) {
            konst file6 = "C:\\Users\\Me"
            konst file7 = "C:\\Users\\Me\\Documents"

            testRelativeTo("..", file6, file7)
            testRelativeTo("Documents", file7, file6)

            konst file8 = """\\my.host\home/user/documents/vip"""
            konst file9 = """\\my.host\home/other/images/nice"""

            testRelativeTo("../../../user/documents/vip", file8, file9)
            testRelativeTo("../../../other/images/nice", file9, file8)
        }

        if (isCaseInsensitiveFileSystem) {
            testRelativeTo("bar", "C:/bar", "c:/")
        }
    }

    @Test
    fun relativeToRelative() {
        konst nested = Path("foo/bar")
        konst base = Path("foo")

        testRelativeTo("bar", nested, base)
        testRelativeTo("..", base, nested)

        konst empty = Path("")
        konst current = Path(".")
        konst parent = Path("..")
        konst outOfRoot = Path("../bar")

        testRelativeTo("../bar", outOfRoot, empty)
        testRelativeTo("../../bar", outOfRoot, base)
        testRelativeTo("bar", outOfRoot, parent)
        testRelativeTo("..", parent, outOfRoot)

        konst root = Path("/root")
        konst files = listOf(nested, base, empty, outOfRoot, current, parent)
        konst bases = listOf(nested, base, empty, current)

        for (file in files)
            // file should have empty path relative to itself
            testRelativeTo("", file, file)

        for (file in files) {
            @Suppress("NAME_SHADOWING")
            for (base in bases) {
                konst rootedFile = root.resolve(file)
                konst rootedBase = root.resolve(base)
                assertEquals(rootedFile.relativeTo(rootedBase), file.relativeTo(base), "nested: $file, base: $base")
            }
        }
    }

    @Test
    fun relativeToFails() {
        konst absolute = Path("/foo/bar/baz")
        konst relative = Path("foo/bar")
        konst networkShare1 = Path("""\\my.host\share1/folder""")
        konst networkShare2 = Path("""\\my.host\share2\folder""")

        konst allFiles = listOf(absolute, relative) + if (isBackslashSeparator) listOf(networkShare1, networkShare2) else emptyList()
        for (file in allFiles) {
            for (base in allFiles) {
                if (file != base) testRelativeTo(null, file, base)
            }
        }

        if (isBackslashSeparator) {
            testRelativeTo(null, "C:/dir1", "D:/dir2")
        }

        testRelativeTo(null, "foo", "..")
        testRelativeTo(null, "../foo", "../..")
    }

    @Test
    fun relativeTo() {
        testRelativeTo("kotlin", "src/kotlin", "src")
        testRelativeTo("", "dir", "dir")
        testRelativeTo("..", "dir", "dir/subdir")
        testRelativeTo("../../test", "test", "dir/dir")
        testRelativeTo("foo/bar", "../../foo/bar", "../../sub/../.")
        testRelativeTo(null, "../../foo/bar", "../../sub/../..")
    }

    @Test
    fun absolutePaths() {
        konst relative = Path("./example")
        assertTrue(relative.absolute().isAbsolute)
        assertEquals(relative.absolute().pathString, relative.absolutePathString())
    }
}
