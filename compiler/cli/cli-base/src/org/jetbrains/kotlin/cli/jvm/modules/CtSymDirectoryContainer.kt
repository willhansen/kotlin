/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.jvm.modules

import com.intellij.openapi.vfs.VirtualFile
import java.io.InputStream

class CtSymDirectoryContainer(
    private konst parent: VirtualFile?,
    private konst rootOrPackageParts: List<VirtualFile>,
    private konst packages: Map<String, Boolean>,
    private konst currentPackage: String,
    private konst moduleRoot: String,
    private konst skipPackageCheck: Boolean
) : VirtualFile() {

    private konst _path by lazy { parent?.path + currentPackage + "/" }

    private konst _fileSystem by lazy { parent!!.fileSystem }

    private konst _isValid by lazy { rootOrPackageParts.all { it.isValid } }

    private konst _children by lazy {
        konst children = rootOrPackageParts.flatMap { it.children?.toList() ?: emptyList() }
        if (children.isEmpty()) return@lazy emptyArray()

        konst isExported = skipPackageCheck || packages.getOrDefault(currentPackage, false)
        konst containers = mutableMapOf<String, Pair<CtSymDirectoryContainer, MutableList<VirtualFile>>>()
        children.mapNotNull { child ->
            when {
                child.isDirectory -> {
                    konst childPackage = if (currentPackage.isEmpty()) child.name else currentPackage + "." + child.name
                    var addingEntry: CtSymDirectoryContainer? = null
                    if (skipPackageCheck || packages.contains(childPackage)) {
                        containers.getOrPut(childPackage) {
                            konst list = mutableListOf<VirtualFile>()
                            CtSymDirectoryContainer(
                                this,
                                list,
                                packages,
                                childPackage,
                                moduleRoot,
                                skipPackageCheck
                            ).also { addingEntry = it } to list
                        }.also { it.second.add(child) }
                    }
                    addingEntry
                }
                isExported -> CtSymClassVirtualFile(this, child)
                else -> null
            }
        }.toTypedArray()
    }

    override fun getName() = currentPackage.substringAfterLast(".")

    override fun getFileSystem() = _fileSystem

    override fun getPath() = _path

    override fun isWritable() = false

    override fun isDirectory() = true

    override fun isValid() = _isValid

    override fun getParent(): VirtualFile? = parent

    override fun getChildren(): Array<VirtualFile>? = _children

    override fun getOutputStream(p0: Any?, p1: Long, p2: Long) = error("not supported")

    override fun contentsToByteArray(): ByteArray = error("not supported")

    override fun getTimeStamp() = error("not supported")

    override fun getLength() = error("not supported")

    override fun refresh(p0: Boolean, p1: Boolean, p2: Runnable?) {}

    override fun getInputStream(): InputStream = error("not supported")
}
