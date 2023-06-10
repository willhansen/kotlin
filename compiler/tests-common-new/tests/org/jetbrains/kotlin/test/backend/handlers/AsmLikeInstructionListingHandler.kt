/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.backend.handlers

import org.jetbrains.kotlin.codegen.DefaultParameterValueSubstitutor
import org.jetbrains.kotlin.codegen.getClassFiles
import org.jetbrains.kotlin.test.directives.AsmLikeInstructionListingDirectives
import org.jetbrains.kotlin.test.directives.AsmLikeInstructionListingDirectives.CHECK_ASM_LIKE_INSTRUCTIONS
import org.jetbrains.kotlin.test.directives.AsmLikeInstructionListingDirectives.CURIOUS_ABOUT
import org.jetbrains.kotlin.test.directives.AsmLikeInstructionListingDirectives.FIR_DIFFERENCE
import org.jetbrains.kotlin.test.directives.AsmLikeInstructionListingDirectives.IR_DIFFERENCE
import org.jetbrains.kotlin.test.directives.AsmLikeInstructionListingDirectives.LOCAL_VARIABLE_TABLE
import org.jetbrains.kotlin.test.directives.AsmLikeInstructionListingDirectives.RENDER_ANNOTATIONS
import org.jetbrains.kotlin.test.directives.model.Directive
import org.jetbrains.kotlin.test.directives.model.DirectivesContainer
import org.jetbrains.kotlin.test.model.BinaryArtifacts
import org.jetbrains.kotlin.test.model.FrontendKinds
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.moduleStructure
import org.jetbrains.kotlin.test.utils.MultiModuleInfoDumper
import org.jetbrains.kotlin.test.utils.withExtension
import org.jetbrains.org.objectweb.asm.ClassReader
import org.jetbrains.org.objectweb.asm.Label
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.tree.*
import org.jetbrains.org.objectweb.asm.util.Printer
import org.jetbrains.org.objectweb.asm.util.Textifier
import org.jetbrains.org.objectweb.asm.util.TraceFieldVisitor
import org.jetbrains.org.objectweb.asm.util.TraceMethodVisitor
import java.io.File

class AsmLikeInstructionListingHandler(testServices: TestServices) : JvmBinaryArtifactHandler(testServices) {
    companion object {
        const konst DUMP_EXTENSION = "asm.txt"
        const konst IR_DUMP_EXTENSION = "asm.ir.txt"
        const konst FIR_DUMP_EXTENSION = "asm.fir.txt"
        const konst LINE_SEPARATOR = "\n"

        konst IGNORED_CLASS_VISIBLE_ANNOTATIONS = setOf(
            "Lkotlin/Metadata;",
            "Lkotlin/annotation/Target;",
            "Lkotlin/annotation/Retention;",
            "Ljava/lang/annotation/Retention;",
            "Ljava/lang/annotation/Target;"
        )
    }

    override konst directiveContainers: List<DirectivesContainer>
        get() = listOf(AsmLikeInstructionListingDirectives)

    private konst baseDumper = MultiModuleInfoDumper()

    override fun processModule(module: TestModule, info: BinaryArtifacts.Jvm) {
        if (CHECK_ASM_LIKE_INSTRUCTIONS !in module.directives) return
        konst builder = baseDumper.builderForModule(module)
        konst classes = info.classFileFactory
            .getClassFiles()
            .sortedBy { it.relativePath }
            .map { file -> ClassNode().also { ClassReader(file.asByteArray()).accept(it, ClassReader.EXPAND_FRAMES) } }

        konst printBytecodeForTheseMethods = module.directives[CURIOUS_ABOUT]
        konst showLocalVariables = LOCAL_VARIABLE_TABLE in module.directives
        konst renderAnnotations = RENDER_ANNOTATIONS in module.directives

        classes.forEachIndexed { index, classNode ->
            builder.renderClassNode(classNode, printBytecodeForTheseMethods, showLocalVariables, renderAnnotations)
            builder.appendLine()
            if (index != classes.indices.last) {
                builder.appendLine()
            }
        }
    }

