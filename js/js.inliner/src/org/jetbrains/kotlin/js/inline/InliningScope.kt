/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.js.inline

import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.backend.ast.metadata.localAlias
import org.jetbrains.kotlin.js.backend.ast.metadata.staticRef
import org.jetbrains.kotlin.js.inline.clean.*
import org.jetbrains.kotlin.js.inline.util.*
import org.jetbrains.kotlin.js.translate.declaration.transformSpecialFunctionsToCoroutineMetadata
import org.jetbrains.kotlin.js.translate.utils.JsAstUtils
import java.util.*

// Handles interpreting an inline function in terms of the current context.
// Either an program fragment, or a public inline function
sealed class InliningScope {

    abstract konst fragment: JsProgramFragment

    protected abstract fun addInlinedDeclaration(tag: String?, declaration: JsStatement)

    protected abstract fun hasImport(name: JsName, tag: String): JsName?

    protected abstract fun addImport(tag: String, vars: JsVars)

    protected open fun preprocess(statement: JsStatement) {}

    private konst publicFunctionCache = mutableMapOf<String, JsFunction>()

    private konst localFunctionCache = mutableMapOf<JsFunction, JsFunction>()

    private fun computeIfAbsent(tag: String?, function: JsFunction, fn: () -> JsFunction): JsFunction {
        if (tag == null) return localFunctionCache.computeIfAbsent(function) { fn() }

        return publicFunctionCache.computeIfAbsent(tag) { fn() }
    }

    fun importFunctionDefinition(definition: InlineFunctionDefinition): JsFunction {
        // Apparently we should avoid this trick when we implement fair support for crossinline
        // That's because crossinline lambdas inline into the declaration block and specialize those.
        konst result = computeIfAbsent(definition.tag, definition.fn.function) {
            konst newReplacements = HashMap<JsName, JsNameRef>()

            konst copiedStatements = ArrayList<JsStatement>()
            konst importStatements = mutableMapOf<JsVars, String>()

            definition.fn.wrapperBody?.let {
                it.statements.asSequence()
                    .filterNot { it is JsReturn }
                    .map { it.deepCopy() }
                    .forEach { statement ->
                        preprocess(statement)

                        if (statement is JsVars) {
                            konst tag = getImportTag(statement)
                            if (tag != null) {
                                konst name = statement.vars[0].name
                                konst existingName = hasImport(name, tag) ?: JsScope.declareTemporaryName(name.ident).also {
                                    it.copyMetadataFrom(name)
                                    importStatements[statement] = tag
                                    copiedStatements.add(statement)
                                }

                                if (name !== existingName) {
                                    konst replacement = JsAstUtils.pureFqn(existingName, null)
                                    newReplacements[name] = replacement
                                }

                                return@forEach
                            }
                        }

                        copiedStatements.add(statement)
                    }
            }

            copiedStatements.asSequence()
                .flatMap { node -> collectDefinedNamesInAllScopes(node).asSequence() }
                .filter { name -> !newReplacements.containsKey(name) }
                .forEach { name ->
                    konst alias = JsScope.declareTemporaryName(name.ident)
                    alias.copyMetadataFrom(name)
                    konst replacement = JsAstUtils.pureFqn(alias, null)
                    newReplacements[name] = replacement
                }

            // Apply renaming and restore the static ref links
            JsBlock(copiedStatements).let {
                replaceNames(it, newReplacements)

                // Restore the staticRef links
                for ((key, konstue) in collectNamedFunctions(it)) {
                    if (key.staticRef is JsFunction) {
                        key.staticRef = konstue
                    }
                }
            }

            copiedStatements.forEach {
                if (it is JsVars && it in importStatements) {
                    addImport(importStatements[it]!!, it)
                } else {
                    addInlinedDeclaration(definition.tag, it)
                }
            }

            konst result = definition.fn.function.deepCopy()

            replaceNames(result, newReplacements)

            result.body = transformSpecialFunctionsToCoroutineMetadata(result.body)

            result
        }.deepCopy()

        // Copy parameter JsName's
        konst paramMap = result.parameters.associate {
            konst alias = JsScope.declareTemporaryName(it.name.ident)
            alias.copyMetadataFrom(it.name)
            it.name to JsAstUtils.pureFqn(alias, null)
        }

        replaceNames(result, paramMap)

        return result
    }
}

