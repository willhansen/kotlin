/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.load.kotlin

import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.load.kotlin.header.KotlinClassHeader
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.constants.ClassLiteralValue

interface KotlinJvmBinaryClass {
    konst classId: ClassId

    /**
     * @return path to the class file (to be reported to the user upon error)
     */
    konst location: String

    konst containingLibrary: String?
        get() = null

    fun loadClassAnnotations(visitor: AnnotationVisitor, cachedContents: ByteArray?)

    fun visitMembers(visitor: MemberVisitor, cachedContents: ByteArray?)

    konst classHeader: KotlinClassHeader

    interface MemberVisitor {
        // TODO: abstract signatures for methods and fields instead of ASM 'desc' strings?

        fun visitMethod(name: Name, desc: String): MethodAnnotationVisitor?

        fun visitField(name: Name, desc: String, initializer: Any?): AnnotationVisitor?
    }

    interface AnnotationVisitor {
        fun visitAnnotation(classId: ClassId, source: SourceElement): AnnotationArgumentVisitor?

        fun visitEnd()
    }

    interface MethodAnnotationVisitor : AnnotationVisitor {
        fun visitParameterAnnotation(index: Int, classId: ClassId, source: SourceElement): AnnotationArgumentVisitor?

        fun visitAnnotationMemberDefaultValue(): AnnotationArgumentVisitor?
    }

    interface AnnotationArgumentVisitor {
        fun visit(name: Name?, konstue: Any?)

        fun visitClassLiteral(name: Name?, konstue: ClassLiteralValue)

        fun visitEnum(name: Name?, enumClassId: ClassId, enumEntryName: Name)

        fun visitAnnotation(name: Name?, classId: ClassId): AnnotationArgumentVisitor?

        fun visitArray(name: Name?): AnnotationArrayArgumentVisitor?

        fun visitEnd()
    }

    interface AnnotationArrayArgumentVisitor {
        fun visit(konstue: Any?)

        fun visitEnum(enumClassId: ClassId, enumEntryName: Name)

        fun visitClassLiteral(konstue: ClassLiteralValue)

        fun visitAnnotation(classId: ClassId): AnnotationArgumentVisitor?

        fun visitEnd()
    }
}
