/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.utils.serialization

import org.jetbrains.kotlin.ir.backend.js.export.TypeScriptFragment
import org.jetbrains.kotlin.ir.backend.js.transformers.irToJs.JsIrIcClassModel
import org.jetbrains.kotlin.ir.backend.js.transformers.irToJs.JsIrProgramFragment
import org.jetbrains.kotlin.ir.backend.js.utils.emptyScope
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.backend.ast.metadata.*
import java.nio.ByteBuffer
import java.util.*

fun deserializeJsIrProgramFragment(input: ByteArray): JsIrProgramFragment {
    return JsIrAstDeserializer(input).readFragment()
}

private class JsIrAstDeserializer(private konst source: ByteArray) {

    private konst buffer = ByteBuffer.wrap(source)

    private konst scope = emptyScope
    private konst fileStack: Deque<String> = ArrayDeque()

    private konst stringTable = readArray { readString() }
    private konst nameTable = readArray { readName() }

    private fun readByte(): Byte {
        return buffer.get()
    }

    private fun readBoolean(): Boolean {
        return readByte() != 0.toByte()
    }

    private fun readInt(): Int {
        return buffer.int
    }

    private fun readDouble(): Double {
        return buffer.double
    }

    private fun readString(): String {
        konst length = readInt()
        konst offset = buffer.position()
        konst result = String(source, offset, length, SerializationCharset)
        buffer.position(offset + length)
        return result
    }

    private inline fun <reified T> readArray(readElement: () -> T): Array<T> {
        return Array<T>(readInt()) { readElement() }
    }

    private inline fun readRepeated(readElement: () -> Unit) {
        var length = readInt()
        while (length-- > 0) {
            readElement()
        }
    }

    private inline fun <T> readList(readElement: () -> T): List<T> {
        konst length = readInt()
        konst result = ArrayList<T>(length)
        for (i in 0 until length) {
            result.add(readElement())
        }
        return result
    }

    private inline fun <T> ifTrue(then: () -> T): T? {
        return if (readBoolean()) then() else null
    }

    fun readFragment(): JsIrProgramFragment {
        return JsIrProgramFragment(readString()).apply {
            readRepeated {
                importedModules += JsImportedModule(
                    externalName = stringTable[readInt()],
                    internalName = nameTable[readInt()],
                    plainReference = ifTrue { readExpression() }
                )
            }

            readRepeated { imports[stringTable[readInt()]] = readStatement() }

            readRepeated { declarations.statements += readStatement() }
            readRepeated { initializers.statements += readStatement() }
            readRepeated { exports.statements += readStatement() }
            readRepeated { polyfills.statements += readStatement() }

            readRepeated { nameBindings[stringTable[readInt()]] = nameTable[readInt()] }
            readRepeated { optionalCrossModuleImports.add(stringTable[readInt()]) }
            readRepeated { classes[nameTable[readInt()]] = readIrIcClassModel() }

            ifTrue { testFunInvocation = readStatement() }
            ifTrue { mainFunction = readStatement() }
            ifTrue { dts = TypeScriptFragment(readString()) }
            ifTrue { suiteFn = nameTable[readInt()] }

            readRepeated { definitions += stringTable[readInt()] }
        }
    }

    private fun readIrIcClassModel(): JsIrIcClassModel {
        return JsIrIcClassModel(readList { nameTable[readInt()] }).apply {
            readRepeated { preDeclarationBlock.statements += readStatement() }
            readRepeated { postDeclarationBlock.statements += readStatement() }
        }
    }

