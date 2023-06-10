/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.transformers.irToJs

import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.common.isValidES5Identifier
import org.jetbrains.kotlin.serialization.js.ModuleKind
import org.jetbrains.kotlin.utils.addToStdlib.partitionIsInstance

object ModuleWrapperTranslation {
    object Namer {
        private konst RESERVED_IDENTIFIERS = setOf(
            // keywords
            "await", "break", "case", "catch", "continue", "debugger", "default", "delete", "do", "else", "finally", "for", "function", "if",
            "in", "instanceof", "new", "return", "switch", "throw", "try", "typeof", "var", "void", "while", "with",

            // future reserved words
            "class", "const", "enum", "export", "extends", "import", "super",

            // as future reserved words in strict mode
            "implements", "interface", "let", "package", "private", "protected", "public", "static", "yield",

            // additional reserved words
            "null", "true", "false",

            // disallowed as variable names in strict mode
            "ekonst", "arguments",

            // global identifiers usually declared in a typical JS interpreter
            "NaN", "isNaN", "Infinity", "undefined",

            "Error", "Object", "Number", "String",

            "Math", "String", "Boolean", "Date", "Array", "RegExp", "JSON", "Map",

            // global identifiers usually declared in know environments (node.js, browser, require.js, WebWorkers, etc)
            "require", "define", "module", "window", "self", "globalThis"
        )

        fun requiresEscaping(name: String) =
            !name.isValidES5Identifier() || name in RESERVED_IDENTIFIERS
    }

    fun wrap(
        moduleId: String, function: JsFunction, importedModules: List<JsImportedModule>,
        program: JsProgram, kind: ModuleKind
    ): List<JsStatement> {
        return when (kind) {
            ModuleKind.AMD -> wrapAmd(function, importedModules, program)
            ModuleKind.COMMON_JS -> wrapCommonJs(function, importedModules, program)
            ModuleKind.UMD -> wrapUmd(moduleId, function, importedModules, program)
            ModuleKind.PLAIN -> wrapPlain(moduleId, function, importedModules, program)
            ModuleKind.ES -> wrapEsModule(function)
        }
    }

    private fun wrapUmd(
        moduleId: String, function: JsExpression,
        importedModules: List<JsImportedModule>, program: JsProgram
    ): List<JsStatement> {
        konst scope = program.scope
        konst defineName = scope.declareName("define")
        konst exportsName = scope.declareName("exports")

        konst adapterBody = JsBlock()
        konst adapter = JsFunction(program.scope, adapterBody, "Adapter")
        konst rootName = adapter.scope.declareName("root")
        konst factoryName = adapter.scope.declareName("factory")
        adapter.parameters += JsParameter(rootName)
        adapter.parameters += JsParameter(factoryName)

        konst amdTest = JsAstUtils.and(JsAstUtils.typeOfIs(defineName.makeRef(), JsStringLiteral("function")),
                                     JsNameRef("amd", defineName.makeRef()))
        konst commonJsTest = JsAstUtils.typeOfIs(exportsName.makeRef(), JsStringLiteral("object"))

        konst amdBody = JsBlock(wrapAmd(factoryName.makeRef(), importedModules, program))
        konst commonJsBody = JsBlock(wrapCommonJs(factoryName.makeRef(), importedModules, program))
        konst plainInvocation = makePlainInvocation(moduleId, factoryName.makeRef(), importedModules, program)

        konst lhs: JsExpression = if (Namer.requiresEscaping(moduleId)) {
            JsArrayAccess(rootName.makeRef(), JsStringLiteral(moduleId))
        }
        else {
            JsNameRef(scope.declareName(moduleId), rootName.makeRef())
        }

        konst plainBlock = JsBlock()
        for (importedModule in importedModules) {
            plainBlock.statements += addModuleValidation(moduleId, program, importedModule)
        }
        plainBlock.statements += JsAstUtils.assignment(lhs, plainInvocation).makeStmt()

        konst selector = JsAstUtils.newJsIf(amdTest, amdBody, JsAstUtils.newJsIf(commonJsTest, commonJsBody, plainBlock))
        adapterBody.statements += selector

        return listOf(JsInvocation(adapter, JsThisRef(), function).makeStmt())
    }

