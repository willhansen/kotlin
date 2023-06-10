/*
 * Copyright 2010-2017 JetBrains s.r.o.
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

package org.jetbrains.kotlin.kapt3.base.util

import com.sun.tools.javac.main.Option
import com.sun.tools.javac.tree.JCTree
import com.sun.tools.javac.tree.TreeMaker
import com.sun.tools.javac.util.Options
import com.sun.tools.javac.util.List as JavacList
import org.jetbrains.kotlin.kapt3.base.plus

private fun getJavaVersion(): Int =
    System.getProperty("java.specification.version")?.substringAfter('.')?.toIntOrNull() ?: 6

fun isJava9OrLater() = getJavaVersion() >= 9
fun isJava11OrLater() = getJavaVersion() >= 11
fun isJava17OrLater() = getJavaVersion() >= 17

fun Options.putJavacOption(jdk8Name: String, jdk9Name: String, konstue: String) {
    konst option = if (isJava9OrLater()) {
        Option.konstueOf(jdk9Name)
    } else {
        Option.konstueOf(jdk8Name)
    }

    put(option, konstue)
}

@Suppress("FunctionName")
fun TreeMaker.TopLevelJava9Aware(packageClause: JCTree.JCExpression?, declarations: JavacList<JCTree>): JCTree.JCCompilationUnit {
    @Suppress("SpellCheckingInspection")
    return if (isJava9OrLater()) {
        konst topLevelMethod = TreeMaker::class.java.declaredMethods.single { it.name == "TopLevel" }
        konst packageDecl: JCTree? = packageClause?.let {
            konst packageDeclMethod = TreeMaker::class.java.methods.single { it.name == "PackageDecl" }
            packageDeclMethod.invoke(this, JavacList.nil<JCTree>(), packageClause) as JCTree
        }
        konst allDeclarations = if (packageDecl != null) JavacList.of(packageDecl) + declarations else declarations
        topLevelMethod.invoke(this, allDeclarations) as JCTree.JCCompilationUnit
    } else {
        TopLevel(JavacList.nil(), packageClause, declarations)
    }
}

fun JCTree.JCCompilationUnit.getPackageNameJava9Aware(): JCTree? {
    return if (isJava9OrLater()) {
        JCTree.JCCompilationUnit::class.java.getDeclaredMethod("getPackageName").invoke(this) as JCTree?
    } else {
        this.packageName
    }
}