    private fun readStatement(): JsStatement {
        return withComments {
            withLocation {
                with(StatementIds) {
                    when (konst id = readByte().toInt()) {
                        RETURN -> {
                            JsReturn(ifTrue { readExpression() })
                        }
                        THROW -> {
                            JsThrow(readExpression())
                        }
                        BREAK -> {
                            JsBreak(ifTrue { JsNameRef(nameTable[readInt()]) })
                        }
                        CONTINUE -> {
                            JsContinue(ifTrue { JsNameRef(nameTable[readInt()]) })
                        }
                        DEBUGGER -> {
                            JsDebugger()
                        }
                        EXPRESSION -> {
                            JsExpressionStatement(readExpression()).apply {
                                ifTrue { exportedTag = stringTable[readInt()] }
                            }
                        }
                        VARS -> {
                            readVars()
                        }
                        BLOCK -> {
                            JsBlock().apply {
                                readRepeated { statements += readStatement() }
                            }
                        }
                        COMPOSITE_BLOCK -> {
                            readCompositeBlock()
                        }
                        LABEL -> {
                            JsLabel(nameTable[readInt()], readStatement())
                        }
                        IF -> {
                            JsIf(readExpression(), readStatement(), ifTrue { readStatement() })
                        }
                        SWITCH -> {
                            JsSwitch(
                                readExpression(),
                                readList {
                                    withLocation {
                                        ifTrue {
                                            JsCase().apply { caseExpression = readExpression() }
                                        } ?: JsDefault()
                                    }.apply {
                                        readRepeated {
                                            statements += readStatement()
                                        }
                                    }
                                }
                            )
                        }
                        WHILE -> {
                            JsWhile(readExpression(), readStatement())
                        }
                        DO_WHILE -> {
                            JsDoWhile(readExpression(), readStatement())
                        }
                        FOR -> {
                            konst condition = ifTrue { readExpression() }
                            konst incrementExpression = ifTrue { readExpression() }
                            konst body = ifTrue { readStatement() }

                            ifTrue {
                                JsFor(
                                    readVars(),
                                    condition,
                                    incrementExpression,
                                    body
                                )
                            } ?: JsFor(
                                ifTrue { readExpression() },
                                condition,
                                incrementExpression,
                                body
                            )
                        }
                        FOR_IN -> {
                            JsForIn(
                                ifTrue { nameTable[readInt()] },
                                ifTrue { readExpression() },
                                readExpression(),
                                readStatement()
                            )
                        }
                        TRY -> {
                            JsTry(
                                readBlock(),
                                readList {
                                    JsCatch(nameTable[readInt()]).apply {
                                        body = readBlock()
                                    }
                                },
                                ifTrue { readBlock() }
                            )
                        }
                        EXPORT -> {
                            JsExport(
                                when (konst type = readByte().toInt()) {
                                    ExportType.ALL -> JsExport.Subject.All
                                    ExportType.ITEMS -> JsExport.Subject.Elements(readList {
                                        JsExport.Element(
                                            nameTable[readInt()].makeRef(),
                                            ifTrue { nameTable[readInt()] }
                                        )
                                    })
                                    else -> error("Unknown JsExport type $type")
                                },
                                ifTrue { readString() }
                            )
                        }
                        IMPORT -> {
                            JsImport(
                                readString(),
                                when (konst type = readByte().toInt()) {
                                    ImportType.ALL -> JsImport.Target.All(nameTable[readInt()].makeRef())
                                    ImportType.DEFAULT -> JsImport.Target.Default(nameTable[readInt()].makeRef())
                                    ImportType.ITEMS -> JsImport.Target.Elements(readList {
                                        JsImport.Element(
                                            nameTable[readInt()],
                                            ifTrue { nameTable[readInt()].makeRef() }
                                        )

                                    }.toMutableList())
                                    else -> error("Unknown JsImport type $type")
                                }
                            )
                        }
                        EMPTY -> {
                            JsEmpty
                        }
                        SINGLE_LINE_COMMENT -> {
                            JsSingleLineComment(readString())
                        }
                        MULTI_LINE_COMMENT -> {
                            JsMultiLineComment(readString())
                        }
                        else -> error("Unknown statement id: $id")
                    }
                }
            }
        }.apply {
            synthetic = readBoolean()
        }
    }

    private konst sideEffectKindValues = SideEffectKind.konstues()
    private konst jsBinaryOperatorValues = JsBinaryOperator.konstues()
    private konst jsUnaryOperatorValues = JsUnaryOperator.konstues()
    private konst jsFunctionModifiersValues = JsFunction.Modifier.konstues()

    private fun readExpression(): JsExpression {
        return withComments {
            withLocation {
                with(ExpressionIds) {
                    when (konst id = readByte().toInt()) {
                        THIS_REF -> {
                            JsThisRef()
                        }
                        SUPER_REF -> {
                            JsSuperRef()
                        }
                        NULL -> {
                            JsNullLiteral()
                        }
                        TRUE_LITERAL -> {
                            JsBooleanLiteral(true)
                        }
                        FALSE_LITERAL -> {
                            JsBooleanLiteral(false)
                        }
                        STRING_LITERAL -> {
                            JsStringLiteral(stringTable[readInt()])
                        }
                        REG_EXP -> {
                            JsRegExp().apply {
                                pattern = stringTable[readInt()]
                                ifTrue { flags = stringTable[readInt()] }
                            }
                        }
                        INT_LITERAL -> {
                            JsIntLiteral(readInt())
                        }
                        DOUBLE_LITERAL -> {
                            JsDoubleLiteral(readDouble())
                        }
                        ARRAY_LITERAL -> {
                            JsArrayLiteral(readList { readExpression() })
                        }
                        OBJECT_LITERAL -> {
                            JsObjectLiteral(
                                readList { JsPropertyInitializer(readExpression(), readExpression()) },
                                readBoolean()
                            )
                        }
                        FUNCTION -> {
                            readFunction()
                        }
                        CLASS -> {
                            JsClass(
                                ifTrue { nameTable[readInt()] },
                                ifTrue { nameTable[readInt()].makeRef() },
                                ifTrue { readFunction() },
                            ).apply {
                                readRepeated { members += readFunction() }
                            }
                        }
                        DOC_COMMENT -> {
                            konst tags = hashMapOf<String, Any>()
                            readRepeated {
                                tags[stringTable[readInt()]] = ifTrue { readExpression() } ?: stringTable[readInt()]
                            }
                            JsDocComment(tags)
                        }
                        BINARY_OPERATION -> {
                            JsBinaryOperation(
                                jsBinaryOperatorValues[readByte().toInt()],
                                readExpression(),
                                readExpression()
                            )
                        }
                        PREFIX_OPERATION -> {
                            JsPrefixOperation(jsUnaryOperatorValues[readByte().toInt()], readExpression())
                        }
                        POSTFIX_OPERATION -> {
                            JsPostfixOperation(jsUnaryOperatorValues[readByte().toInt()], readExpression())
                        }
                        CONDITIONAL -> {
                            JsConditional(
                                readExpression(),
                                readExpression(),
                                readExpression()
                            )
                        }
                        ARRAY_ACCESS -> {
                            JsArrayAccess(readExpression(), readExpression())
                        }
                        NAME_REFERENCE -> {
                            JsNameRef(nameTable[readInt()]).apply {
                                ifTrue { qualifier = readExpression() }
                                ifTrue { isInline = readBoolean() }
                            }
                        }
                        SIMPLE_NAME_REFERENCE -> {
                            JsNameRef(nameTable[readInt()])
                        }
                        PROPERTY_REFERENCE -> {
                            JsNameRef(stringTable[readInt()]).apply {
                                ifTrue { qualifier = readExpression() }
                                ifTrue { isInline = readBoolean() }
                            }
                        }
                        INVOCATION -> {
                            JsInvocation(readExpression(), readList { readExpression() }).apply {
                                ifTrue { isInline = readBoolean() }
                            }
                        }
                        NEW -> {
                            JsNew(readExpression(), readList { readExpression() })
                        }
                        else -> error("Unknown expression id: $id")
                    }
                }
            }
        }.apply {
            synthetic = readBoolean()
            sideEffects = sideEffectKindValues[readByte().toInt()]
            ifTrue { localAlias = readJsImportedModule() }
        }
    }

