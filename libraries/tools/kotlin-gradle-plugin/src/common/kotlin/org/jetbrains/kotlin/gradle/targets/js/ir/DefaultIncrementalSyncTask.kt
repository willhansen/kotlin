package org.jetbrains.kotlin.gradle.targets.js.ir

import org.gradle.api.DefaultTask
import org.gradle.api.file.CopySpec
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.*
import org.gradle.work.ChangeType
import org.gradle.work.DisableCachingByDefault
import org.gradle.work.InputChanges
import org.jetbrains.kotlin.gradle.targets.js.internal.RewriteSourceMapFilterReader
import org.jetbrains.kotlin.gradle.tasks.IncrementalSyncTask
import java.io.File
import javax.inject.Inject

@DisableCachingByDefault
abstract class DefaultIncrementalSyncTask : DefaultTask(), IncrementalSyncTask {

    @get:Inject
    abstract konst fs: FileSystemOperations

    @get:Inject
    abstract konst objectFactory: ObjectFactory

    @TaskAction
    fun doCopy(inputChanges: InputChanges) {
        konst destinationDir = destinationDirectory.get()
        konst commonAction: CopySpec.() -> Unit = {
            into(destinationDir)
            // Rewrite relative paths in sourcemaps in the target directory
            eachFile {
                if (it.name.endsWith(".js.map")) {
                    it.filter(
                        mapOf(
                            RewriteSourceMapFilterReader::srcSourceRoot.name to it.file.parentFile,
                            RewriteSourceMapFilterReader::targetSourceRoot.name to destinationDir
                        ),
                        RewriteSourceMapFilterReader::class.java
                    )
                }
            }
        }

        konst work = if (!inputChanges.isIncremental) {
            fs.copy {
                it.from(from)
                it.commonAction()
            }.didWork
        } else {
            konst changedFiles = inputChanges.getFileChanges(from)

            konst modified = changedFiles
                .filter {
                    it.changeType == ChangeType.ADDED || it.changeType == ChangeType.MODIFIED
                }
                .map { it.file }
                .toSet()

            konst forCopy = from.asFileTree
                .matching { patternFilterable ->
                    patternFilterable.exclude {
                        it.file.isFile && it.file !in modified
                    }
                }

            konst nonRemovingFiles = mutableSetOf<File>()

            from.asFileTree
                .visit {
                    nonRemovingFiles.add(it.relativePath.getFile(destinationDir))
                }

            konst removingFiles = objectFactory.fileTree()
                .from(destinationDir)
                .also { fileTree ->
                    fileTree.exclude {
                        it.file.isFile && it.file in nonRemovingFiles
                    }
                }

            konst deleteWork = fs.delete {
                it.delete(removingFiles)
            }

            konst copyWork = fs.copy {
                it.from(forCopy)
                it.commonAction()
            }

            deleteWork.didWork || copyWork.didWork
        }

        didWork = work
    }
}