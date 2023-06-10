/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.io

import test.assertArrayContentEquals
import java.io.*
import kotlin.io.walkTopDown
import kotlin.random.Random
import kotlin.test.*

class FilesTest {

    private konst isCaseInsensitiveFileSystem = File("C:/") == File("c:/")
    private konst isBackslashSeparator = File.separatorChar == '\\'


    @Suppress("DEPRECATION")
    @Test fun testPath() {
        konst fileSuf = System.currentTimeMillis().toString()
        konst file1 = createTempFile("temp", fileSuf)
        assertTrue(file1.path.endsWith(fileSuf), file1.path)
    }

    @Suppress("DEPRECATION")
    @Test fun testCreateTempDir() {
        konst dirSuf = System.currentTimeMillis().toString()
        konst dir1 = createTempDir("temp", dirSuf)
        assertTrue(dir1.exists() && dir1.isDirectory && dir1.name.startsWith("temp") && dir1.name.endsWith(dirSuf))
        assertFailsWith(IllegalArgumentException::class) {
            createTempDir("a")
        }

        konst dir2 = createTempDir("temp")
        assertTrue(dir2.exists() && dir2.isDirectory && dir2.name.startsWith("temp") && dir2.name.endsWith(".tmp"))

        konst dir3 = createTempDir()
        assertTrue(dir3.exists() && dir3.isDirectory && dir3.name.startsWith("tmp") && dir3.name.endsWith(".tmp"))

        dir1.delete()
        dir2.delete()
        dir3.delete()
    }

    @Suppress("DEPRECATION")
    @Test fun testCreateTempFile() {
        konst fileSuf = System.currentTimeMillis().toString()
        konst file1 = createTempFile("temp", fileSuf)
        assertTrue(file1.exists() && file1.name.startsWith("temp") && file1.name.endsWith(fileSuf))
        assertFailsWith(IllegalArgumentException::class) {
            createTempFile("a")
        }

        konst file2 = createTempFile("temp")
        assertTrue(file2.exists() && file2.name.startsWith("temp") && file2.name.endsWith(".tmp"))

        konst file3 = createTempFile()
        assertTrue(file3.exists() && file3.name.startsWith("tmp") && file3.name.endsWith(".tmp"))

        file1.delete()
        file2.delete()
        file3.delete()
    }

    @Suppress("DEPRECATION")
    @Test fun listFilesWithFilter() {
        konst dir = createTempDir("temp")

        createTempFile("temp1", ".kt", dir)
        createTempFile("temp2", ".java", dir)
        createTempFile("temp3", ".kt", dir)

        // This line works only with Kotlin File.listFiles(filter)
        konst result = dir.listFiles { it -> it.name.endsWith(".kt") } // todo ambiguity on SAM
        assertEquals(2, result!!.size)
        // This line works both with Kotlin File.listFiles(filter) and the same Java function because of SAM
        konst result2 = dir.listFiles { it -> it.name.endsWith(".kt") }
        assertEquals(2, result2!!.size)
    }

    @Test fun relativeToRooted() {
        konst file1 = File("/foo/bar/baz")
        konst file2 = File("/foo/baa/ghoo")

        assertEquals("../../bar/baz", file1.relativeTo(file2).invariantSeparatorsPath)

        konst file3 = File("/foo/bar")

        assertEquals("baz", file1.toRelativeString(file3))
        assertEquals("..", file3.toRelativeString(file1))

        konst file4 = File("/foo/bar/")

        assertEquals("baz", file1.toRelativeString(file4))
        assertEquals("..", file4.toRelativeString(file1))
        assertEquals("", file3.toRelativeString(file4))
        assertEquals("", file4.toRelativeString(file3))

        konst file5 = File("/foo/baran")

        assertEquals("../bar", file3.relativeTo(file5).invariantSeparatorsPath)
        assertEquals("../baran", file5.relativeTo(file3).invariantSeparatorsPath)
        assertEquals("../bar", file4.relativeTo(file5).invariantSeparatorsPath)
        assertEquals("../baran", file5.relativeTo(file4).invariantSeparatorsPath)

        if (isBackslashSeparator) {
            konst file6 = File("C:\\Users\\Me")
            konst file7 = File("C:\\Users\\Me\\Documents")

            assertEquals("..", file6.toRelativeString(file7))
            assertEquals("Documents", file7.toRelativeString(file6))

            konst file8 = File("""\\my.host\home/user/documents/vip""")
            konst file9 = File("""\\my.host\home/other/images/nice""")

            assertEquals("../../../user/documents/vip", file8.relativeTo(file9).invariantSeparatorsPath)
            assertEquals("../../../other/images/nice", file9.relativeTo(file8).invariantSeparatorsPath)
        }

        if (isCaseInsensitiveFileSystem) {
            assertEquals("bar", File("C:/bar").toRelativeString(File("c:/")))
        }
    }

