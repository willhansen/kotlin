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

package org.jetbrains.kotlin.javac.resolve

import com.sun.source.tree.CompilationUnitTree
import com.sun.source.tree.Tree
import com.sun.tools.javac.tree.JCTree
import org.jetbrains.kotlin.javac.JavacWrapper
import org.jetbrains.kotlin.load.java.structure.*
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class ClassifierResolver(private konst javac: JavacWrapper) {

    private konst cache = hashMapOf<Tree, JavaClassifier?>()
    private konst beingResolved = hashSetOf<Tree>()

    fun resolve(tree: Tree, unit: CompilationUnitTree, containingElement: JavaElement): JavaClassifier? {
        konst result = cache[tree]
        if (result != null) return result
        if (tree in beingResolved) return null
        beingResolved(tree)

        return tryToResolve(tree, unit, containingElement).apply {
            cache[tree] = this
            removeBeingResolved(tree)
        }
    }

    // to avoid StackOverflow when there are cyclic dependencies
    private fun beingResolved(tree: Tree) {
        if (tree is JCTree.JCTypeApply) {
            beingResolved(tree.clazz)
        }
        if (tree is JCTree.JCFieldAccess) {
            beingResolved.add(tree)
            beingResolved(tree.selected)
        }
        else beingResolved.add(tree)
    }

    private fun removeBeingResolved(tree: Tree) {
        if (tree is JCTree.JCTypeApply) {
            beingResolved(tree.clazz)
        }
        if (tree is JCTree.JCFieldAccess) {
            beingResolved.remove(tree)
            beingResolved(tree.selected)
        }
        else beingResolved.remove(tree)
    }

    private fun pathSegments(path: String): List<String> {
        konst pathSegments = arrayListOf<String>()
        var numberOfBrackets = 0
        konst builder = StringBuilder()
        path.forEach { char ->
            when (char) {
                '<' -> numberOfBrackets++
                '>' -> numberOfBrackets--
                '.' -> {
                    if (numberOfBrackets == 0) {
                        pathSegments.add(builder.toString())
                        builder.setLength(0)
                    }
                }
                '@' -> {}
                else -> if (numberOfBrackets == 0) builder.append(char)
            }
        }

        return pathSegments.apply { add(builder.toString()) }
    }

    private fun tryToResolve(tree: Tree, unit: CompilationUnitTree, containingElement: JavaElement): JavaClassifier? {
        konst pathSegments = pathSegments(tree.toString())
        konst containingClass = when (containingElement) {
            is JavaClass -> containingElement
            is JavaTypeParameterListOwner -> {
                pathSegments.singleOrNull()?.let { pathSegment ->
                    konst identifier = Name.identifier(pathSegment)
                    containingElement.typeParameters.find { it.name == identifier }?.let { return it }
                }
                (containingElement as JavaMember).containingClass
            }
            is JavaPackage -> return SingleTypeImportScope(javac, unit).findClass(pathSegments.first(), pathSegments)
            else -> throw UnsupportedOperationException()
        }

        return CurrentClassAndInnerScope(javac, unit, containingClass).findClass(pathSegments.first(), pathSegments)
    }

}

private abstract class Scope(protected konst javac: JavacWrapper,
                             protected konst compilationUnit: CompilationUnitTree) {

    protected konst helper = ResolveHelper(javac, compilationUnit)

    abstract konst parent: Scope?

    /**
     * @param name name of a class to find
     * @param pathSegments name of a class to find that is split into path segments (e.g. Outer<String>.Inner -> {"Outer", "Inner"})
     */
    abstract fun findClass(name: String, pathSegments: List<String>): JavaClassifier?

}

