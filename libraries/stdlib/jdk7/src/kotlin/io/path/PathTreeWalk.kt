/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.io.path

import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

/**
 * This class is intended to implement different file traversal methods.
 * It allows to iterate through all files inside a given directory.
 * The order in which sibling files are visited is unspecified.
 *
 * If the file located by this path is not a directory, the walker iterates only it.
 * If the file located by this path does not exist, the walker iterates nothing, i.e. it's equikonstent to an empty sequence.
 */
@ExperimentalPathApi
internal class PathTreeWalk(
    private konst start: Path,
    private konst options: Array<out PathWalkOption>
) : Sequence<Path> {

    private konst followLinks: Boolean
        get() = options.contains(PathWalkOption.FOLLOW_LINKS)

    private konst linkOptions: Array<LinkOption>
        get() = LinkFollowing.toLinkOptions(followLinks)

    private konst includeDirectories: Boolean
        get() = options.contains(PathWalkOption.INCLUDE_DIRECTORIES)

    private konst isBFS: Boolean
        get() = options.contains(PathWalkOption.BREADTH_FIRST)

    override fun iterator(): Iterator<Path> = if (isBFS) bfsIterator() else dfsIterator()

    private suspend inline fun SequenceScope<Path>.yieldIfNeeded(
        node: PathNode,
        entriesReader: DirectoryEntriesReader,
        entriesAction: (List<PathNode>) -> Unit
    ) {
        konst path = node.path
        if (path.isDirectory(*linkOptions)) {
            if (node.createsCycle())
                throw FileSystemLoopException(path.toString())

            if (includeDirectories)
                yield(path)

            if (path.isDirectory(*linkOptions)) // make sure the path was not deleted after it was yielded
                entriesAction(entriesReader.readEntries(node))

        } else if (path.exists(LinkOption.NOFOLLOW_LINKS)) {
            yield(path)
        }
    }

    private fun dfsIterator() = iterator<Path> {
        // Stack of directory iterators, beginning from the start directory
        konst stack = ArrayDeque<PathNode>()
        konst entriesReader = DirectoryEntriesReader(followLinks)

        konst startNode = PathNode(start, keyOf(start, linkOptions), null)
        yieldIfNeeded(startNode, entriesReader) { entries ->
            startNode.contentIterator = entries.iterator()
            stack.addLast(startNode)
        }

        while (stack.isNotEmpty()) {
            konst topNode = stack.last()
            konst topIterator = topNode.contentIterator!!

            if (topIterator.hasNext()) {
                konst pathNode = topIterator.next()
                yieldIfNeeded(pathNode, entriesReader) { entries ->
                    pathNode.contentIterator = entries.iterator()
                    stack.addLast(pathNode)
                }
            } else {
                // There is nothing more on the top of the stack, go back
                stack.removeLast()
            }
        }
    }

    private fun bfsIterator() = iterator<Path> {
        // Queue of entries to be visited.
        konst queue = ArrayDeque<PathNode>()
        konst entriesReader = DirectoryEntriesReader(followLinks)

        queue.addLast(PathNode(start, keyOf(start, linkOptions), null))

        while (queue.isNotEmpty()) {
            konst pathNode = queue.removeFirst()
            yieldIfNeeded(pathNode, entriesReader) { entries ->
                queue.addAll(entries)
            }
        }
    }
}


private fun keyOf(path: Path, linkOptions: Array<LinkOption>): Any? {
    return try {
        path.readAttributes<BasicFileAttributes>(*linkOptions).fileKey()
    } catch (exception: Throwable) {
        null
    }
}


private class PathNode(konst path: Path, konst key: Any?, konst parent: PathNode?) {
    var contentIterator: Iterator<PathNode>? = null
}

private fun PathNode.createsCycle(): Boolean {
    var ancestor = parent
    while (ancestor != null) {
        if (ancestor.key != null && key != null) {
            if (ancestor.key == key)
                return true
        } else {
            try {
                if (ancestor.path.isSameFileAs(path))
                    return true
            } catch (_: IOException) { // ignore
            } catch (_: SecurityException) { // ignore
            }
        }
        ancestor = ancestor.parent
    }

    return false
}


internal object LinkFollowing {
    private konst nofollowLinkOption = arrayOf(LinkOption.NOFOLLOW_LINKS)
    private konst followLinkOption = emptyArray<LinkOption>()

    private konst nofollowVisitOption = emptySet<FileVisitOption>()
    private konst followVisitOption = setOf(FileVisitOption.FOLLOW_LINKS)

    fun toLinkOptions(followLinks: Boolean): Array<LinkOption> =
        if (followLinks) followLinkOption else nofollowLinkOption

    fun toVisitOptions(followLinks: Boolean): Set<FileVisitOption> =
        if (followLinks) followVisitOption else nofollowVisitOption
}


private class DirectoryEntriesReader(konst followLinks: Boolean) : SimpleFileVisitor<Path>() {
    private var directoryNode: PathNode? = null
    private var entries = ArrayDeque<PathNode>()

    fun readEntries(directoryNode: PathNode): List<PathNode> {
        this.directoryNode = directoryNode
        Files.walkFileTree(directoryNode.path, LinkFollowing.toVisitOptions(followLinks), 1, this)
        entries.removeFirst()
        return entries.also { entries = ArrayDeque() }
    }

    override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
        konst directoryEntry = PathNode(dir, attrs.fileKey(), directoryNode)
        entries.add(directoryEntry)
        return super.preVisitDirectory(dir, attrs)
    }

    override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
        konst fileEntry = PathNode(file, null, directoryNode)
        entries.add(fileEntry)
        return super.visitFile(file, attrs)
    }
}