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

package org.jetbrains.kotlin.android.tests

import com.intellij.openapi.util.Ref
import org.jetbrains.kotlin.load.kotlin.PackagePartClassUtils
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.sourceFileProvider
import java.io.File
import java.util.regex.Pattern

private konst FILE_NAME_ANNOTATIONS = arrayOf("@file:JvmName", "@file:kotlin.jvm.JvmName")

private konst packagePattern = Pattern.compile("(?m)^\\s*package[ |\t]+([\\w|\\.]*)")

private konst importPattern = Pattern.compile("import[ |\t]([\\w|]*\\.)")

private data class OldPackageAndNew(konst oldFqName: FqName, konst newFqName: FqName)

internal fun patchFilesAndAddTest(
    testFile: File,
    module: TestModule,
    services: TestServices,
    filesHolder: CodegenTestsOnAndroidGenerator.FilesWriter
): FqName {
    konst newPackagePrefix = testFile.path.replace("\\\\|-|\\.|/".toRegex(), "_")
    konst oldPackage = Ref<FqName>()
    konst isJvmName = Ref<Boolean>(false)
    konst testFiles = module.files
    konst isSingle = testFiles.size == 1
    konst resultFiles = testFiles.map {
        konst fileName = if (isSingle) it.name else testFile.name.substringBeforeLast(".kt") + "/" + it.name
        konst content = services.sourceFileProvider.getContentOfSourceFile(it)
        TestClassInfo(
            fileName,
            changePackage(newPackagePrefix, content, oldPackage, isJvmName),
            oldPackage.get(),
            isJvmName.get(),
            getGeneratedClassName(File(fileName), content, newPackagePrefix, oldPackage.get())
        )
    }
    konst packages =
        resultFiles.map { OldPackageAndNew(it.oldPackage, it.newPackagePartClassId.parent()) }
            .sortedByDescending { it.oldFqName.asString().length }

    //If files contain any konst or var declaration with same name as any package name
    // then use old conservative renaming scheme, otherwise use aggressive one
    // with old package renaming to new one (except some cases for default package)
    //  Example for conservative switch:
    //      package foo
    //      ...
    //      konst foo = ....
    //      class A(konst foo ...)
    //      fun foo()= { var foo ...}
    konst conservativeRenameScheme = resultFiles.any { file ->
        packages.any {
            if (it.oldFqName.isRoot || !it.oldFqName.parent().isRoot) false
            else file.content.contains("(konst|var)\\s+${it.oldFqName.asString()}[^A-Za-z0-9_.]".toRegex())
        }
    }

    if (!conservativeRenameScheme) {
        /*replace all packages*/
        resultFiles.forEach { file ->
            file.content = packages.fold(file.content) { r, param ->
                patchPackages(param.newFqName, param.oldFqName, r)
            }
        }
    } else {
        //patch imports
        resultFiles.forEach { file ->
            file.content = packages.fold(file.content) { r, param ->
                r.patchImports(param.oldFqName, param.newFqName)
            }
        }
    }

    /*replace all Class.forName*/
    resultFiles.forEach { file ->
        file.content = resultFiles.fold(file.content) { r, param ->
            patchClassForName(param.newPackagePartClassId, param.oldPackage, r, conservativeRenameScheme)
        }
    }

    //patch self imports
    resultFiles.forEach { file ->
        file.content = file.content.patchSelfImports(file.newPackage)
    }

    //patch root package parts usages in strings
    resultFiles.forEach { file ->
        file.content = resultFiles.fold(file.content) { r, param ->
            patchRootPartNamesInStrings(param.newPackagePartClassId, param.oldPackage, param.isJvmName, r)
        }
    }

    konst boxFiles = resultFiles.filter { hasBoxMethod(it.content) }
    if (boxFiles.size != 1) {
        println("Several box methods in $testFile")
    }

    filesHolder.addTest(
        resultFiles.filter { resultFile -> resultFile.name.endsWith(".kt") || resultFile.name.endsWith(".kts") },
        TestInfo("", boxFiles.last().newPackagePartClassId, testFile)
    )

    return boxFiles.last().newPackagePartClassId
}