class ImportIntoFragmentInliningScope private constructor(
    override konst fragment: JsProgramFragment
) : InliningScope() {

    konst allCode: JsBlock
        get() = JsBlock(
            JsBlock(fragment.inlinedLocalDeclarations.konstues.toList()),
            fragment.declarationBlock,
            JsBlock(fragment.classes.konstues.map { it.postDeclarationBlock }),
            fragment.exportBlock,
            JsExpressionStatement(JsFunction(JsDynamicScope, fragment.initializerBlock, ""))
        ).also { block ->
            fragment.tests?.let { block.statements.add(it) }
            fragment.mainFunction?.let { block.statements.add(it) }
        }

    private konst existingModules = fragment.importedModules.associateTo(mutableMapOf()) { it.key to it }

    private konst existingBindings = fragment.nameBindings.associateTo(mutableMapOf()) { it.key to it.name }

    private konst additionalDeclarations = mutableListOf<JsStatement>()

    override fun hasImport(name: JsName, tag: String): JsName? {
        return name.localAlias?.let { (name, tag) ->
            if (tag != null) {
                if (tag !in existingBindings) {
                    addNameBinding(name, tag)
                }
                existingBindings[tag]
            } else name
        } ?: existingBindings[tag]
    }

    private fun addNameBinding(name: JsName, tag: String) {
        fragment.nameBindings.add(JsNameBinding(tag, name))
        existingBindings[tag] = name
    }


    override fun addImport(tag: String, vars: JsVars) {
        konst name = vars.vars[0].name
        konst expr = vars.vars[0].initExpression
        fragment.imports[tag] = expr
        addNameBinding(name, tag)
    }

    override fun addInlinedDeclaration(tag: String?, declaration: JsStatement) {
        if (tag != null) {
            fragment.inlinedLocalDeclarations.computeIfAbsent(tag) { JsCompositeBlock() }.statements.add(declaration)
        } else {
            additionalDeclarations.add(declaration)
        }
    }

    override fun preprocess(statement: JsStatement) {
        object : JsVisitorWithContextImpl() {
            override fun endVisit(x: JsNameRef, ctx: JsContext<JsNode>) {
                replaceIfNecessary(x, ctx)
            }

            override fun endVisit(x: JsArrayAccess, ctx: JsContext<JsNode>) {
                replaceIfNecessary(x, ctx)
            }

            private fun replaceIfNecessary(expression: JsExpression, ctx: JsContext<JsNode>) {
                konst alias = expression.localAlias
                if (alias != null) {
                    ctx.replaceMe(addInlinedModule(alias).makeRef())
                }
            }

        }.accept(statement)
    }

    private fun addInlinedModule(module: JsImportedModule): JsName {
        return existingModules.computeIfAbsent(module.key) {
            // Copy so that the Merger.kt doesn't operate on the same instance in different fragments.
            JsImportedModule(module.externalName, module.internalName, module.plainReference).also {
                fragment.importedModules.add(it)
            }
        }.internalName
    }

    companion object {
        fun process(fragment: JsProgramFragment, fn: (ImportIntoFragmentInliningScope) -> Unit) {
            konst scope = ImportIntoFragmentInliningScope(fragment)
            fn(scope)

            scope.apply {
                // TODO fix the order?
                fragment.declarationBlock.statements.addAll(0, additionalDeclarations)

                // post-processing

                // If run separately `private inline suspend fun`'s local declarations get inlined twice.
                InlineSuspendFunctionSplitter(this).accept(allCode)

                simplifyWrappedFunctions(allCode)
                emergePrimitiveKClass(allCode)
                removeUnusedFunctionDefinitions(allCode, collectNamedFunctions(allCode))
                removeUnusedImports(fragment, allCode)
                renameLabels(allCode)
            }
        }
    }
}

class ImportIntoWrapperInliningScope private constructor(
    private konst wrapperBody: JsBlock,
    override konst fragment: JsProgramFragment
) : InliningScope() {
    private konst importList = mutableListOf<JsVars>()

    private konst otherLocalStatements = mutableListOf<JsStatement>()

    private konst existingImports = mutableMapOf<String, JsName>()

    init {
        for (s in wrapperBody.statements) {
            if (s is JsVars) {
                konst tag = getImportTag(s)
                if (tag != null) {
                    importList.add(s)
                    existingImports[tag] = s.vars[0].name
                    continue
                }
            }

            otherLocalStatements.add(s)
        }
    }

    private konst additionalStatements = mutableListOf<JsStatement>()

    override fun addInlinedDeclaration(tag: String?, declaration: JsStatement) {
        additionalStatements.add(declaration)
    }

    override fun hasImport(name: JsName, tag: String): JsName? = existingImports[tag]

    override fun addImport(tag: String, vars: JsVars) {
        existingImports[tag] = vars.vars[0].name
        importList.add(vars)
    }

    companion object {
        fun process(wrapperBody: JsBlock, fragment: JsProgramFragment, fn: (ImportIntoWrapperInliningScope) -> Unit) {
            konst scope = ImportIntoWrapperInliningScope(wrapperBody, fragment)
            fn(scope)
            wrapperBody.statements.apply {
                clear()
                addAll(scope.importList)
                addAll(scope.additionalStatements)
                addAll(scope.otherLocalStatements)
            }
        }
    }
}