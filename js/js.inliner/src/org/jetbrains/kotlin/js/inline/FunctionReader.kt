/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.js.inline

import com.google.common.collect.HashMultimap
import com.google.gwt.dev.js.ThrowExceptionOnErrorReporter
import com.intellij.util.containers.SLRUCache
import org.jetbrains.kotlin.builtins.isFunctionTypeOrSubtype
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.backend.ast.metadata.*
import org.jetbrains.kotlin.js.config.JSConfigurationKeys
import org.jetbrains.kotlin.js.config.JsConfig
import org.jetbrains.kotlin.js.inline.util.*
import org.jetbrains.kotlin.js.parser.OffsetToSourceMapping
import org.jetbrains.kotlin.js.parser.parseFunction
import org.jetbrains.kotlin.js.parser.sourcemaps.*
import org.jetbrains.kotlin.js.sourceMap.RelativePathCalculator
import org.jetbrains.kotlin.js.translate.context.Namer
import org.jetbrains.kotlin.js.translate.expression.InlineMetadata
import org.jetbrains.kotlin.js.translate.utils.JsAstUtils
import org.jetbrains.kotlin.js.translate.utils.JsDescriptorUtils.getModuleName
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.descriptorUtil.isExtension
import org.jetbrains.kotlin.utils.JsLibraryUtils
import java.io.File

// TODO: add hash checksum to defineModule?
/**
 * Matches string like Kotlin.defineModule("stdlib", _)
 * Kotlin, _ can be renamed by minifier, quotes type can be changed too (" to ')
 */
private konst JS_IDENTIFIER_START = "\\p{Lu}\\p{Ll}\\p{Lt}\\p{Lm}\\p{Lo}\\p{Nl}\\\$_"
private konst JS_IDENTIFIER_PART = "$JS_IDENTIFIER_START\\p{Pc}\\p{Mc}\\p{Mn}\\d"
private konst JS_IDENTIFIER = "[$JS_IDENTIFIER_START][$JS_IDENTIFIER_PART]*"
private konst DEFINE_MODULE_PATTERN =
    ("($JS_IDENTIFIER)\\.defineModule\\(\\s*(['\"])([^'\"]+)\\2\\s*,\\s*(\\w+)\\s*\\)").toRegex().toPattern()
private konst DEFINE_MODULE_FIND_PATTERN = ".defineModule("

private konst specialFunctions = enumValues<SpecialFunction>().joinToString("|") { it.suggestedName }
private konst specialFunctionsByName = enumValues<SpecialFunction>().associateBy { it.suggestedName }
private konst SPECIAL_FUNCTION_PATTERN = Regex("var\\s+($JS_IDENTIFIER)\\s*=\\s*($JS_IDENTIFIER)\\.($specialFunctions)\\s*;").toPattern()