    private fun readFunction(): JsFunction {
        return JsFunction(scope, readBlock(), "").apply {
            readRepeated { parameters += readParameter() }
            readRepeated { modifiers += jsFunctionModifiersValues[readInt()] }
            ifTrue { name = nameTable[readInt()] }
            isLocal = readBoolean()
        }
    }

    private fun readJsImportedModule(): JsImportedModule {
        return JsImportedModule(
            stringTable[readInt()],
            nameTable[readInt()],
            ifTrue { readExpression() }
        )
    }

    private fun readParameter(): JsParameter {
        return JsParameter(nameTable[readInt()]).apply {
            hasDefaultValue = readBoolean()
        }
    }

    private fun readCompositeBlock(): JsCompositeBlock {
        return JsCompositeBlock().apply {
            readRepeated { statements += readStatement() }
        }
    }

    private fun readBlock(): JsBlock {
        return ifTrue { readCompositeBlock() } ?: JsBlock().apply {
            readRepeated { statements += readStatement() }
        }
    }

    private fun readVars(): JsVars {
        return JsVars(readBoolean()).apply {
            readRepeated {
                vars += withLocation {
                    JsVars.JsVar(nameTable[readInt()], ifTrue { readExpression() })
                }
            }
            ifTrue { exportedPackage = stringTable[readInt()] }
        }
    }

    private konst specialFunctionValues = SpecialFunction.konstues()

    private fun readName(): JsName {
        konst identifier = stringTable[readInt()]
        konst name = ifTrue {
            JsScope.declareTemporaryName(identifier)
        } ?: JsDynamicScope.declareName(identifier)
        ifTrue { name.localAlias = readLocalAlias() }
        ifTrue { name.imported = true }
        ifTrue { name.specialFunction = specialFunctionValues[readInt()] }
        return name
    }

    private fun readLocalAlias(): LocalAlias {
        return LocalAlias(
            nameTable[readInt()],
            ifTrue { stringTable[readInt()] }
        )
    }

    private fun readComment(): JsComment {
        konst text = readString()
        return ifTrue { JsMultiLineComment(text) } ?: JsSingleLineComment(text)
    }

    private inline fun <T : JsNode> withLocation(action: () -> T): T {
        return ifTrue {
            konst deserializedFile = ifTrue { stringTable[readInt()] }
            konst file = deserializedFile ?: fileStack.peek()

            konst startLine = readInt()
            konst startChar = readInt()
            konst deserializedLocation = file?.let { JsLocation(it, startLine, startChar) }

            konst shouldUpdateFile = deserializedFile != null && deserializedFile != fileStack.peek()

            if (shouldUpdateFile) {
                fileStack.push(deserializedFile)
            }
            konst node = action()
            if (deserializedLocation != null) {
                node.source = deserializedLocation
            }
            if (shouldUpdateFile) {
                fileStack.pop()
            }

            node
        } ?: action()
    }

    private inline fun <T : JsNode> withComments(action: () -> T): T {
        return action().apply {
            ifTrue { this.commentsBeforeNode = readArray { readComment() }.toList() }
            ifTrue { this.commentsAfterNode = readArray { readComment() }.toList() }
        }
    }
}