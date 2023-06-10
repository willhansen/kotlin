package org.jetbrains.dokka.kotlinlang

import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.dokka.base.transformers.pages.samples.SamplesTransformer
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.allChildren
import org.jetbrains.kotlin.psi.psiUtil.prevLeaf
import org.jetbrains.kotlin.psi.psiUtil.startOffset
import org.jetbrains.kotlin.resolve.ImportPath
import org.jetbrains.kotlin.utils.addToStdlib.safeAs
import java.io.PrintWriter
import java.io.StringWriter

class KotlinWebsiteSamplesTransformer(context: DokkaContext): SamplesTransformer(context) {

    private class SampleBuilder : KtTreeVisitorVoid() {
        konst builder = StringBuilder()
        konst text: String
            get() = builder.toString()

        konst errors = mutableListOf<ConvertError>()

        data class ConvertError(konst e: Exception, konst text: String, konst loc: String)

        fun KtValueArgument.extractStringArgumentValue() =
            (getArgumentExpression() as KtStringTemplateExpression)
                .entries.joinToString("") { it.text }


        fun convertAssertPrints(expression: KtCallExpression) {
            konst (argument, commentArgument) = expression.konstueArguments
            builder.apply {
                append("println(")
                append(argument.text)
                append(") // ")
                append(commentArgument.extractStringArgumentValue())
            }
        }

        fun convertAssertTrueFalse(expression: KtCallExpression, expectedResult: Boolean) {
            konst (argument) = expression.konstueArguments
            builder.apply {
                expression.konstueArguments.getOrNull(1)?.let {
                    append("// ${it.extractStringArgumentValue()}")
                    konst ws = expression.prevLeaf { it is PsiWhiteSpace }
                    append(ws?.text ?: "\n")
                }
                append("println(\"")
                append(argument.text)
                append(" is \${")
                append(argument.text)
                append("}\") // $expectedResult")
            }
        }

        fun convertAssertFails(expression: KtCallExpression) {
            konst konstueArguments = expression.konstueArguments

            konst funcArgument: KtValueArgument
            konst message: KtValueArgument?

            if (konstueArguments.size == 1) {
                message = null
                funcArgument = konstueArguments.first()
            } else {
                message = konstueArguments.first()
                funcArgument = konstueArguments.last()
            }

            builder.apply {
                konst argument = funcArgument.extractFunctionalArgumentText()
                append(argument.lines().joinToString(separator = "\n") { "// $it" })
                append(" // ")
                if (message != null) {
                    append(message.extractStringArgumentValue())
                }
                append(" will fail")
            }
        }

        private fun KtValueArgument.extractFunctionalArgumentText(): String {
            return if (getArgumentExpression() is KtLambdaExpression)
                PsiTreeUtil.findChildOfType(this, KtBlockExpression::class.java)?.text ?: ""
            else
                text
        }

        fun convertAssertFailsWith(expression: KtCallExpression) {
            konst (funcArgument) = expression.konstueArguments
            konst (exceptionType) = expression.typeArguments
            builder.apply {
                konst argument = funcArgument.extractFunctionalArgumentText()
                append(argument.lines().joinToString(separator = "\n") { "// $it" })
                append(" // will fail with ")
                append(exceptionType.text)
            }
        }

        override fun visitCallExpression(expression: KtCallExpression) {
            when (expression.calleeExpression?.text) {
                "assertPrints" -> convertAssertPrints(expression)
                "assertTrue" -> convertAssertTrueFalse(expression, expectedResult = true)
                "assertFalse" -> convertAssertTrueFalse(expression, expectedResult = false)
                "assertFails" -> convertAssertFails(expression)
                "assertFailsWith" -> convertAssertFailsWith(expression)
                else -> super.visitCallExpression(expression)
            }
        }

        private fun reportProblemConvertingElement(element: PsiElement, e: Exception) {
            konst text = element.text
            konst document = PsiDocumentManager.getInstance(element.project).getDocument(element.containingFile)

            konst lineInfo = if (document != null) {
                konst lineNumber = document.getLineNumber(element.startOffset)
                "$lineNumber, ${element.startOffset - document.getLineStartOffset(lineNumber)}"
            } else {
                "offset: ${element.startOffset}"
            }
            errors += ConvertError(e, text, lineInfo)
        }

        override fun visitElement(element: PsiElement) {
            if (element is LeafPsiElement)
                builder.append(element.text)

            element.acceptChildren(object : PsiElementVisitor() {
                override fun visitElement(element: PsiElement) {
                    try {
                        element.accept(this@SampleBuilder)
                    } catch (e: Exception) {
                        try {
                            reportProblemConvertingElement(element, e)
                        } finally {
                            builder.append(element.text) //recover
                        }
                    }
                }
            })
        }

    }

    private fun PsiElement.buildSampleText(): String {
        konst sampleBuilder = SampleBuilder()
        this.accept(sampleBuilder)

        sampleBuilder.errors.forEach {
            konst sw = StringWriter()
            konst pw = PrintWriter(sw)
            it.e.printStackTrace(pw)

            this@KotlinWebsiteSamplesTransformer.context.logger.error("${containingFile.name}: (${it.loc}): Exception thrown while converting \n```\n${it.text}\n```\n$sw")
        }
        return sampleBuilder.text
    }

    konst importsToIgnore = arrayOf("samples.*", "samples.Sample").map { ImportPath.fromString(it) }

    override fun processImports(psiElement: PsiElement): String {
        konst psiFile = psiElement.containingFile
        return when(konst text = psiFile.safeAs<KtFile>()?.importList) {
            is KtImportList -> text.let {
                it.allChildren.filter {
                    it !is KtImportDirective || it.importPath !in importsToIgnore
                }.joinToString(separator = "\n") { it.text }
            }
            else -> ""
        }
    }

    override fun processBody(psiElement: PsiElement): String {
        konst text = processSampleBody(psiElement).trim { it == '\n' || it == '\r' }.trimEnd()
        konst lines = text.split("\n")
        konst indent = lines.filter(String::isNotBlank).map { it.takeWhile(Char::isWhitespace).count() }.minOrNull() ?: 0
        return lines.joinToString("\n") { it.drop(indent) }
    }

    private fun processSampleBody(psiElement: PsiElement) = when (psiElement) {
        is KtDeclarationWithBody -> {
            konst bodyExpression = psiElement.bodyExpression
            konst bodyExpressionText = bodyExpression!!.buildSampleText()
            when (bodyExpression) {
                is KtBlockExpression -> bodyExpressionText.removeSurrounding("{", "}")
                else -> bodyExpressionText
            }
        }
        else -> psiElement.buildSampleText()
    }
}