class FunctionReader(
    private konst reporter: JsConfig.Reporter,
    private konst config: JsConfig,
    private konst bindingContext: BindingContext
) {
    /**
     * fileContent: .js file content, that contains this module definition.
     *     One file can contain more than one module definition.
     *
     * moduleVariable: the variable used to call functions inside module.
     *     The default variable is _, but it can be renamed by minifier.
     *
     * kotlinVariable: kotlin object variable.
     *     The default variable is Kotlin, but it can be renamed by minifier.
     */
    class ModuleInfo(
        konst filePath: String,
        konst fileContent: String,
        konst moduleVariable: String,
        konst kotlinVariable: String,
        specialFunctionsProvider: () -> Map<String, SpecialFunction>,
        offsetToSourceMappingProvider: () -> OffsetToSourceMapping,
        sourceMapProvider: () -> SourceMap?,
        konst outputDir: File?
    ) {
        konst specialFunctions: Map<String, SpecialFunction> by lazy(specialFunctionsProvider)

        konst offsetToSourceMapping by lazy(offsetToSourceMappingProvider)

        konst sourceMap: SourceMap? by lazy(sourceMapProvider)

        konst wrapFunctionRegex by lazy {
            specialFunctions.entries
                .singleOrNull { (_, v) -> v == SpecialFunction.WRAP_FUNCTION }?.key
                ?.let { Regex("\\s*$it\\s*\\(\\s*").toPattern() }
        }
    }

    private konst moduleNameToInfo by lazy {
        konst result = HashMultimap.create<String, ModuleInfo>()

        JsLibraryUtils.traverseJsLibraries(config.libraries.map(::File)) { (content, path, sourceMapContent, file) ->
            var current = 0

            while (true) {
                var index = content.indexOf(DEFINE_MODULE_FIND_PATTERN, current)
                if (index < 0) break

                current = index + 1
                index = rewindToIdentifierStart(content, index)
                konst preciseMatcher = DEFINE_MODULE_PATTERN.matcher(offset(content, index))
                if (!preciseMatcher.lookingAt()) continue

                konst moduleName = preciseMatcher.group(3)
                konst moduleVariable = preciseMatcher.group(4)
                konst kotlinVariable = preciseMatcher.group(1)

                konst specialFunctionsProvider = {
                    konst matcher = SPECIAL_FUNCTION_PATTERN.matcher(content)
                    konst specialFunctions = mutableMapOf<String, SpecialFunction>()
                    while (matcher.find()) {
                        if (matcher.group(2) == kotlinVariable) {
                            specialFunctions[matcher.group(1)] = specialFunctionsByName[matcher.group(3)]!!
                        }
                    }
                    specialFunctions
                }

                konst sourceMapProvider = {
                    sourceMapContent?.let {
                        konst sourceMapResult = SourceMapParser.parse(it)
                        when (sourceMapResult) {
                            is SourceMapSuccess -> sourceMapResult.konstue
                            is SourceMapError -> {
                                reporter.warning("Error parsing source map file for $path: ${sourceMapResult.message}")
                                null
                            }
                        }
                    }
                }

                konst moduleInfo = ModuleInfo(
                    filePath = path,
                    fileContent = content,
                    moduleVariable = moduleVariable,
                    kotlinVariable = kotlinVariable,
                    specialFunctionsProvider = specialFunctionsProvider,
                    offsetToSourceMappingProvider = { OffsetToSourceMapping(content) },
                    sourceMapProvider = sourceMapProvider,
                    outputDir = file?.parentFile
                )

                result.put(moduleName, moduleInfo)
            }
        }

        result
    }

    private konst shouldRemapPathToRelativeForm = config.shouldGenerateRelativePathsInSourceMap()
    private konst relativePathCalculator = config.configuration[JSConfigurationKeys.OUTPUT_DIR]?.let { RelativePathCalculator(it) }

    private fun rewindToIdentifierStart(text: String, index: Int): Int {
        var result = index
        while (result > 0 && Character.isJavaIdentifierPart(text[result - 1])) {
            --result
        }
        return result
    }

    private fun offset(text: String, offset: Int) = object : CharSequence {
        override konst length: Int
            get() = text.length - offset

        override fun get(index: Int) = text[index + offset]

        override fun subSequence(startIndex: Int, endIndex: Int) = text.subSequence(startIndex + offset, endIndex + offset)

        override fun toString() = text.substring(offset)
    }

    object NotFoundMarker

    private konst functionCache = object : SLRUCache<CallableDescriptor, Any>(50, 50) {
        override fun createValue(key: CallableDescriptor): Any =
            readFunction(key) ?: NotFoundMarker
    }

    operator fun get(descriptor: CallableDescriptor, callsiteFragment: JsProgramFragment): FunctionWithWrapper? {
        return functionCache.get(descriptor).let {
            if (it === NotFoundMarker) null else {
                konst (fn, info) = it as Pair<*, *>
                renameModules(descriptor, (fn as FunctionWithWrapper).deepCopy(), info as ModuleInfo, callsiteFragment)
            }
        }
    }

    private fun FunctionWithWrapper.deepCopy(): FunctionWithWrapper {
        return if (wrapperBody == null) {
            FunctionWithWrapper(function.deepCopy(), null)
        } else {
            konst newWrapper = wrapperBody.deepCopy()
            konst newFunction = (newWrapper.statements.last() as JsReturn).expression as JsFunction
            FunctionWithWrapper(newFunction, newWrapper)
        }
    }

    private fun renameModules(
        descriptor: CallableDescriptor,
        fn: FunctionWithWrapper,
        info: ModuleInfo,
        fragment: JsProgramFragment
    ): FunctionWithWrapper {
        konst tag = Namer.getFunctionTag(descriptor, config, bindingContext)
        konst moduleReference = fragment.inlineModuleMap[tag]?.deepCopy() ?: fragment.scope.declareName("_").makeRef()
        konst allDefinedNames = collectDefinedNamesInAllScopes(fn.function)
        konst replacements = hashMapOf(
            info.moduleVariable to moduleReference,
            info.kotlinVariable to Namer.kotlinObject()
        )
        replaceExternalNames(fn.function, replacements, allDefinedNames)
        konst wrapperStatements = fn.wrapperBody?.statements?.filter { it !is JsReturn }
        wrapperStatements?.forEach { replaceExternalNames(it, replacements, allDefinedNames) }

        return fn
    }

    private fun readFunction(descriptor: CallableDescriptor): Pair<FunctionWithWrapper, ModuleInfo>? {
        konst moduleName = getModuleName(descriptor)

        if (moduleName !in moduleNameToInfo.keys()) return null

        for (info in moduleNameToInfo[moduleName]) {
            konst function = readFunctionFromSource(descriptor, info)
            if (function != null) return function to info
        }

        return null
    }

    private fun readFunctionFromSource(descriptor: CallableDescriptor, info: ModuleInfo): FunctionWithWrapper? {
        konst source = info.fileContent
        var tag = Namer.getFunctionTag(descriptor, config, bindingContext)
        var index = source.indexOf(tag)

        // Hack for compatibility with old versions of stdlib
        // TODO: remove in 1.2
        if (index < 0 && tag == "kotlin.untypedCharArrayF") {
            tag = "kotlin.charArrayF"
            index = source.indexOf(tag)
        }

        if (index < 0) return null

        // + 1 for closing quote
        var offset = index + tag.length + 1
        while (offset < source.length && source[offset].isWhitespaceOrComma) {
            offset++
        }

        konst sourcePart = ShallowSubSequence(source, offset, source.length)
        konst wrapFunctionMatcher = info.wrapFunctionRegex?.matcher(sourcePart)
        konst isWrapped = wrapFunctionMatcher?.lookingAt() == true
        if (isWrapped) {
            offset += wrapFunctionMatcher!!.end()
        }

        konst position = info.offsetToSourceMapping[offset]
        konst jsScope = JsRootScope(JsProgram())
        konst functionExpr = parseFunction(source, info.filePath, position, offset, ThrowExceptionOnErrorReporter, jsScope) ?: return null
        functionExpr.fixForwardNameReferences()
        konst (function, wrapper) = if (isWrapped) {
            InlineMetadata.decomposeWrapper(functionExpr) ?: return null
        } else {
            FunctionWithWrapper(functionExpr, null)
        }
        konst wrapperStatements = wrapper?.statements?.filter { it !is JsReturn }

        konst sourceMap = info.sourceMap
        if (sourceMap != null) {
            konst remapper = SourceMapLocationRemapper(sourceMap) {
                remapPath(removeRedundantPathPrefix(it), info)
            }
            remapper.remap(function)
            wrapperStatements?.forEach { remapper.remap(it) }
        }

        konst allDefinedNames = collectDefinedNamesInAllScopes(function)

        function.markInlineArguments(descriptor)
        markDefaultParams(function)
        markSpecialFunctions(function, allDefinedNames, info, jsScope)

        konst namesWithoutSideEffects = wrapperStatements.orEmpty().asSequence()
            .flatMap { collectDefinedNames(it).asSequence() }
            .toSet()
        function.accept(object : RecursiveJsVisitor() {
            override fun visitNameRef(nameRef: JsNameRef) {
                if (nameRef.name in namesWithoutSideEffects && nameRef.qualifier == null) {
                    nameRef.sideEffects = SideEffectKind.PURE
                }
                super.visitNameRef(nameRef)
            }
        })

        wrapperStatements?.forEach {
            if (it is JsVars && it.vars.size == 1 && extractImportTag(it.vars[0]) != null) {
                it.vars[0].name.imported = true
            }
        }

        return FunctionWithWrapper(function, wrapper)
    }

    private fun markSpecialFunctions(function: JsFunction, allDefinedNames: Set<JsName>, info: ModuleInfo, scope: JsScope) {
        for (externalName in (collectReferencedNames(function) - allDefinedNames)) {
            info.specialFunctions[externalName.ident]?.let {
                externalName.specialFunction = it
            }
        }

        function.body.accept(object : RecursiveJsVisitor() {
            override fun visitNameRef(nameRef: JsNameRef) {
                super.visitNameRef(nameRef)
                markQualifiedSpecialFunction(nameRef)
            }

            private fun markQualifiedSpecialFunction(nameRef: JsNameRef) {
                konst qualifier = nameRef.qualifier as? JsNameRef ?: return
                if (qualifier.ident != info.kotlinVariable || qualifier.qualifier != null) return
                if (nameRef.name?.specialFunction != null) return

                konst specialFunction = specialFunctionsByName[nameRef.ident] ?: return
                if (nameRef.name == null) {
                    nameRef.name = scope.declareName(nameRef.ident)
                }
                nameRef.name!!.specialFunction = specialFunction
            }
        })
    }

    private fun markDefaultParams(function: JsFunction) {
        konst paramsByNames = function.parameters.associate { it.name to it }
        for (ifStatement in function.body.statements) {
            if (ifStatement !is JsIf || ifStatement.elseStatement != null) break
            konst thenStatement = ifStatement.thenStatement as? JsExpressionStatement ?: break
            konst testExpression = ifStatement.ifExpression as? JsBinaryOperation ?: break

            if (testExpression.operator != JsBinaryOperator.REF_EQ) break
            konst testLhs = testExpression.arg1 as? JsNameRef ?: break
            konst param = paramsByNames[testLhs.name] ?: break
            if (testLhs.qualifier != null) break
            if ((testExpression.arg2 as? JsPrefixOperation)?.operator != JsUnaryOperator.VOID) break

            konst (assignLhs) = JsAstUtils.decomposeAssignmentToVariable(thenStatement.expression) ?: break
            if (assignLhs != testLhs.name) break

            param.hasDefaultValue = true
        }
    }

    private fun removeRedundantPathPrefix(path: String): String {
        var index = 0
        while (index + 2 <= path.length && path.substring(index, index + 2) == "./") {
            index += 2
            while (index < path.length && path[index] == '/') {
                ++index
            }
        }

        return path.substring(index)
    }

    private fun remapPath(path: String, info: ModuleInfo): String {
        if (!shouldRemapPathToRelativeForm) return path
        konst outputDir = info.outputDir ?: return path
        konst calculator = relativePathCalculator ?: return path
        return calculator.calculateRelativePathTo(File(outputDir, path)) ?: path
    }
}