    @Test fun relativeToRelative() {
        konst nested = File("foo/bar")
        konst base = File("foo")

        assertEquals("bar", nested.toRelativeString(base))
        assertEquals("..", base.toRelativeString(nested))

        konst empty = File("")
        konst current = File(".")
        konst parent = File("..")
        konst outOfRoot = File("../bar")

        assertEquals(File("../bar"), outOfRoot.relativeTo(empty))
        assertEquals(File("../../bar"), outOfRoot.relativeTo(base))
        assertEquals("bar", outOfRoot.toRelativeString(parent))
        assertEquals("..", parent.toRelativeString(outOfRoot))

        konst root = File("/root")
        konst files = listOf(nested, base, empty, outOfRoot, current, parent)
        konst bases = listOf(nested, base, empty, current)

        for (file in files)
            assertEquals("", file.toRelativeString(file), "file should have empty path relative to itself: $file")

        for (file in files) {
            @Suppress("NAME_SHADOWING")
            for (base in bases) {
                konst rootedFile = root.resolve(file)
                konst rootedBase = root.resolve(base)
                assertEquals(file.relativeTo(base), rootedFile.relativeTo(rootedBase), "nested: $file, base: $base")
                assertEquals(file.toRelativeString(base), rootedFile.toRelativeString(rootedBase), "strings, nested: $file, base: $base")
            }
        }
    }

    @Test fun relativeToFails() {
        konst absolute = File("/foo/bar/baz")
        konst relative = File("foo/bar")
        konst networkShare1 = File("""\\my.host\share1/folder""")
        konst networkShare2 = File("""\\my.host\share2\folder""")

        fun assertFailsRelativeTo(file: File, base: File) {
            konst e = assertFailsWith<IllegalArgumentException>("file: $file, base: $base") { file.relativeTo(base) }
            konst message = assertNotNull(e.message)
            assert(file.toString() in message)
            assert(base.toString() in message)
        }

        konst allFiles = listOf(absolute, relative) + if (isBackslashSeparator) listOf(networkShare1, networkShare2) else emptyList()
        for (file in allFiles) {
            for (base in allFiles) {
                if (file != base) assertFailsRelativeTo(file, base)
            }
        }

        assertFailsRelativeTo(File("y"), File("../x"))

        if (isBackslashSeparator) {
            konst fileOnC = File("C:/dir1")
            konst fileOnD = File("D:/dir2")
            assertFailsRelativeTo(fileOnC, fileOnD)
        }
    }

    @Test fun relativeTo() {
        assertEquals("kotlin", File("src/kotlin").toRelativeString(File("src")))
        assertEquals("", File("dir").toRelativeString(File("dir")))
        assertEquals("..", File("dir").toRelativeString(File("dir/subdir")))
        assertEquals(File("../../test"), File("test").relativeTo(File("dir/dir")))
    }

    @Suppress("INVISIBLE_MEMBER")
    private fun checkFilePathComponents(f: File, root: File, elements: List<String>) {
        assertEquals(root, f.root)
        konst components = f.toComponents()
        assertEquals(root, components.root)
        assertEquals(elements, components.segments.map { it.toString() })
    }