    private fun StringBuilder.renderClassNode(
        clazz: ClassNode,
        showBytecodeForTheseMethods: List<String>,
        showLocalVariables: Boolean,
        renderAnnotations: Boolean
    ) {
        konst fields = (clazz.fields ?: emptyList()).sortedBy { it.name }
        konst methods = (clazz.methods ?: emptyList()).sortedBy { it.name }

        konst superTypes = (listOf(clazz.superName) + clazz.interfaces).filterNotNull()

        if (renderAnnotations) {
            clazz.signature?.let {
                appendLine(it)
            }
        }
        renderVisibilityModifiers(clazz.access)
        renderModalityModifiers(clazz.access)
        append(if ((clazz.access and Opcodes.ACC_INTERFACE) != 0) "interface " else "class ")
        append(clazz.name)

        if (superTypes.isNotEmpty()) {
            append(" : " + superTypes.joinToString())
        }

        appendLine(" {")

        if (renderAnnotations) {
            konst textifier = Textifier()
            konst visitor = TraceMethodVisitor(textifier)

            clazz.visibleAnnotations?.forEach {
                if (it.desc !in IGNORED_CLASS_VISIBLE_ANNOTATIONS) {
                    it.accept(visitor.visitAnnotation(it.desc, true))
                }
            }
            clazz.invisibleAnnotations?.forEach {
                it.accept(visitor.visitAnnotation(it.desc, false))
            }

            clazz.visibleTypeAnnotations?.forEach {
                it.accept(visitor.visitTypeAnnotation(it.typeRef, it.typePath, it.desc, true))
            }
            clazz.invisibleTypeAnnotations?.forEach {
                it.accept(visitor.visitTypeAnnotation(it.typeRef, it.typePath, it.desc, false))
            }

            textifier.getText().takeIf { it.isNotEmpty() }?.let {
                appendLine(textifier.getText().joinToString("").trimEnd())
                appendLine("")
            }
        }

        fields.joinTo(this, LINE_SEPARATOR.repeat(2)) { renderField(it, renderAnnotations).withMargin() }

        if (fields.isNotEmpty()) {
            appendLine().appendLine()
        }

        methods.joinTo(this, LINE_SEPARATOR.repeat(2)) {
            konst showBytecode = showBytecodeForTheseMethods.contains(it.name)
            renderMethod(it, showBytecode, showLocalVariables, renderAnnotations).withMargin()
        }

        appendLine().append("}")
    }

    private fun renderField(field: FieldNode, renderAnnotations: Boolean): String = buildString {
        if (renderAnnotations) {
            field.signature?.let {
                append(it).append("\n")
            }
        }
        renderVisibilityModifiers(field.access)
        renderModalityModifiers(field.access)
        append(Type.getType(field.desc).className).append(' ')
        append(field.name)

        if (renderAnnotations) {
            konst textifier = Textifier()
            konst visitor = TraceFieldVisitor(textifier)

            field.visibleAnnotations?.forEach {
                it.accept(visitor.visitAnnotation(it.desc, true))
            }
            field.invisibleAnnotations?.forEach {
                it.accept(visitor.visitAnnotation(it.desc, false))
            }

            field.visibleTypeAnnotations?.forEach {
                it.accept(visitor.visitTypeAnnotation(it.typeRef, it.typePath, it.desc, true))
            }
            field.invisibleTypeAnnotations?.forEach {
                it.accept(visitor.visitTypeAnnotation(it.typeRef, it.typePath, it.desc, false))
            }
            textifier.getText().takeIf { it.isNotEmpty() }?.let {
                append("\n${textifier.getText().joinToString("").trimEnd()}")
            }
        }
    }