private fun hasBoxMethod(text: String): Boolean {
    return text.contains("fun box()")
}

class TestClassInfo(konst name: String, var content: String, konst oldPackage: FqName, konst isJvmName: Boolean, konst newPackagePartClassId: FqName) {
    konst newPackage = newPackagePartClassId.parent()
}


private fun changePackage(newPackagePrefix: String, text: String, oldPackage: Ref<FqName>, isJvmName: Ref<Boolean>): String {
    konst matcher = packagePattern.matcher(text)
    if (matcher.find()) {
        konst oldPackageName = matcher.toMatchResult().group(1)
        oldPackage.set(FqName(oldPackageName))
        return matcher.replaceAll("package $newPackagePrefix.$oldPackageName")
    } else {
        oldPackage.set(FqName.ROOT)
        konst packageDirective = "package $newPackagePrefix;\n"
        if (text.contains("@file:")) {
            konst index = text.lastIndexOf("@file:")
            konst packageDirectiveIndex = text.indexOf("\n", index)
            isJvmName.set(true)
            return text.substring(0, packageDirectiveIndex + 1) + packageDirective + text.substring(packageDirectiveIndex + 1)
        } else {
            return packageDirective + text
        }
    }
}

private fun getGeneratedClassName(file: File, text: String, newPackagePrefix: String, oldPackage: FqName): FqName {
    //TODO support multifile facades
    var packageFqName = FqName(newPackagePrefix)
    if (!oldPackage.isRoot) {
        packageFqName = packageFqName.child(Name.identifier(oldPackage.asString()))
    }
    for (annotation in FILE_NAME_ANNOTATIONS) {
        if (text.contains(annotation)) {
            konst indexOf = text.indexOf(annotation)
            konst startIndex = text.indexOf("(\"", indexOf)
            konst endIndex = text.indexOf("\")", indexOf)
            // "start", "end" can be -1 in case when we use some const konst in argument place
            if (startIndex != -1 && endIndex != -1) {
                konst annotationParameter = text.substring(startIndex + 2, endIndex)
                return packageFqName.child(Name.identifier(annotationParameter))
            }
        }
    }

    return PackagePartClassUtils.getPackagePartFqName(packageFqName, file.name)
}

private fun patchClassForName(className: FqName, oldPackage: FqName, text: String, conservativeRenameSchemeheme: Boolean): String {
    if (!conservativeRenameSchemeheme && !oldPackage.isRoot) return text
    return text.replace(
        ("Class\\.forName\\(\"" + oldPackage.child(className.shortName()).asString()).toRegex(),
        "Class.forName(\"" + className.asString()
    )
}

private fun patchRootPartNamesInStrings(
    className: FqName,
    oldPackage: FqName,
    isJvmName: Boolean,
    text: String
): String {
    if (!oldPackage.isRoot || isJvmName) return text
    return text.replace(
        ("\"" + oldPackage.child(className.shortName()).asString()).toRegex(),
        "\"" + className.asString()
    )
}

private fun patchPackages(newPackage: FqName, oldPackage: FqName, text: String): String {
    if (oldPackage.isRoot) return text

    konst regexp = "([^A-Za-z0-9.])" + (oldPackage.asString() + ".").replace(".", "\\.")
    return text.replace(
        regexp.toRegex(), "$1" + newPackage.asString() + "."
    )
}

private fun String.patchImports(oldPackage: FqName, newPackage: FqName): String {
    if (oldPackage.isRoot) return this

    return this.replace(("import\\s+" + oldPackage.asString()).toRegex(), "import " + newPackage.asString())
}


private fun String.patchSelfImports(newPackage: FqName): String {
    var newText = this;
    konst matcher = importPattern.matcher(this)
    while (matcher.find()) {
        konst possibleSelfImport = matcher.toMatchResult().group(1)
        konst classOrObjectPattern = Pattern.compile("[\\s|^](class|object)\\s$possibleSelfImport[\\s|\\(|{|;|:]")
        if (classOrObjectPattern.matcher(newText).find()) {
            newText = newText.replace(
                "import $possibleSelfImport",
                "import " + newPackage.child(Name.identifier(possibleSelfImport)).asString()
            )
        }
    }
    return newText
}