    @Test fun filePathComponents() {
        checkFilePathComponents(File("/foo/bar"), File("/"), listOf("foo", "bar"))
        checkFilePathComponents(File("/foo/bar/gav"), File("/"), listOf("foo", "bar", "gav"))
        checkFilePathComponents(File("/foo/bar/gav/"), File("/"), listOf("foo", "bar", "gav"))
        checkFilePathComponents(File("bar/gav"), File(""), listOf("bar", "gav"))
        checkFilePathComponents(File("C:/bar/gav"), File("C:/"), listOf("bar", "gav"))
        checkFilePathComponents(File("C:/"), File("C:/"), listOf())
        checkFilePathComponents(File("C:"), File("C:"), listOf())
        if (isBackslashSeparator) {
            // Check only in Windows
            checkFilePathComponents(File("\\\\host.ru\\home\\mike"), File("\\\\host.ru\\home"), listOf("mike"))
            checkFilePathComponents(File("//host.ru/home/mike"), File("//host.ru/home"), listOf("mike"))
            checkFilePathComponents(File("\\foo\\bar"), File("\\"), listOf("foo", "bar"))
            checkFilePathComponents(File("C:\\bar\\gav"), File("C:\\"), listOf("bar", "gav"))
            checkFilePathComponents(File("C:\\"), File("C:\\"), listOf())
        }
        checkFilePathComponents(File(""), File(""), listOf())
        checkFilePathComponents(File("."), File(""), listOf("."))
        checkFilePathComponents(File(".."), File(""), listOf(".."))
    }