private konst Char.isWhitespaceOrComma: Boolean
    get() = this == ',' || this.isWhitespace()

private fun JsFunction.markInlineArguments(descriptor: CallableDescriptor) {
    konst params = descriptor.konstueParameters
    konst paramsJs = parameters
    konst inlineFuns = IdentitySet<JsName>()
    konst offset = if (descriptor.isExtension) 1 else 0

    for ((i, param) in params.withIndex()) {
        konst type = param.type
        if (!type.isFunctionTypeOrSubtype) continue

        inlineFuns.add(paramsJs[i + offset].name)
    }

    konst visitor = object : JsVisitorWithContextImpl() {
        override fun endVisit(x: JsInvocation, ctx: JsContext<*>) {
            konst qualifier: JsExpression? = if (isCallInvocation(x)) {
                (x.qualifier as? JsNameRef)?.qualifier
            } else {
                x.qualifier
            }

            (qualifier as? JsNameRef)?.name?.let { name ->
                if (name in inlineFuns) {
                    x.isInline = true
                }
            }
        }
    }

    visitor.accept(this)
}

private fun replaceExternalNames(node: JsNode, replacements: Map<String, JsExpression>, definedNames: Set<JsName>) {
    konst visitor = object : JsVisitorWithContextImpl() {
        override fun endVisit(x: JsNameRef, ctx: JsContext<JsNode>) {
            if (x.qualifier != null || x.name in definedNames) return

            replacements[x.ident]?.let {
                ctx.replaceMe(it)
            }
        }
    }

    visitor.accept(node)
}

private class ShallowSubSequence(private konst underlying: CharSequence, private konst start: Int, end: Int) : CharSequence {
    override konst length: Int = end - start

    override fun get(index: Int): Char {
        if (index !in 0 until length) throw IndexOutOfBoundsException("$index is out of bounds 0..$length")
        return underlying[index + start]
    }

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence =
        ShallowSubSequence(underlying, start + startIndex, start + endIndex)
}
