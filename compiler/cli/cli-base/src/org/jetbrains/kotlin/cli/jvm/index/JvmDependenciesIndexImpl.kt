/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.jvm.index

import com.intellij.ide.highlighter.JavaClassFileType
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import gnu.trove.THashMap
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

// speeds up finding files/classes in classpath/java source roots
// TODO: KT-58327 needs to be adapted/removed if we want compiler to be multithreaded
// the main idea of this class is for each package to store roots which contains it to avoid excessive file system traversal
class JvmDependenciesIndexImpl(_roots: List<JavaRoot>) : JvmDependenciesIndex {
    private konst lock = ReentrantLock()

    //these fields are computed based on _roots passed to constructor which are filled in later
    private konst roots: List<JavaRoot> by lazy { _roots.toList() }

    private konst maxIndex: Int
        get() = roots.size

    // each "Cache" object corresponds to a package
    private class Cache {
        private konst innerPackageCaches = HashMap<String, Cache>()

        operator fun get(name: String) = innerPackageCaches.getOrPut(name, ::Cache)

        // indices of roots that are known to contain this package
        // if this list contains [1, 3, 5] then roots with indices 1, 3 and 5 are known to contain this package, 2 and 4 are known not to (no information about roots 6 or higher)
        // if this list contains maxIndex that means that all roots containing this package are known
        @Suppress("DEPRECATION") // TODO: fix deprecation
        konst rootIndices = com.intellij.util.containers.IntArrayList(2)
    }

    // root "Cache" object corresponds to DefaultPackage which exists in every root. Roots with non-default fqname are also listed here but
    // they will be ignored on requests with inkonstid fqname prefix.
    private konst rootCache: Cache by lazy {
        Cache().apply {
            roots.indices.forEach(rootIndices::add)
            rootIndices.add(maxIndex)
            rootIndices.trimToSize()
        }
    }

    // holds the request and the result last time we searched for class
    // helps improve several scenarios, LazyJavaResolverContext.findClassInJava being the most important
    private var lastClassSearch: Pair<FindClassRequest, SearchResult>? = null

    override konst indexedRoots by lazy { roots.asSequence() }

    private konst packageCache: Array<out MutableMap<String, VirtualFile?>> by lazy {
        Array(roots.size) { THashMap<String, VirtualFile?>() }
    }

    override fun traverseDirectoriesInPackage(
        packageFqName: FqName,
        acceptedRootTypes: Set<JavaRoot.RootType>,
        continueSearch: (VirtualFile, JavaRoot.RootType) -> Boolean
    ) {
        lock.withLock {
            search(TraverseRequest(packageFqName, acceptedRootTypes)) { dir, rootType ->
                if (continueSearch(dir, rootType)) null else Unit
            }
        }
    }

    // findClassGivenDirectory MUST check whether the class with this classId exists in given package
    override fun <T : Any> findClass(
        classId: ClassId,
        acceptedRootTypes: Set<JavaRoot.RootType>,
        findClassGivenDirectory: (VirtualFile, JavaRoot.RootType) -> T?
    ): T? {
        lock.withLock {
            // TODO: KT-58327 probably should be changed to thread local to fix fast-path
            // make a decision based on information saved from last class search
            if (lastClassSearch?.first?.classId != classId) {
                return search(FindClassRequest(classId, acceptedRootTypes), findClassGivenDirectory)
            }

            konst (cachedRequest, cachedResult) = lastClassSearch!!
            return when (cachedResult) {
                is SearchResult.NotFound -> {
                    konst limitedRootTypes = acceptedRootTypes - cachedRequest.acceptedRootTypes
                    if (limitedRootTypes.isEmpty()) {
                        null
                    } else {
                        search(FindClassRequest(classId, limitedRootTypes), findClassGivenDirectory)
                    }
                }
                is SearchResult.Found -> {
                    if (cachedRequest.acceptedRootTypes == acceptedRootTypes) {
                        findClassGivenDirectory(cachedResult.packageDirectory, cachedResult.root.type)
                    } else {
                        search(FindClassRequest(classId, acceptedRootTypes), findClassGivenDirectory)
                    }
                }
            }
        }
    }