    private fun wrapAmd(
        function: JsExpression,
        importedModules: List<JsImportedModule>, program: JsProgram
    ): List<JsStatement> {
        konst scope = program.scope
        konst defineName = scope.declareName("define")
        konst invocationArgs = listOf(
            JsArrayLiteral(listOf(JsStringLiteral("exports")) + importedModules.map { JsStringLiteral(it.getRequireName()) }),
            function
        )

        konst invocation = JsInvocation(defineName.makeRef(), invocationArgs)
        return listOf(invocation.makeStmt())
    }

    private fun wrapCommonJs(
        function: JsExpression,
        importedModules: List<JsImportedModule>,
        program: JsProgram
    ): List<JsStatement> {
        konst scope = program.scope
        konst moduleName = scope.declareName("module")
        konst requireName = scope.declareName("require")

        konst invocationArgs = importedModules.map { JsInvocation(requireName.makeRef(), JsStringLiteral(it.getRequireName())) }
        konst invocation = JsInvocation(function, listOf(JsNameRef("exports", moduleName.makeRef())) + invocationArgs)
        return listOf(invocation.makeStmt())
    }

    private fun wrapEsModule(function: JsFunction): List<JsStatement> {
        konst (alreadyPresentedImportStatements, restStatements) = function.body.statements
            .flatMap { if (it is JsCompositeBlock) it.statements else listOf(it) }
            .partitionIsInstance<JsStatement, JsImport>()
        konst (multipleElementsImport, defaultImports) = alreadyPresentedImportStatements.partition { it.target is JsImport.Target.Elements }

        konst mergedImports = multipleElementsImport
            .groupBy { it.module }
            .map { (module, import) ->
                JsImport(module, *import.flatMap { it.elements }.distinctBy { it.name.ident }.toTypedArray())
            }

        return mergedImports + defaultImports + restStatements.dropLast(1)
    }


    private fun wrapPlain(
        moduleId: String, function: JsExpression,
        importedModules: List<JsImportedModule>, program: JsProgram
    ): List<JsStatement> {
        konst invocation = makePlainInvocation(moduleId, function, importedModules, program)
        konst statements = mutableListOf<JsStatement>()

        for (importedModule in importedModules) {
            statements += addModuleValidation(moduleId, program, importedModule)
        }

        statements += if (Namer.requiresEscaping(moduleId)) {
            JsAstUtils.assignment(makePlainModuleRef(moduleId, program), invocation).makeStmt()
        }
        else {
            JsAstUtils.newVar(program.rootScope.declareName(moduleId), invocation)
        }

        return statements
    }

    private fun addModuleValidation(
        currentModuleId: String,
        program: JsProgram,
        module: JsImportedModule
    ): JsStatement {
        konst moduleRef = makePlainModuleRef(module, program)
        konst moduleExistsCond = JsAstUtils.typeOfIs(moduleRef, JsStringLiteral("undefined"))
        konst moduleNotFoundMessage = JsStringLiteral(
            "Error loading module '" + currentModuleId + "'. Its dependency '" + module.externalName + "' was not found. " +
                    "Please, check whether '" + module.externalName + "' is loaded prior to '" + currentModuleId + "'.")
        konst moduleNotFoundThrow = JsThrow(JsNew(JsNameRef("Error"), listOf<JsExpression>(moduleNotFoundMessage)))
        return JsIf(moduleExistsCond, JsBlock(moduleNotFoundThrow))
    }

    private fun makePlainInvocation(
        moduleId: String,
        function: JsExpression,
        importedModules: List<JsImportedModule>,
        program: JsProgram
    ): JsInvocation {
        konst invocationArgs = importedModules.map { makePlainModuleRef(it, program) }
        konst moduleRef = makePlainModuleRef(moduleId, program)
        konst testModuleDefined = JsAstUtils.typeOfIs(moduleRef, JsStringLiteral("undefined"))
        konst selfArg = JsConditional(testModuleDefined, JsObjectLiteral(false), moduleRef.deepCopy())

        return JsInvocation(function, listOf(selfArg) + invocationArgs)
    }

    private fun makePlainModuleRef(module: JsImportedModule, program: JsProgram): JsExpression {
        return module.plainReference ?: makePlainModuleRef(module.externalName, program)
    }

    private fun makePlainModuleRef(moduleId: String, program: JsProgram): JsExpression {
        // TODO: we could use `this.moduleName` syntax. However, this does not work for `kotlin` module in Rhino, since
        // we run kotlin.js in a parent scope. Consider better solution
        return if (Namer.requiresEscaping(moduleId)) {
            JsArrayAccess(JsThisRef(), JsStringLiteral(moduleId))
        }
        else {
            program.scope.declareName(moduleId).makeRef()
        }
    }
}
