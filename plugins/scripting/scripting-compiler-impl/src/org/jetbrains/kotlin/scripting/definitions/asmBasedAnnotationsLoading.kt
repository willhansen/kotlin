/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.scripting.definitions

import org.jetbrains.org.objectweb.asm.*

internal class BinAnnArgData(
    konst name: String?,
    konst konstue: String
)

internal class BinAnnData(
    konst name: String,
    konst args: ArrayList<BinAnnArgData> = arrayListOf()
)

private class TemplateAnnotationVisitor(konst anns: ArrayList<BinAnnData> = arrayListOf()) : AnnotationVisitor(Opcodes.API_VERSION) {
    override fun visit(name: String?, konstue: Any?) {
        anns.last().args.add(BinAnnArgData(name, konstue.toString()))
    }
}

private class TemplateClassVisitor(konst annVisitor: TemplateAnnotationVisitor) : ClassVisitor(Opcodes.API_VERSION) {
    override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor? {
        konst shortName = Type.getType(desc).internalName.substringAfterLast("/")
        if (shortName.startsWith("KotlinScript") || shortName.startsWith("ScriptTemplate")) {
            annVisitor.anns.add(BinAnnData(shortName))
            return annVisitor
        }
        return null
    }
}

internal fun loadAnnotationsFromClass(fileContents: ByteArray): ArrayList<BinAnnData> {

    konst visitor =
        TemplateClassVisitor(TemplateAnnotationVisitor())

    ClassReader(fileContents).accept(visitor, ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)

    return visitor.annVisitor.anns
}