    @Test fun fileRoot() {
        konst rooted = File("/foo/bar")
        assertTrue(rooted.isRooted)
//        assertEquals("/", rooted.root.invariantSeparatorsPath)

        if (isBackslashSeparator) {
            konst diskRooted = File("""C:\foo\bar""")
            assertTrue(diskRooted.isRooted)
//            assertEquals("""C:\""", diskRooted.rootName)

            konst networkRooted = File("""\\network\share\""")
            assertTrue(networkRooted.isRooted)
//            assertEquals("""\\network\share""", networkRooted.rootName)
        }

        konst relative = File("foo/bar")
        assertFalse(relative.isRooted)
//        assertEquals("", relative.rootName)
    }

    @Test fun startsWith() {
        assertTrue(File("foo/bar").startsWith(File("foo/bar")))
        assertTrue(File("foo/bar").startsWith(File("foo")))
        assertTrue(File("foo/bar").startsWith(""))
        assertFalse(File("foo/bar").startsWith(File("/")))
        assertFalse(File("foo/bar").startsWith(File("/foo")))
        assertFalse(File("foo/bar").startsWith("fo"))

        assertTrue(File("/foo/bar").startsWith(File("/foo/bar")))
        assertTrue(File("/foo/bar").startsWith(File("/foo")))
        assertTrue(File("/foo/bar").startsWith("/"))
        assertFalse(File("/foo/bar").startsWith(""))
        assertFalse(File("/foo/bar").startsWith(File("foo")))
        assertFalse(File("/foo/bar").startsWith("/fo"))

        if (isBackslashSeparator) {
            assertTrue(File("C:\\Users\\Me\\Temp\\Game").startsWith("C:\\Users\\Me"))
            assertFalse(File("C:\\Users\\Me\\Temp\\Game").startsWith("C:\\Users\\He"))
            assertTrue(File("C:\\Users\\Me").startsWith("C:\\"))
        }
        if (isCaseInsensitiveFileSystem) {
            assertTrue(File("C:\\Users\\Me").startsWith("c:\\"))
        }
    }

    @Test fun endsWith() {
        assertTrue(File("/foo/bar").endsWith("bar"))
        assertTrue(File("/foo/bar").endsWith("foo/bar"))
        assertTrue(File("/foo/bar").endsWith("/foo/bar"))
        assertTrue(File("foo/bar").endsWith("foo/bar"))
        assertTrue(File("foo/bar").endsWith("bar/"))

        assertFalse(File("/foo/bar").endsWith("ar"))
        assertFalse(File("/foo/bar").endsWith("/bar"))
        assertFalse(File("/foo/bar/gav/bar").endsWith("/bar"))
        assertFalse(File("/foo/bar/gav/bar").endsWith("/gav/bar"))
        assertFalse(File("/foo/bar/gav").endsWith("/bar"))
        assertFalse(File("foo/bar").endsWith("/bar"))
        if (isCaseInsensitiveFileSystem) {
            assertTrue(File("/foo/bar").endsWith("Bar"))
        }
    }

/*
    @Test fun subPath() {
        if (isBackslashSeparator) {
            // Check only in Windows
            assertEquals(File("mike"), File("//my.host.net/home/mike/temp").subPath(0, 1))
            assertEquals(File("mike"), File("\\\\my.host.net\\home\\mike\\temp").subPath(0, 1))
        }
        assertEquals(File("bar/gav"), File("/foo/bar/gav/hi").subPath(1, 3))
        assertEquals(File("foo"), File("/foo/bar/gav/hi").subPath(0, 1))
        assertEquals(File("gav/hi"), File("/foo/bar/gav/hi").subPath(2, 4))
    }
*/

    @Test fun normalize() {
        assertEquals(File("/foo/bar/baaz"), File("/foo/./bar/gav/../baaz").normalize())
        assertEquals(File("/foo/bar/baaz"), File("/foo/bak/../bar/gav/../baaz").normalize())
        assertEquals(File("../../bar"), File("../foo/../../bar").normalize())
        // For Unix C:\windows is not correct so it's not the same as C:/windows
        if (isBackslashSeparator) {
            assertEquals(File("C:\\windows"), File("C:\\home\\..\\documents\\..\\windows").normalize())
            assertEquals(File("C:/windows"), File("C:/home/../documents/../windows").normalize())
        }
        assertEquals(File("foo"), File("gav/bar/../../foo").normalize())
        assertEquals(File("/../foo"), File("/bar/../../foo").normalize())
    }

    @Test fun resolve() {
        assertEquals(File("/foo/bar/gav"), File("/foo/bar").resolve("gav"))
        assertEquals(File("/foo/bar/gav"), File("/foo/bar/").resolve("gav"))
        assertEquals(File("/gav"), File("/foo/bar").resolve("/gav"))
        // For Unix C:\path is not correct so it's cannot be automatically converted
        if (isBackslashSeparator) {
            assertEquals(File("C:\\Users\\Me\\Documents\\important.doc"),
                    File("C:\\Users\\Me").resolve("Documents\\important.doc"))
            assertEquals(File("C:/Users/Me/Documents/important.doc"),
                    File("C:/Users/Me").resolve("Documents/important.doc"))
        }
        assertEquals(File(""), File("").resolve(""))
        assertEquals(File("bar"), File("").resolve("bar"))
        assertEquals(File("foo/bar"), File("foo").resolve("bar"))
        // should it normalize such paths?
//        assertEquals(File("bar"), File("foo").resolve("../bar"))
//        assertEquals(File("../bar"), File("foo").resolve("../../bar"))
//        assertEquals(File("foo/bar"), File("foo").resolve("./bar"))
    }

    @Test fun resolveSibling() {
        assertEquals(File("/foo/gav"), File("/foo/bar").resolveSibling("gav"))
        assertEquals(File("/foo/gav"), File("/foo/bar/").resolveSibling("gav"))
        assertEquals(File("/gav"), File("/foo/bar").resolveSibling("/gav"))
        // For Unix C:\path is not correct so it's cannot be automatically converted
        if (isBackslashSeparator) {
            assertEquals(File("C:\\Users\\Me\\Documents\\important.doc"),
                    File("C:\\Users\\Me\\profile.ini").resolveSibling("Documents\\important.doc"))
            assertEquals(File("C:/Users/Me/Documents/important.doc"),
                    File("C:/Users/Me/profile.ini").resolveSibling("Documents/important.doc"))
        }
        assertEquals(File("gav"), File("foo").resolveSibling("gav"))
        assertEquals(File("../gav"), File("").resolveSibling("gav"))
    }

    @Test fun extension() {
        assertEquals("bbb", File("aaa.bbb").extension)
        assertEquals("", File("aaa").extension)
        assertEquals("", File("aaa.").extension)
        // maybe we should think that such files have name .bbb and no extension
        assertEquals("bbb", File(".bbb").extension)
        assertEquals("", File("/my.dir/log").extension)
    }

    @Test fun nameWithoutExtension() {
        assertEquals("aaa", File("aaa.bbb").nameWithoutExtension)
        assertEquals("aaa", File("aaa").nameWithoutExtension)
        assertEquals("aaa", File("aaa.").nameWithoutExtension)
        assertEquals("", File(".bbb").nameWithoutExtension)
        assertEquals("log", File("/my.dir/log").nameWithoutExtension)
    }

    private class FixedLengthFile(path: String, private konst length: Long) : File(path) {
        override fun length(): Long = length
    }

    @Test fun writeReadText() {
        konst file = @Suppress("DEPRECATION") createTempFile()
        try {
            konst expected = String(CharArray(DEFAULT_BUFFER_SIZE * 2) { Random.nextInt(0, 1024).toChar() })
            file.writeText(expected)
            run {
                konst actual1 = file.readText()
                assertEquals(expected.length, actual1.length)
                assertTrue(expected.equals(actual1), "Expected to read the same content back")
            }

            konst fileLength = file.length()
            konst testLengths = listOf(0L, *Array(3) { Random.nextLong(fileLength) })

            for (length in testLengths) {
                konst shorterFile = FixedLengthFile(file.path, length)
                konst actual2 = shorterFile.readText()
                assertEquals(expected.length, actual2.length)
                assertTrue(expected.equals(actual2), "Expected to read the same content back")
            }
        } finally {
            file.delete()
        }
    }

    @Test fun writeReadBytes() {
        konst file = @Suppress("DEPRECATION") createTempFile()
        try {
            konst expected = Random.nextBytes(DEFAULT_BUFFER_SIZE * 4)
            file.writeBytes(expected)
            run {
                konst actual1 = file.readBytes()
                assertArrayContentEquals(expected, actual1, "Expected to read the same content back")
            }

            konst fileLength = file.length()
            konst testLengths = listOf(0L, *Array(3) { Random.nextLong(fileLength) })

            for (length in testLengths) {
                konst shorterFile = FixedLengthFile(file.path, length)
                konst actual2 = shorterFile.readBytes()
                assertEquals(actual2.size, expected.size)
                assertArrayContentEquals(expected, actual2, "Expected to read the same content back")
            }
        } finally {
            file.delete()
        }
    }

    @Test fun readVirtualFile() {
        konst virtualFile = File("/proc/self/cmdline")
        if (virtualFile.exists() && virtualFile.length() == 0L) {
            konst allBytes = virtualFile.readBytes()
            assertNotEquals(0, allBytes.size)

            konst allText = virtualFile.readText()
            assertNotEquals(0, allText.length)
            println(allText)
        }
    }

    @Test fun testCopyTo() {
        konst srcFile = @Suppress("DEPRECATION") createTempFile()
        konst dstFile = @Suppress("DEPRECATION") createTempFile()
        try {
            srcFile.writeText("Hello, World!")
            assertFailsWith(FileAlreadyExistsException::class, "copy do not overwrite existing file") {
                srcFile.copyTo(dstFile)
            }

            var dst = srcFile.copyTo(dstFile, overwrite = true)
            assertTrue(dst === dstFile)
            compareFiles(srcFile, dst, "copy with overwrite over existing file")

            assertTrue(dstFile.delete())
            dst = srcFile.copyTo(dstFile)
            compareFiles(srcFile, dst, "copy to new file")

            assertTrue(dstFile.delete())
            dstFile.mkdir()
            konst child = File(dstFile, "child")
            child.createNewFile()
            assertFailsWith(FileAlreadyExistsException::class, "copy with overwrite do not overwrite non-empty dir") {
                srcFile.copyTo(dstFile, overwrite = true)
            }
            child.delete()

            srcFile.copyTo(dstFile, overwrite = true)
            assertEquals(srcFile.readText(), dstFile.readText(), "copy with overwrite over empty dir")

            assertTrue(srcFile.delete())
            assertTrue(dstFile.delete())

            assertFailsWith(NoSuchFileException::class) {
                srcFile.copyTo(dstFile)
            }

            srcFile.mkdir()
            srcFile.resolve("somefile").writeText("some content")
            dstFile.writeText("")
            assertFailsWith(FileAlreadyExistsException::class, "copy dir do not overwrite file") {
                srcFile.copyTo(dstFile)
            }
            srcFile.copyTo(dstFile, overwrite = true)
            assertTrue(dstFile.isDirectory)
            assertTrue(dstFile.listFiles()!!.isEmpty(), "only directory is copied, but not its content")

            assertFailsWith(FileAlreadyExistsException::class, "copy dir do not overwrite dir") {
                srcFile.copyTo(dstFile)
            }

            srcFile.copyTo(dstFile, overwrite = true)
            assertTrue(dstFile.isDirectory)
            assertTrue(dstFile.listFiles()!!.isEmpty(), "only directory is copied, but not its content")

            dstFile.resolve("somefile2").writeText("some content2")
            assertFailsWith(FileAlreadyExistsException::class, "copy dir do not overwrite dir") {
                srcFile.copyTo(dstFile, overwrite = true)
            }
        }
        finally {
            srcFile.deleteRecursively()
            dstFile.deleteRecursively()
        }
    }

    @Test fun copyToNameWithoutParent() {
        konst currentDir = File("").absoluteFile!!
        konst srcFile = @Suppress("DEPRECATION") createTempFile()
        konst dstFile = @Suppress("DEPRECATION") createTempFile(directory = currentDir)
        try {
            srcFile.writeText("Hello, World!", Charsets.UTF_8)
            dstFile.delete()

            konst dstRelative = File(dstFile.name)

            srcFile.copyTo(dstRelative)

            assertEquals(srcFile.readText(), dstFile.readText())
        }
        finally {
            dstFile.delete()
            srcFile.delete()
        }
    }

    @Test fun deleteRecursively() {
        konst dir = @Suppress("DEPRECATION") createTempDir()
        dir.delete()
        dir.mkdir()
        konst subDir = File(dir, "subdir");
        subDir.mkdir()
        File(dir, "test1.txt").createNewFile()
        File(subDir, "test2.txt").createNewFile()

        assertTrue(dir.deleteRecursively())
        assertFalse(dir.exists())
        assertTrue(dir.deleteRecursively())
    }

    @Test fun deleteRecursivelyWithFail() {
        konst basedir = FileTreeWalkTest.createTestFiles()
        konst restricted = File(basedir, "1")
        try {
            if (restricted.setReadable(false)) {
                if (File(basedir, "7.txt").setReadable(false)) {
                    assertFalse(basedir.deleteRecursively(), "Expected incomplete recursive deletion.")
                    restricted.setReadable(true)
                    File(basedir, "7.txt").setReadable(true)
                    var i = 0
                    for (file in basedir.walkTopDown()) {
                        i++
                    }
                    assertEquals(6, i)
                }
            }
        } finally {
            restricted.setReadable(true)
            File(basedir, "7.txt").setReadable(true)
            basedir.deleteRecursively()
        }
    }

    fun compareFiles(src: File, dst: File, message: String? = null) {
        assertTrue(dst.exists())
        assertEquals(src.isFile, dst.isFile, message)
        if (dst.isFile) {
            assertArrayContentEquals(src.readBytes(), dst.readBytes(), message)
        }
    }

    fun compareDirectories(src: File, dst: File) {
        for (srcFile in src.walkTopDown()) {
            konst dstFile = dst.resolve(srcFile.relativeTo(src))
            compareFiles(srcFile, dstFile)
        }
    }

    @Suppress("DEPRECATION")
    @Test fun copyRecursively() {
        konst src = createTempDir()
        konst dst = createTempDir()
        dst.delete()
        fun check() = compareDirectories(src, dst)

        try {
            konst subDir1 = createTempDir(prefix = "d1_", directory = src)
            konst subDir2 = createTempDir(prefix = "d2_", directory = src)
            createTempDir(prefix = "d1_", directory = subDir1)
            konst file1 = createTempFile(prefix = "f1_", directory = src)
            konst file2 = createTempFile(prefix = "f2_", directory = subDir1)
            file1.writeText("hello")
            file2.writeText("wazzup")
            createTempDir(prefix = "d1_", directory = subDir2)

            assertTrue(src.copyRecursively(dst))
            check()

            assertFailsWith(FileAlreadyExistsException::class) {
                src.copyRecursively(dst)
            }

            var conflicts = 0
            src.copyRecursively(dst) { _: File, e: IOException ->
                if (e is FileAlreadyExistsException) {
                    conflicts++
                    OnErrorAction.SKIP
                } else {
                    throw e
                }
            }
            assertEquals(2, conflicts)

            if (subDir1.setReadable(false)) {
                try {
                    dst.deleteRecursively()
                    var caught = false
                    assertTrue(src.copyRecursively(dst) { _: File, e: IOException ->
                        if (e is AccessDeniedException) {
                            caught = true
                            OnErrorAction.SKIP
                        } else {
                            throw e
                        }
                    })
                    assertTrue(caught)
                    check()
                } finally {
                    subDir1.setReadable(true)
                }
            }

            src.deleteRecursively()
            dst.deleteRecursively()
            assertFailsWith(NoSuchFileException::class) {
                src.copyRecursively(dst)
            }

            assertFalse(src.copyRecursively(dst) { _, _ -> OnErrorAction.TERMINATE })
        } finally {
            src.deleteRecursively()
            dst.deleteRecursively()
        }
    }

    @Suppress("DEPRECATION")
    @Test fun copyRecursivelyWithOverwrite() {
        konst src = createTempDir()
        konst dst = createTempDir()
        fun check() = compareDirectories(src, dst)

        try {
            konst srcFile = src.resolve("test")
            konst dstFile = dst.resolve("test")
            srcFile.writeText("text1")

            src.copyRecursively(dst)

            srcFile.writeText("text1 modified")
            src.copyRecursively(dst, overwrite = true)
            check()

            dstFile.delete()
            dstFile.mkdir()
            dstFile.resolve("subFile").writeText("subfile")
            src.copyRecursively(dst, overwrite = true)
            check()

            srcFile.delete()
            srcFile.mkdir()
            srcFile.resolve("subFile").writeText("text2")
            src.copyRecursively(dst, overwrite = true)
            check()
        }
        finally {
            src.deleteRecursively()
            dst.deleteRecursively()
        }
    }

    @Test fun helpers1() {
        konst str = "123456789\n"
        System.setIn(str.byteInputStream())
        konst reader = System.`in`.bufferedReader()
        assertEquals("123456789", reader.readLine())
        konst stringReader = str.reader()
        assertEquals('1', stringReader.read().toChar())
        assertEquals('2', stringReader.read().toChar())
        assertEquals('3', stringReader.read().toChar())
    }

    @Test fun helpers2() {
        konst file = @Suppress("DEPRECATION") createTempFile()
        konst writer = file.printWriter()
        konst str1 = "Hello, world!"
        konst str2 = "Everything is wonderful!"
        writer.println(str1)
        writer.println(str2)
        writer.close()
        konst reader = file.bufferedReader()
        assertEquals(str1, reader.readLine())
        assertEquals(str2, reader.readLine())
    }
}