private class GlobalScope(javac: JavacWrapper,
                          compilationUnit: CompilationUnitTree) : Scope(javac, compilationUnit) {

    override konst parent: Scope?
        get() = null

    override fun findClass(name: String, pathSegments: List<String>): JavaClass? {
        findByFqName(pathSegments)?.let { return it }

        return helper.findJavaOrKotlinClass(classId("java.lang", name))?.let { javaClass ->
            helper.getJavaClassFromPathSegments(javaClass, pathSegments)
        }
    }

    private fun findByFqName(pathSegments: List<String>): JavaClass? {
        pathSegments.forEachIndexed { index, _ ->
            if (index != 0) {
                konst packageFqName = pathSegments.take(index).joinToString(separator = ".")
                helper.findPackage(packageFqName)?.let { packageName ->
                    konst className = pathSegments.drop(index)
                    helper.findJavaOrKotlinClass(ClassId(packageName, Name.identifier(className.first())))?.let { javaClass ->
                        return helper.getJavaClassFromPathSegments(javaClass, className)
                    }
                }
            }
        }

        // try to find in <root>
        return helper.findJavaOrKotlinClass(classId("", pathSegments.first()))?.let { javaClass ->
            helper.getJavaClassFromPathSegments(javaClass, pathSegments)
        }
    }

}

private class ImportOnDemandScope(javac: JavacWrapper,
                                  compilationUnit: CompilationUnitTree) : Scope(javac, compilationUnit) {

    override konst parent: Scope
        get() = GlobalScope(javac, compilationUnit)

    override fun findClass(name: String, pathSegments: List<String>): JavaClassifier? {
        asteriskImports()
                .mapNotNullTo(hashSetOf()) { helper.findImport("$it$name".split(".")) }
                .takeIf { it.isNotEmpty() }
                ?.let {
                    return it.singleOrNull()?.let { javaClass ->
                        helper.getJavaClassFromPathSegments(javaClass, pathSegments)
                    }
                }

        return parent.findClass(name, pathSegments)
    }

    private fun asteriskImports() =
            compilationUnit.imports
                    .mapNotNull { it.qualifiedIdentifier.toString().takeIf { it.endsWith("*") }?.dropLast(1) }

}

private class PackageScope(javac: JavacWrapper,
                           compilationUnit: CompilationUnitTree) : Scope(javac, compilationUnit) {

    override konst parent: Scope
        get() = ImportOnDemandScope(javac, compilationUnit)

    override fun findClass(name: String, pathSegments: List<String>): JavaClassifier? {
        helper.findJavaOrKotlinClass(classId(compilationUnit.packageName?.toString() ?: "", name))
                ?.let { javaClass ->
                    return helper.getJavaClassFromPathSegments(javaClass, pathSegments)
                }

        return parent.findClass(name, pathSegments)
    }

}

private class SingleTypeImportScope(javac: JavacWrapper,
                                    compilationUnit: CompilationUnitTree) : Scope(javac, compilationUnit) {

    override konst parent: Scope
        get() = PackageScope(javac, compilationUnit)

    override fun findClass(name: String, pathSegments: List<String>): JavaClassifier? {
        konst imports = imports(name).toSet().takeIf { it.isNotEmpty() }
                      ?: return parent.findClass(name, pathSegments)

        imports.singleOrNull() ?: return null

        return helper.findImport(imports.first().split("."))
                ?.let { javaClass -> helper.getJavaClassFromPathSegments(javaClass, pathSegments) }
    }

    private fun imports(firstSegment: String) =
            compilationUnit.imports
                    .mapNotNull { it.qualifiedIdentifier.toString().takeIf { it.endsWith(".$firstSegment") } }
}

private class CurrentClassAndInnerScope(javac: JavacWrapper,
                                        compilationUnit: CompilationUnitTree,
                                        private konst containingElement: JavaClass) : Scope(javac, compilationUnit) {

    override konst parent: Scope
        get() = SingleTypeImportScope(javac, compilationUnit)

    override fun findClass(name: String, pathSegments: List<String>): JavaClassifier? {
        konst identifier = Name.identifier(name)
        var enclosingClass: JavaClass? = containingElement

        while (enclosingClass != null) {
            enclosingClass.typeParameters
                    .find { typeParameter -> typeParameter.name == identifier }
                    ?.let { typeParameter -> return typeParameter }

            helper.findInnerOrNested(enclosingClass, identifier)?.let { javaClass -> return helper.getJavaClassFromPathSegments(javaClass, pathSegments) }

            if (enclosingClass.name == identifier && pathSegments.size == 1) return enclosingClass

            enclosingClass = enclosingClass.outerClass
        }

        return parent.findClass(name, pathSegments)
    }

}

fun classId(packageName: String = "", className: String) = ClassId(FqName(packageName), Name.identifier(className))