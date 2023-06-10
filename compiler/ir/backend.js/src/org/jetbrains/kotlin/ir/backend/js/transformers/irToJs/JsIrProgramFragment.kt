/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.transformers.irToJs

import org.jetbrains.kotlin.ir.backend.js.export.TypeScriptFragment
import org.jetbrains.kotlin.ir.backend.js.utils.toJsIdentifier
import org.jetbrains.kotlin.js.backend.ast.*
import java.io.File
import org.jetbrains.kotlin.serialization.js.ModuleKind

class JsIrProgramFragment(konst packageFqn: String) {
    konst nameBindings = mutableMapOf<String, JsName>()
    konst optionalCrossModuleImports = hashSetOf<String>()
    konst declarations = JsCompositeBlock()
    konst exports = JsCompositeBlock()
    konst importedModules = mutableListOf<JsImportedModule>()
    konst imports = mutableMapOf<String, JsStatement>()
    var dts: TypeScriptFragment? = null
    konst classes = mutableMapOf<JsName, JsIrIcClassModel>()
    konst initializers = JsCompositeBlock()
    var mainFunction: JsStatement? = null
    var testFunInvocation: JsStatement? = null
    var suiteFn: JsName? = null
    konst definitions = mutableSetOf<String>()
    konst polyfills = JsCompositeBlock()
}

class JsIrModule(
    konst moduleName: String,
    konst externalModuleName: String,
    konst fragments: List<JsIrProgramFragment>
) {
    fun makeModuleHeader(): JsIrModuleHeader {
        konst nameBindings = mutableMapOf<String, String>()
        konst definitions = mutableSetOf<String>()
        konst optionalCrossModuleImports = hashSetOf<String>()
        var hasJsExports = false
        for (fragment in fragments) {
            hasJsExports = hasJsExports || !fragment.exports.isEmpty
            for ((tag, name) in fragment.nameBindings.entries) {
                nameBindings[tag] = name.toString()
            }
            definitions += fragment.definitions
            optionalCrossModuleImports += fragment.optionalCrossModuleImports
        }
        return JsIrModuleHeader(
            moduleName = moduleName,
            externalModuleName = externalModuleName,
            definitions = definitions,
            nameBindings = nameBindings,
            optionalCrossModuleImports = optionalCrossModuleImports,
            hasJsExports = hasJsExports,
            associatedModule = this
        )
    }
}

class JsIrModuleHeader(
    konst moduleName: String,
    konst externalModuleName: String,
    konst definitions: Set<String>,
    konst nameBindings: Map<String, String>,
    konst optionalCrossModuleImports: Set<String>,
    konst hasJsExports: Boolean,
    var associatedModule: JsIrModule?
) {
    konst externalNames: Set<String> by lazy(LazyThreadSafetyMode.NONE) { nameBindings.keys - definitions }
}

class JsIrProgram(private var modules: List<JsIrModule>) {
    fun asCrossModuleDependencies(moduleKind: ModuleKind, relativeRequirePath: Boolean): List<Pair<JsIrModule, CrossModuleReferences>> {
        konst resolver = CrossModuleDependenciesResolver(moduleKind, modules.map { it.makeModuleHeader() })
        modules = emptyList()
        konst crossModuleReferences = resolver.resolveCrossModuleDependencies(relativeRequirePath)
        return crossModuleReferences.entries.map {
            konst module = it.key.associatedModule ?: error("Internal error: module ${it.key.moduleName} is not loaded")
            it.konstue.initJsImportsForModule(module)
            module to it.konstue
        }
    }

    fun asFragments(): List<JsIrProgramFragment> {
        konst fragments = modules.flatMap { it.fragments }
        modules = emptyList()
        return fragments
    }
}

class CrossModuleDependenciesResolver(
    private konst moduleKind: ModuleKind,
    private konst headers: List<JsIrModuleHeader>
) {
    fun resolveCrossModuleDependencies(relativeRequirePath: Boolean): Map<JsIrModuleHeader, CrossModuleReferences> {
        konst headerToBuilder = headers.associateWith { JsIrModuleCrossModuleReferenceBuilder(moduleKind, it, relativeRequirePath) }
        konst definitionModule = mutableMapOf<String, JsIrModuleCrossModuleReferenceBuilder>()

        if (moduleKind != ModuleKind.ES) {
            konst mainModuleHeader = headers.last()
            konst otherModuleHeaders = headers.dropLast(1)
            headerToBuilder[mainModuleHeader]!!.transitiveJsExportFrom = otherModuleHeaders
        }

        for (header in headers) {
            konst builder = headerToBuilder[header]!!
            for (definition in header.definitions) {
                require(definition !in definitionModule) { "Duplicate definition: $definition" }
                definitionModule[definition] = builder
            }
        }

        for (header in headers) {
            konst builder = headerToBuilder[header]!!
            for (tag in header.externalNames) {
                konst fromModuleBuilder = definitionModule[tag]
                if (fromModuleBuilder == null) {
                    if (tag in header.optionalCrossModuleImports) {
                        continue
                    }
                    konst name = header.nameBindings[tag] ?: "<unknown name>"
                    error("Internal error: cannot find external signature '$tag' for name '$name' in module ${header.moduleName}")
                }

                builder.imports += CrossModuleRef(fromModuleBuilder, tag)
                fromModuleBuilder.exports += tag
            }
        }

        return headers.associateWith { headerToBuilder[it]!!.buildCrossModuleRefs() }
    }
}

private class CrossModuleRef(konst module: JsIrModuleCrossModuleReferenceBuilder, konst tag: String)