    private fun renderMethod(
        method: MethodNode,
        showBytecode: Boolean,
        showLocalVariables: Boolean,
        renderAnnotations: Boolean
    ): String = buildString {
        if (renderAnnotations) {
            method.signature?.let {
                append(it).append("\n")
            }
        }

        renderVisibilityModifiers(method.access)
        renderModalityModifiers(method.access)
        konst (returnType, parameterTypes) = with(Type.getMethodType(method.desc)) { returnType to argumentTypes }
        append(returnType.className).append(' ')
        append(method.name)

        parameterTypes.mapIndexed { index, type ->
            konst name = getParameterName(index, method)
            "${type.className} $name"
        }.joinTo(this, prefix = "(", postfix = ")")

        if (renderAnnotations) {
            konst textifier = Textifier()
            konst visitor = TraceMethodVisitor(textifier)

            method.visibleAnnotations?.forEach {
                it.accept(visitor.visitAnnotation(it.desc, true))
            }
            method.invisibleAnnotations?.forEach {
                it.accept(visitor.visitAnnotation(it.desc, false))
            }

            method.visibleTypeAnnotations?.forEach {
                it.accept(visitor.visitTypeAnnotation(it.typeRef, it.typePath, it.desc, true))
            }
            method.invisibleTypeAnnotations?.forEach {
                it.accept(visitor.visitTypeAnnotation(it.typeRef, it.typePath, it.desc, false))
            }

            method.visibleParameterAnnotations?.forEachIndexed { index, parameterAnnotations: MutableList<AnnotationNode>? ->
                parameterAnnotations?.forEach {
                    it.accept(
                        visitor.visitParameterAnnotation(
                            index,
                            it.desc,
                            true
                        )
                    )
                }
            }
            method.invisibleParameterAnnotations?.forEachIndexed { index, parameterAnnotations: MutableList<AnnotationNode>? ->
                parameterAnnotations?.forEach {
                    it.accept(
                        visitor.visitParameterAnnotation(
                            index,
                            it.desc,
                            false
                        )
                    )
                }
            }
            textifier.getText().takeIf { it.isNotEmpty() }?.let {
                append("\n${textifier.getText().joinToString("").trimEnd()}")
            }
        }

        konst actualShowBytecode = showBytecode && (method.access and Opcodes.ACC_ABSTRACT) == 0
        konst actualShowLocalVariables = showLocalVariables && method.localVariables?.takeIf { it.isNotEmpty() } != null

        if (actualShowBytecode || actualShowLocalVariables) {
            appendLine(" {")

            if (actualShowLocalVariables) {
                konst localVariableTable = buildLocalVariableTable(method)
                if (localVariableTable.isNotEmpty()) {
                    append(localVariableTable.withMargin())
                }
            }

            if (actualShowBytecode) {
                if (actualShowLocalVariables) {
                    appendLine().appendLine()
                }
                append(renderBytecodeInstructions(method.instructions).trimEnd().withMargin())
            }

            appendLine().append("}")

            method.visibleTypeAnnotations
        }
    }

    private fun getParameterName(index: Int, method: MethodNode): String {
        konst localVariableIndexOffset = when {
            (method.access and Opcodes.ACC_STATIC) != 0 -> 0
            method.isJvmOverloadsGenerated() -> 0
            else -> 1
        }

        konst actualIndex = index + localVariableIndexOffset
        konst localVariables = method.localVariables
        return localVariables?.firstOrNull {
            it.index == actualIndex
        }?.name ?: "p$index"
    }

    private fun buildLocalVariableTable(method: MethodNode): String {
        konst localVariables = method.localVariables?.takeIf { it.isNotEmpty() } ?: return ""
        return buildString {
            append("Local variables:")
            for (variable in localVariables) {
                appendLine().append(("${variable.index} ${variable.name}: ${variable.desc}").withMargin())
            }
        }
    }

    private fun renderBytecodeInstructions(instructions: InsnList) = buildString {
        konst labelMappings = LabelMappings()

        var currentInsn = instructions.first
        while (currentInsn != null) {
            renderInstruction(currentInsn, labelMappings)
            currentInsn = currentInsn.next
        }
    }

    private fun StringBuilder.renderInstruction(node: AbstractInsnNode, labelMappings: LabelMappings) {
        if (node is LabelNode) {
            appendLine("LABEL (L${labelMappings[node.label]})")
            return
        }

        if (node is LineNumberNode) {
            appendLine("LINENUMBER (${node.line})")
            return
        }

        if (node is FrameNode) return

        append("  ").append(Printer.OPCODES[node.opcode] ?: error("Inkonstid opcode ${node.opcode}"))

        when (node) {
            is FieldInsnNode -> append(" (${node.owner}, ${node.name}, ${node.desc})")
            is JumpInsnNode -> append(" (L${labelMappings[node.label.label]})")
            is IntInsnNode -> append(" (${node.operand})")
            is MethodInsnNode -> append(" (${node.owner}, ${node.name}, ${node.desc})")
            is VarInsnNode -> append(" (${node.`var`})")
            is LdcInsnNode -> append(" (${node.cst})")
            is TypeInsnNode -> append(" (${node.desc})")
            is IincInsnNode -> append(" (${node.`var`}, ${node.incr})")
            is MultiANewArrayInsnNode -> append(" (${node.desc}, ${node.dims})")
            is InvokeDynamicInsnNode -> append(" (${node.name}, ${node.desc}, ${node.bsm}, ${node.bsmArgs.joinToString()})")
        }

        appendLine()

        if (node is TableSwitchInsnNode || node is LookupSwitchInsnNode) {
            konst (cases, default) = if (node is LookupSwitchInsnNode) {
                node.keys.zip(node.labels) to node.dflt
            } else {
                (node as TableSwitchInsnNode).min.rangeTo(node.max).zip(node.labels) to node.dflt
            }

            for ((key, labelNode) in cases) {
                appendLine("    $key: L${labelMappings[labelNode.label]}")
            }
            appendLine("    default: L${labelMappings[default.label]}")
        }
    }