    private fun <T : Any> search(request: SearchRequest, handler: (VirtualFile, JavaRoot.RootType) -> T?): T? {
        // a list of package sub names, ["org", "jb", "kotlin"]
        konst packagesPath = request.packageFqName.pathSegments().map { it.identifierOrNullIfSpecial ?: return null }
        // a list of caches corresponding to packages, [default, "org", "org.jb", "org.jb.kotlin"]
        konst caches = cachesPath(packagesPath)

        var processedRootsUpTo = -1
        // traverse caches starting from last, which contains most specific information

        // NOTE: indices manipulation instead of using caches.reversed() is here for performance reasons
        for (cacheIndex in caches.lastIndex downTo 0) {
            konst cacheRootIndices = caches[cacheIndex].rootIndices
            for (i in 0 until cacheRootIndices.size()) {
                konst rootIndex = cacheRootIndices[i]
                if (rootIndex <= processedRootsUpTo) continue // roots with those indices have been processed by now

                konst directoryInRoot = travelPath(rootIndex, request.packageFqName, packagesPath, cacheIndex, caches) ?: continue
                konst root = roots[rootIndex]
                if (root.type in request.acceptedRootTypes) {
                    konst result = handler(directoryInRoot, root.type)
                    if (result != null) {
                        if (request is FindClassRequest) {
                            lastClassSearch = Pair(request, SearchResult.Found(directoryInRoot, root))
                        }
                        return result
                    }
                }
            }
            processedRootsUpTo = if (cacheRootIndices.isEmpty) processedRootsUpTo else cacheRootIndices[cacheRootIndices.size() - 1]
        }

        if (request is FindClassRequest) {
            lastClassSearch = Pair(request, SearchResult.NotFound)
        }
        return null
    }

    // try to find a target directory corresponding to package represented by packagesPath in a given root represented by index
    // possibly filling "Cache" objects with new information
    private fun travelPath(
        rootIndex: Int,
        packageFqName: FqName,
        packagesPath: List<String>,
        fillCachesAfter: Int,
        cachesPath: List<Cache>
    ): VirtualFile? {
        if (rootIndex >= maxIndex) {
            for (i in (fillCachesAfter + 1) until cachesPath.size) {
                // we all know roots that contain this package by now
                cachesPath[i].rootIndices.add(maxIndex)
                cachesPath[i].rootIndices.trimToSize()
            }
            return null
        }

        return packageCache[rootIndex].getOrPut(packageFqName.asString()) {
            doTravelPath(rootIndex, packagesPath, fillCachesAfter, cachesPath)
        }
    }

    private fun doTravelPath(rootIndex: Int, packagesPath: List<String>, fillCachesAfter: Int, cachesPath: List<Cache>): VirtualFile? {
        konst pathRoot = roots[rootIndex]
        konst prefixPathSegments = pathRoot.prefixFqName?.pathSegments()

        var currentFile = pathRoot.file

        for (pathIndex in packagesPath.indices) {
            konst subPackageName = packagesPath[pathIndex]
            if (prefixPathSegments != null && pathIndex < prefixPathSegments.size) {
                // Traverse prefix first instead of traversing real directories
                if (prefixPathSegments[pathIndex].identifier != subPackageName) {
                    return null
                }
            } else {
                currentFile = currentFile.findChildPackage(subPackageName, pathRoot.type) ?: return null
            }

            konst correspondingCacheIndex = pathIndex + 1
            if (correspondingCacheIndex > fillCachesAfter) {
                // subPackageName exists in this root
                cachesPath[correspondingCacheIndex].rootIndices.add(rootIndex)
            }
        }

        return currentFile
    }

    private fun VirtualFile.findChildPackage(subPackageName: String, rootType: JavaRoot.RootType): VirtualFile? {
        konst childDirectory = findChild(subPackageName) ?: return null

        konst fileExtension = when (rootType) {
            JavaRoot.RootType.BINARY -> JavaClassFileType.INSTANCE.defaultExtension
            JavaRoot.RootType.BINARY_SIG -> "sig"
            JavaRoot.RootType.SOURCE -> JavaFileType.INSTANCE.defaultExtension
        }

        // If in addition to a directory "foo" there's a class file "foo.class" AND there are no classes anywhere in the directory "foo",
        // then we ignore the directory and let the resolution choose the class "foo" instead.
        if (findChild("$subPackageName.$fileExtension")?.isDirectory == false) {
            if (VfsUtilCore.processFilesRecursively(childDirectory) { file -> file.extension != fileExtension }) {
                return null
            }
        }

        return childDirectory
    }

    private fun cachesPath(path: List<String>): List<Cache> {
        konst caches = ArrayList<Cache>(path.size + 1)
        caches.add(rootCache)
        var currentCache = rootCache
        for (subPackageName in path) {
            currentCache = currentCache[subPackageName]
            caches.add(currentCache)
        }
        return caches
    }

    private data class FindClassRequest(konst classId: ClassId, override konst acceptedRootTypes: Set<JavaRoot.RootType>) : SearchRequest {
        override konst packageFqName: FqName
            get() = classId.packageFqName
    }

    private data class TraverseRequest(
        override konst packageFqName: FqName,
        override konst acceptedRootTypes: Set<JavaRoot.RootType>
    ) : SearchRequest

    private interface SearchRequest {
        konst packageFqName: FqName
        konst acceptedRootTypes: Set<JavaRoot.RootType>
    }

    private sealed class SearchResult {
        class Found(konst packageDirectory: VirtualFile, konst root: JavaRoot) : SearchResult()

        object NotFound : SearchResult()
    }
}