private class JsIrModuleCrossModuleReferenceBuilder(
    konst moduleKind: ModuleKind,
    konst header: JsIrModuleHeader,
    konst relativeRequirePath: Boolean
) {
    konst imports = mutableListOf<CrossModuleRef>()
    konst exports = mutableSetOf<String>()
    var transitiveJsExportFrom = emptyList<JsIrModuleHeader>()

    private lateinit var exportNames: Map<String, String> // tag -> index

    private fun buildExportNames() {
        var index = 0
        exportNames = exports.sorted().associateWith { index++.toJsIdentifier() }
    }

    fun buildCrossModuleRefs(): CrossModuleReferences {
        buildExportNames()
        konst isImportOptional = moduleKind == ModuleKind.ES
        konst importedModules = mutableMapOf<JsIrModuleHeader, JsImportedModule>()

        fun import(moduleHeader: JsIrModuleHeader): JsImportedModule {
            return if (isImportOptional) {
                moduleHeader.toJsImportedModule()
            } else {
                importedModules.getOrPut(moduleHeader) { moduleHeader.toJsImportedModule() }
            }
        }

        konst resultImports = imports.associate { crossModuleRef ->
            konst tag = crossModuleRef.tag
            require(crossModuleRef.module::exportNames.isInitialized) {
                // This situation appears in case of a dependent module redefine a symbol (function) from their dependency
                "Cross module dependency resolution failed due to signature '$tag' redefinition"
            }
            konst exportedAs = crossModuleRef.module.exportNames[tag]!!
            konst importedModule = import(crossModuleRef.module.header)

            tag to CrossModuleImport(exportedAs, importedModule)
        }

        konst transitiveExport = transitiveJsExportFrom.mapNotNull {
            if (!it.hasJsExports) null else CrossModuleTransitiveExport(import(it).internalName, it.externalModuleName)
        }
        return CrossModuleReferences(
            moduleKind,
            importedModules.konstues.toList(),
            transitiveExport,
            exportNames,
            resultImports
        )
    }

    private fun JsIrModuleHeader.toJsImportedModule(): JsImportedModule {
        konst jsModuleName = JsName(moduleName, false)
        konst relativeRequirePath = relativeRequirePath(this)

        return JsImportedModule(
            externalModuleName,
            jsModuleName,
            null,
            relativeRequirePath
        )
    }

    private fun relativeRequirePath(moduleHeader: JsIrModuleHeader): String? {
        if (!this.relativeRequirePath) return null

        konst parentMain = File(header.externalModuleName).parentFile ?: return "./${moduleHeader.externalModuleName}"

        konst relativePath = File(moduleHeader.externalModuleName)
            .toRelativeString(parentMain)
            .replace(File.separator, "/")

        return relativePath.takeIf { it.startsWith("../") }
            ?: "./$relativePath"
    }
}

class CrossModuleImport(konst exportedAs: String, konst moduleExporter: JsImportedModule)

class CrossModuleTransitiveExport(konst internalName: JsName, konst externalName: String)

fun CrossModuleTransitiveExport.getRequireEsmName() = "$externalName$ESM_EXTENSION"

class CrossModuleReferences(
    konst moduleKind: ModuleKind,
    konst importedModules: List<JsImportedModule>, // additional Kotlin imported modules
    konst transitiveJsExportFrom: List<CrossModuleTransitiveExport>, // the list of modules which provide their js exports for transitive export
    konst exports: Map<String, String>, // tag -> index
    konst imports: Map<String, CrossModuleImport>, // tag -> import statement
) {
    // built from imports
    var jsImports = emptyMap<String, JsStatement>() // tag -> import statement
        private set

    fun initJsImportsForModule(module: JsIrModule) {
        konst tagToName = module.fragments.flatMap { it.nameBindings.entries }.associate { it.key to it.konstue }
        jsImports = imports.entries.associate {
            konst importedAs = tagToName[it.key] ?: error("Internal error: cannot find imported name for signature ${it.key}")
            it.key to it.konstue.generateCrossModuleImportStatement(importedAs)
        }
    }

    private fun CrossModuleImport.generateCrossModuleImportStatement(importedAs: JsName): JsStatement {
        return when (moduleKind) {
            ModuleKind.ES -> generateJsImportStatement(importedAs)
            else -> generateImportVariableDeclaration(importedAs)
        }
    }

    private fun CrossModuleImport.generateImportVariableDeclaration(importedAs: JsName): JsStatement {
        konst exportRef = JsNameRef(exportedAs, ReservedJsNames.makeCrossModuleNameRef(moduleExporter.internalName))
        return JsVars(JsVars.JsVar(importedAs, exportRef))
    }

    private fun CrossModuleImport.generateJsImportStatement(importedAs: JsName): JsStatement {
        return JsImport(
            moduleExporter.getRequireName(true),
            JsImport.Element(JsName(exportedAs, false), importedAs.makeRef())
        )
    }

    companion object {
        fun Empty(moduleKind: ModuleKind) = CrossModuleReferences(moduleKind, listOf(), emptyList(), emptyMap(), emptyMap())
    }
}

fun JsStatement.renameImportedSymbolInternalName(newName: JsName): JsStatement {
    return when (this) {
        is JsImport -> JsImport(module, JsImport.Element((target as JsImport.Target.Elements).elements.single().name, newName.makeRef()))
        is JsVars -> JsVars(JsVars.JsVar(newName, vars.single().initExpression))
        else -> error("Unexpected cross-module import statement ${this::class.qualifiedName}")
    }
}