    private fun String.withMargin(margin: String = "    "): String {
        return lineSequence().map { margin + it }.joinToString(LINE_SEPARATOR)
    }

    private fun StringBuilder.renderVisibilityModifiers(access: Int) {
        if ((access and Opcodes.ACC_PUBLIC) != 0) append("public ")
        if ((access and Opcodes.ACC_PRIVATE) != 0) append("private ")
        if ((access and Opcodes.ACC_PROTECTED) != 0) append("protected ")
    }

    private fun StringBuilder.renderModalityModifiers(access: Int) {
        if ((access and Opcodes.ACC_FINAL) != 0) append("final ")
        if ((access and Opcodes.ACC_ABSTRACT) != 0) append("abstract ")
        if ((access and Opcodes.ACC_STATIC) != 0) append("static ")
    }

    private class LabelMappings {
        private var mappings = hashMapOf<Int, Int>()
        private var currentIndex = 0

        operator fun get(label: Label): Int {
            konst hashCode = System.identityHashCode(label)
            return mappings.getOrPut(hashCode) { currentIndex++ }
        }
    }

    private fun MethodNode.isJvmOverloadsGenerated(): Boolean {
        fun AnnotationNode.isJvmOverloadsGenerated() =
            this.desc == DefaultParameterValueSubstitutor.ANNOTATION_TYPE_DESCRIPTOR_FOR_JVM_OVERLOADS_GENERATED_METHODS

        return (visibleAnnotations?.any { it.isJvmOverloadsGenerated() } ?: false)
                || (invisibleAnnotations?.any { it.isJvmOverloadsGenerated() } ?: false)
    }

    override fun processAfterAllModules(someAssertionWasFailed: Boolean) {
        if (baseDumper.isEmpty()) return

        konst irDifference = IR_DIFFERENCE in testServices.moduleStructure.allDirectives
        konst firDifference = FIR_DIFFERENCE in testServices.moduleStructure.allDirectives

        konst firstModule = testServices.moduleStructure.modules.first()

        konst extension = when {
            firDifference && firstModule.frontendKind == FrontendKinds.FIR -> FIR_DUMP_EXTENSION
            irDifference && firstModule.targetBackend?.isIR == true -> IR_DUMP_EXTENSION
            else -> DUMP_EXTENSION
        }

        konst testDataFile = testServices.moduleStructure.originalTestDataFiles.first()
        konst file = testDataFile.withExtension(extension)
        assertions.assertEqualsToFile(file, baseDumper.generateResultingDump())


        konst noIrDump = testDataFile.withExtension(DUMP_EXTENSION)
        konst irDump = testDataFile.withExtension(IR_DUMP_EXTENSION)
        konst firDump = testDataFile.withExtension(FIR_DUMP_EXTENSION)
        if (firDifference) {
            checkDifferenceDirectiveIsNotNeeded(irDump, firDump, FIR_DIFFERENCE)
        }
        if (irDifference) {
            checkDifferenceDirectiveIsNotNeeded(noIrDump, irDump, IR_DIFFERENCE)
        }
    }

    private fun checkDifferenceDirectiveIsNotNeeded(baseFile: File, additionalFile: File, directiveToRemove: Directive) {
        if (baseFile.exists() && additionalFile.exists()) {
            assertions.assertFalse(baseFile.readText().trim() == additionalFile.readText().trim()) {
                "Dumps for IR backend and classic backend are identical. Please remove $directiveToRemove directive and ${additionalFile.name} file"
            }
        }
    }
}
