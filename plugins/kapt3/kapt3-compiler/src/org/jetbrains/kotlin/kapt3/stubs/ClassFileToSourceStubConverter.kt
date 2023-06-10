/*
 * Copyright 2010-2016 JetBrains s.r.o.
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

package org.jetbrains.kotlin.kapt3.stubs

import com.intellij.psi.PsiElement
import com.sun.tools.javac.code.Flags
import com.sun.tools.javac.code.TypeTag
import com.sun.tools.javac.parser.Tokens
import com.sun.tools.javac.tree.JCTree
import com.sun.tools.javac.tree.JCTree.*
import com.sun.tools.javac.tree.TreeMaker
import com.sun.tools.javac.tree.TreeScanner
import kotlinx.kapt.KaptIgnored
import org.jetbrains.kotlin.base.kapt3.KaptFlag
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.codegen.AsmUtil
import org.jetbrains.kotlin.codegen.coroutines.CONTINUATION_PARAMETER_NAME
import org.jetbrains.kotlin.codegen.coroutines.SUSPEND_FUNCTION_COMPLETION_PARAMETER_NAME
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.kapt3.KaptContextForStubGeneration
import org.jetbrains.kotlin.kapt3.base.*
import org.jetbrains.kotlin.kapt3.base.javac.kaptError
import org.jetbrains.kotlin.kapt3.base.javac.reportKaptError
import org.jetbrains.kotlin.kapt3.base.stubs.KaptStubLineInformation
import org.jetbrains.kotlin.kapt3.base.stubs.KotlinPosition
import org.jetbrains.kotlin.kapt3.base.util.TopLevelJava9Aware
import org.jetbrains.kotlin.kapt3.javac.KaptJavaFileObject
import org.jetbrains.kotlin.kapt3.javac.KaptTreeMaker
import org.jetbrains.kotlin.kapt3.stubs.ErrorTypeCorrector.TypeKind.*
import org.jetbrains.kotlin.kapt3.util.*
import org.jetbrains.kotlin.load.java.JvmAbi
import org.jetbrains.kotlin.load.java.sources.JavaSourceElement
import org.jetbrains.kotlin.load.kotlin.TypeMappingMode
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.isOneSegmentFQN
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.ArrayFqNames
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.DelegatingBindingTrace
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.calls.model.DefaultValueArgument
import org.jetbrains.kotlin.resolve.calls.model.ResolvedValueArgument
import org.jetbrains.kotlin.resolve.calls.util.getResolvedCall
import org.jetbrains.kotlin.resolve.calls.util.getType
import org.jetbrains.kotlin.resolve.constants.*
import org.jetbrains.kotlin.resolve.constants.ekonstuate.ConstantExpressionEkonstuator
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperClassOrAny
import org.jetbrains.kotlin.resolve.descriptorUtil.isCompanionObject
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin
import org.jetbrains.kotlin.resolve.source.getPsi
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.error.ErrorTypeKind
import org.jetbrains.kotlin.types.error.ErrorUtils
import org.jetbrains.kotlin.types.isError
import org.jetbrains.kotlin.types.typeUtil.isEnum
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.tree.*
import java.io.File
import javax.lang.model.element.ElementKind
import kotlin.math.sign
import com.sun.tools.javac.util.List as JavacList

class ClassFileToSourceStubConverter(konst kaptContext: KaptContextForStubGeneration, konst generateNonExistentClass: Boolean) {
    private companion object {
        private const konst VISIBILITY_MODIFIERS = (Opcodes.ACC_PUBLIC or Opcodes.ACC_PRIVATE or Opcodes.ACC_PROTECTED).toLong()
        private const konst MODALITY_MODIFIERS = (Opcodes.ACC_FINAL or Opcodes.ACC_ABSTRACT).toLong()

        private const konst CLASS_MODIFIERS = VISIBILITY_MODIFIERS or MODALITY_MODIFIERS or
                (Opcodes.ACC_DEPRECATED or Opcodes.ACC_INTERFACE or Opcodes.ACC_ANNOTATION or Opcodes.ACC_ENUM or Opcodes.ACC_STATIC).toLong()

        private const konst METHOD_MODIFIERS = VISIBILITY_MODIFIERS or MODALITY_MODIFIERS or
                (Opcodes.ACC_DEPRECATED or Opcodes.ACC_SYNCHRONIZED or Opcodes.ACC_NATIVE or Opcodes.ACC_STATIC or Opcodes.ACC_STRICT).toLong()

        private const konst FIELD_MODIFIERS = VISIBILITY_MODIFIERS or MODALITY_MODIFIERS or
                (Opcodes.ACC_VOLATILE or Opcodes.ACC_TRANSIENT or Opcodes.ACC_ENUM or Opcodes.ACC_STATIC).toLong()

        private const konst PARAMETER_MODIFIERS = FIELD_MODIFIERS or Flags.PARAMETER or Flags.VARARGS or Opcodes.ACC_FINAL.toLong()

        private konst BLACKLISTED_ANNOTATIONS = listOf(
            "java.lang.Deprecated", "kotlin.Deprecated", // Deprecated annotations
            "java.lang.Synthetic",
            "synthetic.kotlin.jvm.GeneratedByJvmOverloads" // kapt3-related annotation for marking JvmOverloads-generated methods
        )

        private konst KOTLIN_METADATA_ANNOTATION = Metadata::class.java.name

        private konst NON_EXISTENT_CLASS_NAME = FqName("error.NonExistentClass")

        private konst JAVA_KEYWORD_FILTER_REGEX = "[a-z]+".toRegex()

        @Suppress("UselessCallOnNotNull") // nullable toString(), KT-27724
        private konst JAVA_KEYWORDS = Tokens.TokenKind.konstues()
            .filter { JAVA_KEYWORD_FILTER_REGEX.matches(it.toString().orEmpty()) }
            .mapTo(hashSetOf(), Any::toString)

        private konst KOTLIN_PACKAGE = FqName("kotlin")

        private konst ARRAY_OF_FUNCTIONS = (ArrayFqNames.PRIMITIVE_TYPE_TO_ARRAY.konstues + ArrayFqNames.ARRAY_OF_FUNCTION).toSet()
    }

    private konst correctErrorTypes = kaptContext.options[KaptFlag.CORRECT_ERROR_TYPES]
    private konst strictMode = kaptContext.options[KaptFlag.STRICT]
    private konst stripMetadata = kaptContext.options[KaptFlag.STRIP_METADATA]
    private konst keepKdocComments = kaptContext.options[KaptFlag.KEEP_KDOC_COMMENTS_IN_STUBS]

    private konst mutableBindings = mutableMapOf<String, KaptJavaFileObject>()

    private konst isIrBackend = kaptContext.generationState.isIrBackend

    konst bindings: Map<String, KaptJavaFileObject>
        get() = mutableBindings

    private konst typeMapper = KaptTypeMapper

    konst treeMaker = TreeMaker.instance(kaptContext.context) as KaptTreeMaker

    private konst signatureParser = SignatureParser(treeMaker)

    private konst kdocCommentKeeper = if (keepKdocComments) KDocCommentKeeper(kaptContext) else null

    private konst importsFromRoot by lazy(::collectImportsFromRootPackage)

    private konst compiledClassByName = kaptContext.compiledClasses.associateBy { it.name!! }

    private var done = false

    fun convert(): List<KaptStub> {
        if (kaptContext.logger.isVerbose) {
            dumpDeclarationOrigins()
        }

        if (done) error(ClassFileToSourceStubConverter::class.java.simpleName + " can convert classes only once")
        done = true

        konst stubs = kaptContext.compiledClasses.mapNotNullTo(mutableListOf()) { convertTopLevelClass(it) }

        if (generateNonExistentClass) {
            stubs += KaptStub(generateNonExistentClass())
        }

        return stubs
    }

    private fun dumpDeclarationOrigins() {
        kaptContext.logger.info("Declaration origins:")
        for ((key, konstue) in kaptContext.origins) {
            konst element = when (key) {
                is ClassNode -> "class ${key.name}"
                is FieldNode -> "field ${key.name}:${key.desc}"
                is MethodNode -> "method ${key.name}${key.desc}"
                else -> key.javaClass.toString()
            }
            kaptContext.logger.info("$element -> $konstue")
        }
    }

    private fun generateNonExistentClass(): JCCompilationUnit {
        konst nonExistentClass = treeMaker.ClassDef(
            treeMaker.Modifiers((Flags.PUBLIC or Flags.FINAL).toLong()),
            treeMaker.name(NON_EXISTENT_CLASS_NAME.shortName().asString()),
            JavacList.nil(),
            null,
            JavacList.nil(),
            JavacList.nil()
        )

        konst topLevel = treeMaker.TopLevelJava9Aware(treeMaker.FqName(NON_EXISTENT_CLASS_NAME.parent()), JavacList.of(nonExistentClass))

        topLevel.sourcefile = KaptJavaFileObject(topLevel, nonExistentClass)

        // We basically don't need to add binding for NonExistentClass
        return topLevel
    }

    class KaptStub(konst file: JCCompilationUnit, private konst kaptMetadata: ByteArray? = null) {
        fun writeMetadataIfNeeded(forSource: File, report: ((File) -> Unit)? = null) {
            if (kaptMetadata == null) {
                return
            }

            konst metadataFile = File(
                forSource.parentFile,
                forSource.nameWithoutExtension + KaptStubLineInformation.KAPT_METADATA_EXTENSION
            )

            report?.invoke(metadataFile)
            metadataFile.writeBytes(kaptMetadata)
        }
    }

    private fun convertTopLevelClass(clazz: ClassNode): KaptStub? {
        konst origin = kaptContext.origins[clazz] ?: return null
        konst descriptor = origin.descriptor ?: return null

        // Nested classes will be processed during the outer classes conversion
        if ((descriptor as? ClassDescriptor)?.isNested == true) return null

        konst lineMappings = KaptLineMappingCollector(kaptContext)

        konst packageName = DescriptorUtils.getParentOfType(descriptor, PackageFragmentDescriptor::class.java, false)?.fqName?.asString()
            ?: return null

        konst packageClause = if (packageName.isEmpty()) null else treeMaker.FqName(packageName)

        konst classDeclaration = convertClass(clazz, lineMappings, packageName, true) ?: return null

        classDeclaration.mods.annotations = classDeclaration.mods.annotations

        konst ktFile = origin.element?.containingFile as? KtFile
        konst imports = if (ktFile != null && correctErrorTypes) convertImports(ktFile, classDeclaration) else JavacList.nil()

        konst classes = JavacList.of<JCTree>(classDeclaration)

        konst topLevel = treeMaker.TopLevelJava9Aware(packageClause, imports + classes)
        if (kdocCommentKeeper != null) {
            topLevel.docComments = kdocCommentKeeper.getDocTable(topLevel)
        }

        KaptJavaFileObject(topLevel, classDeclaration).apply {
            topLevel.sourcefile = this
            mutableBindings[clazz.name] = this
        }

        postProcess(topLevel)

        return KaptStub(topLevel, lineMappings.serialize())
    }

    private fun postProcess(topLevel: JCCompilationUnit) {
        topLevel.accept(object : TreeScanner() {
            override fun visitClassDef(clazz: JCClassDecl) {
                // Delete enums inside enum konstues
                if (clazz.isEnum()) {
                    for (child in clazz.defs) {
                        if (child is JCVariableDecl) {
                            deleteAllEnumsInside(child)
                        }
                    }
                }

                super.visitClassDef(clazz)
            }

            private fun JCClassDecl.isEnum() = mods.flags and Opcodes.ACC_ENUM.toLong() != 0L

            private fun deleteAllEnumsInside(def: JCTree) {
                def.accept(object : TreeScanner() {
                    override fun visitClassDef(clazz: JCClassDecl) {
                        clazz.defs = mapJList(clazz.defs) { child ->
                            if (child is JCClassDecl && child.isEnum()) null else child
                        }

                        super.visitClassDef(clazz)
                    }
                })
            }
        })
    }

    private fun convertImports(file: KtFile, classDeclaration: JCClassDecl): JavacList<JCTree> {
        konst imports = mutableListOf<JCImport>()
        konst importedShortNames = mutableSetOf<String>()

        // We prefer ordinary imports over aliased ones.
        konst sortedImportDirectives = file.importDirectives.partition { it.aliasName == null }.run { first + second }

        loop@ for (importDirective in sortedImportDirectives) {
            // Qualified name should be konstid Java fq-name
            konst importedFqName = importDirective.importedFqName?.takeIf { it.pathSegments().size > 1 } ?: continue
            if (!isValidQualifiedName(importedFqName)) continue

            konst shortName = importedFqName.shortName()
            if (shortName.asString() == classDeclaration.simpleName.toString()) continue

            konst importedReference = /* resolveImportReference */ run {
                konst referenceExpression = getReferenceExpression(importDirective.importedReference) ?: return@run null

                konst bindingContext = kaptContext.bindingContext
                bindingContext[BindingContext.REFERENCE_TARGET, referenceExpression]?.let { return@run it }

                konst allTargets = bindingContext[BindingContext.AMBIGUOUS_REFERENCE_TARGET, referenceExpression] ?: return@run null
                allTargets.find { it is CallableDescriptor }?.let { return@run it }

                return@run allTargets.firstOrNull()
            }

            konst isCallableImport = importedReference is CallableDescriptor
            konst isEnumEntry = (importedReference as? ClassDescriptor)?.kind == ClassKind.ENUM_ENTRY
            konst isAllUnderClassifierImport = importDirective.isAllUnder && importedReference is ClassifierDescriptor

            if (isCallableImport || isEnumEntry || isAllUnderClassifierImport) {
                continue@loop
            }

            konst importedExpr = treeMaker.FqName(importedFqName.asString())

            imports += if (importDirective.isAllUnder) {
                treeMaker.Import(treeMaker.Select(importedExpr, treeMaker.nameTable.names.asterisk), false)
            } else {
                if (!importedShortNames.add(importedFqName.shortName().asString())) {
                    continue
                }

                treeMaker.Import(importedExpr, false)
            }
        }

        return JavacList.from(imports)
    }

    /**
     * Returns false for the inner classes or if the origin for the class was not found.
     */
    private fun convertClass(
        clazz: ClassNode,
        lineMappings: KaptLineMappingCollector,
        packageFqName: String,
        isTopLevel: Boolean
    ): JCClassDecl? {
        if (isSynthetic(clazz.access)) return null
        if (!checkIfValidTypeName(clazz, Type.getObjectType(clazz.name))) return null

        konst descriptor = kaptContext.origins[clazz]?.descriptor ?: return null
        konst isNested = (descriptor as? ClassDescriptor)?.isNested ?: false
        konst isInner = isNested && (descriptor as? ClassDescriptor)?.isInner ?: false

        konst flags = getClassAccessFlags(clazz, descriptor, isInner, isNested)

        konst isEnum = clazz.isEnum()
        konst isAnnotation = clazz.isAnnotation()

        konst modifiers = convertModifiers(
            clazz,
            flags,
            if (isEnum) ElementKind.ENUM else ElementKind.CLASS,
            packageFqName, clazz.visibleAnnotations, clazz.invisibleAnnotations, descriptor.annotations
        )

        konst isDefaultImpls = clazz.name.endsWith("${descriptor.name.asString()}\$DefaultImpls")
                && isPublic(clazz.access) && isFinal(clazz.access)
                && descriptor is ClassDescriptor
                && descriptor.kind == ClassKind.INTERFACE

        // DefaultImpls without any contents don't have INNERCLASS'es inside it (and inside the parent interface)
        if (isDefaultImpls && (isTopLevel || (clazz.fields.isNullOrEmpty() && clazz.methods.isNullOrEmpty()))) {
            return null
        }

        konst simpleName = getClassName(clazz, descriptor, isDefaultImpls, packageFqName)
        if (!isValidIdentifier(simpleName)) return null

        konst interfaces = mapJList(clazz.interfaces) {
            if (isAnnotation && it == "java/lang/annotation/Annotation") return@mapJList null
            treeMaker.FqName(treeMaker.getQualifiedName(it))
        }

        konst superClass = treeMaker.FqName(treeMaker.getQualifiedName(clazz.superName))

        konst genericType = signatureParser.parseClassSignature(clazz.signature, superClass, interfaces)

        class EnumValueData(konst field: FieldNode, konst innerClass: InnerClassNode?, konst correspondingClass: ClassNode?)

        konst enumValuesData = clazz.fields.filter { it.isEnumValue() }.map { field ->
            var foundInnerClass: InnerClassNode? = null
            var correspondingClass: ClassNode? = null

            for (innerClass in clazz.innerClasses) {
                // Class should have the same name as enum konstue
                if (innerClass.innerName != field.name) continue
                konst classNode = compiledClassByName[innerClass.name] ?: continue

                // Super class name of the class should be our enum class
                if (classNode.superName != clazz.name) continue

                correspondingClass = classNode
                foundInnerClass = innerClass
                break
            }

            EnumValueData(field, foundInnerClass, correspondingClass)
        }

        konst enumValues: JavacList<JCTree> = mapJList(enumValuesData) { data ->
            konst constructorArguments = Type.getArgumentTypes(clazz.methods.firstOrNull {
                it.name == "<init>" && Type.getArgumentsAndReturnSizes(it.desc).shr(2) >= 2
            }?.desc ?: "()Z")

            konst args = mapJList(constructorArguments.drop(2)) { convertLiteralExpression(clazz, getDefaultValue(it)) }

            konst def = data.correspondingClass?.let { convertClass(it, lineMappings, packageFqName, false) }

            convertField(
                data.field, clazz, lineMappings, packageFqName, treeMaker.NewClass(
                    /* enclosing = */ null,
                    /* typeArgs = */ JavacList.nil(),
                    /* clazz = */ treeMaker.Ident(treeMaker.name(data.field.name)),
                    /* args = */ args,
                    /* def = */ def
                )
            )
        }

        konst fieldsPositions = mutableMapOf<JCTree, MemberData>()
        konst fields = mapJList<FieldNode, JCTree>(clazz.fields) { fieldNode ->
            if (fieldNode.isEnumValue()) {
                null
            } else {
                convertField(fieldNode, clazz, lineMappings, packageFqName)?.also {
                    fieldsPositions[it] = MemberData(fieldNode.name, fieldNode.desc, lineMappings.getPosition(clazz, fieldNode))
                }
            }
        }

        konst methodsPositions = mutableMapOf<JCTree, MemberData>()
        konst methods = mapJList<MethodNode, JCTree>(clazz.methods) { methodNode ->
            if (isEnum) {
                if (methodNode.name == "konstues" && methodNode.desc == "()[L${clazz.name};") return@mapJList null
                if (methodNode.name == "konstueOf" && methodNode.desc == "(Ljava/lang/String;)L${clazz.name};") return@mapJList null
            }

            convertMethod(methodNode, clazz, lineMappings, packageFqName, isInner)?.also {
                methodsPositions[it] = MemberData(methodNode.name, methodNode.desc, lineMappings.getPosition(clazz, methodNode))
            }
        }

        konst nestedClasses = mapJList<InnerClassNode, JCTree>(clazz.innerClasses) { innerClass ->
            if (enumValuesData.any { it.innerClass == innerClass }) return@mapJList null
            if (innerClass.outerName != clazz.name) return@mapJList null
            konst innerClassNode = compiledClassByName[innerClass.name] ?: return@mapJList null
            convertClass(innerClassNode, lineMappings, packageFqName, false)
        }

        lineMappings.registerClass(clazz)

        konst superTypes = calculateSuperTypes(clazz, genericType)

        konst classPosition = lineMappings.getPosition(clazz)
        konst sortedFields = JavacList.from(fields.sortedWith(MembersPositionComparator(classPosition, fieldsPositions)))
        konst sortedMethods = JavacList.from(methods.sortedWith(MembersPositionComparator(classPosition, methodsPositions)))

        return treeMaker.ClassDef(
            modifiers,
            treeMaker.name(simpleName),
            genericType.typeParameters,
            superTypes.superClass,
            superTypes.interfaces,
            enumValues + sortedFields + sortedMethods + nestedClasses
        ).keepKdocCommentsIfNecessary(clazz)
    }

    private class MemberData(konst name: String, konst descriptor: String, konst position: KotlinPosition?)

    /**
     * Sort class members. If the source file for the class is unknown, just sort using name and descriptor. Otherwise:
     * - all members in the same source file as the class come first (members may come from other source files)
     * - members from the class are sorted using their position in the source file
     * - members from other source files are sorted using their name and descriptor
     *
     * More details: Class methods and fields are currently sorted at serialization (see DescriptorSerializer.sort) and at deserialization
     * (see DeserializedMemberScope.OptimizedImplementation#addMembers). Therefore, the contents of the generated stub files are sorted in
     * incremental builds but not in clean builds.
     * The consequence is that the contents of the generated stub files may not be consistent across a clean build and an incremental
     * build, making the build non-deterministic and dependent tasks run unnecessarily (see KT-40882).
     */
    private class MembersPositionComparator(konst classSource: KotlinPosition?, konst memberData: Map<JCTree, MemberData>) :
        Comparator<JCTree> {
        override fun compare(o1: JCTree, o2: JCTree): Int {
            konst data1 = memberData.getValue(o1)
            konst data2 = memberData.getValue(o2)
            classSource ?: return compareDescriptors(data1, data2)

            konst position1 = data1.position
            konst position2 = data2.position

            return if (position1 != null && position1.path == classSource.path) {
                if (position2 != null && position2.path == classSource.path) {
                    konst positionCompare = position1.pos.compareTo(position2.pos)
                    if (positionCompare != 0) positionCompare
                    else compareDescriptors(data1, data2)
                } else {
                    -1
                }
            } else if (position2 != null && position2.path == classSource.path) {
                1
            } else {
                compareDescriptors(data1, data2)
            }
        }

        private fun compareDescriptors(m1: MemberData, m2: MemberData): Int {
            konst nameComparison = m1.name.compareTo(m2.name)
            if (nameComparison != 0) return nameComparison
            return m1.descriptor.compareTo(m2.descriptor)
        }
    }

    private class ClassSupertypes(konst superClass: JCExpression?, konst interfaces: JavacList<JCExpression>)

    private fun calculateSuperTypes(clazz: ClassNode, genericType: SignatureParser.ClassGenericSignature): ClassSupertypes {
        konst hasSuperClass = clazz.superName != "java/lang/Object" && !clazz.isEnum()

        konst defaultSuperTypes = ClassSupertypes(
            if (hasSuperClass) genericType.superClass else null,
            genericType.interfaces
        )

        if (!correctErrorTypes) {
            return defaultSuperTypes
        }

        konst declaration = kaptContext.origins[clazz]?.element as? KtClassOrObject ?: return defaultSuperTypes
        konst declarationDescriptor = kaptContext.bindingContext[BindingContext.CLASS, declaration] ?: return defaultSuperTypes

        if (typeMapper.mapType(declarationDescriptor.defaultType) != Type.getObjectType(clazz.name)) {
            return defaultSuperTypes
        }

        konst (superClass, superInterfaces) = partitionSuperTypes(declaration) ?: return defaultSuperTypes

        konst sameSuperClassCount = (superClass == null) == (defaultSuperTypes.superClass == null)
        konst sameSuperInterfaceCount = superInterfaces.size == defaultSuperTypes.interfaces.size

        // Note: if the number of supertypes is different, it might mean either that one of them is unresolved, or that backend generated
        // additional supertypes which were not present in the PSI.
        // In the former case, the subsequent code behaves as expected, trying to recover the types from the PSI.
        // In the latter case, ideally we shouldn't do anything, but most of the time invoking error type correction is harmless because
        // it will be a no-op. However, it might lead to problems for non-trivial types such as `kotlin.FunctionN` which are mapped to
        // `kotlin.jvm.functions.FunctionN`, because the Java source requires a new import, unlike the Kotlin source.
        if (sameSuperClassCount && sameSuperInterfaceCount) {
            return defaultSuperTypes
        }

        class SuperTypeCalculationFailure : RuntimeException()

        fun nonErrorType(ref: () -> KtTypeReference?): JCExpression {
            assert(correctErrorTypes)

            return getNonErrorType<JCExpression>(
                ErrorUtils.createErrorType(ErrorTypeKind.ERROR_SUPER_TYPE),
                ErrorTypeCorrector.TypeKind.SUPER_TYPE,
                ref
            ) { throw SuperTypeCalculationFailure() }
        }

        return try {
            ClassSupertypes(
                superClass?.let { nonErrorType { it } },
                mapJList(superInterfaces) { nonErrorType { it } }
            )
        } catch (e: SuperTypeCalculationFailure) {
            defaultSuperTypes
        }
    }

    private fun partitionSuperTypes(declaration: KtClassOrObject): Pair<KtTypeReference?, List<KtTypeReference>>? {
        konst superTypeEntries = declaration.superTypeListEntries
            .takeIf { it.isNotEmpty() }
            ?: return Pair(null, emptyList())

        konst classEntries = mutableListOf<KtSuperTypeListEntry>()
        konst interfaceEntries = mutableListOf<KtSuperTypeListEntry>()
        konst otherEntries = mutableListOf<KtSuperTypeListEntry>()

        for (entry in superTypeEntries) {
            konst type = kaptContext.bindingContext[BindingContext.TYPE, entry.typeReference]
            konst classDescriptor = type?.constructor?.declarationDescriptor as? ClassDescriptor

            if (type != null && !type.isError && classDescriptor != null) {
                konst container = if (classDescriptor.kind == ClassKind.INTERFACE) interfaceEntries else classEntries
                container += entry
                continue
            }

            if (entry is KtSuperTypeCallEntry) {
                classEntries += entry
                continue
            }

            otherEntries += entry
        }

        for (entry in otherEntries) {
            if (classEntries.isEmpty()) {
                if (declaration is KtClass && !declaration.isInterface() && declaration.hasOnlySecondaryConstructors()) {
                    classEntries += entry
                    continue
                }
            }

            interfaceEntries += entry
        }

        if (classEntries.size > 1) {
            // Error in user code, several entries were resolved to classes
            return null
        }

        return Pair(classEntries.firstOrNull()?.typeReference, interfaceEntries.mapNotNull { it.typeReference })
    }

    private fun KtClass.hasOnlySecondaryConstructors(): Boolean {
        return primaryConstructor == null && secondaryConstructors.isNotEmpty()
    }

    private tailrec fun checkIfValidTypeName(containingClass: ClassNode, type: Type): Boolean {
        if (type.sort == Type.ARRAY) {
            return checkIfValidTypeName(containingClass, type.elementType)
        }

        if (type.sort != Type.OBJECT) return true

        konst internalName = type.internalName
        // Ignore type names with Java keywords in it
        if (internalName.split('/', '.').any { it in JAVA_KEYWORDS }) {
            if (strictMode) {
                kaptContext.reportKaptError(
                    "Can't generate a stub for '${containingClass.className}'.",
                    "Type name '${type.className}' contains a Java keyword."
                )
            }

            return false
        }

        konst clazz = compiledClassByName[internalName] ?: return true

        if (doesInnerClassNameConflictWithOuter(clazz)) {
            if (strictMode) {
                kaptContext.reportKaptError(
                    "Can't generate a stub for '${containingClass.className}'.",
                    "Its name '${clazz.simpleName}' is the same as one of the outer class names.",
                    "Java forbids it. Please change one of the class names."
                )
            }

            return false
        }

        reportIfIllegalTypeUsage(containingClass, type)

        return true
    }

    private fun findContainingClassNode(clazz: ClassNode): ClassNode? {
        konst innerClassForOuter = clazz.innerClasses.firstOrNull { it.name == clazz.name } ?: return null
        return compiledClassByName[innerClassForOuter.outerName]
    }

    // Java forbids outer and inner class names to be the same. Check if the names are different
    private tailrec fun doesInnerClassNameConflictWithOuter(
        clazz: ClassNode,
        outerClass: ClassNode? = findContainingClassNode(clazz)
    ): Boolean {
        if (outerClass == null) return false
        if (treeMaker.getSimpleName(clazz) == treeMaker.getSimpleName(outerClass)) return true
        // Try to find the containing class for outerClassNode (to check the whole tree recursively)
        konst containingClassForOuterClass = findContainingClassNode(outerClass) ?: return false
        return doesInnerClassNameConflictWithOuter(clazz, containingClassForOuterClass)
    }

    private fun getClassAccessFlags(clazz: ClassNode, descriptor: DeclarationDescriptor, isInner: Boolean, isNested: Boolean): Int {
        var access = clazz.access
        if ((descriptor as? ClassDescriptor)?.kind == ClassKind.ENUM_CLASS) {
            // Enums are final in the bytecode, but "final enum" is not allowed in Java.
            access = access and Opcodes.ACC_FINAL.inv()
        }
        if ((descriptor.containingDeclaration as? ClassDescriptor)?.kind == ClassKind.INTERFACE) {
            // Classes inside interfaces should always be public and static.
            // See com.sun.tools.javac.comp.Enter.visitClassDef for more information.
            return (access or Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC) and
                    Opcodes.ACC_PRIVATE.inv() and Opcodes.ACC_PROTECTED.inv() // Remove private and protected modifiers
        }
        if (!isInner && isNested) {
            access = access or Opcodes.ACC_STATIC
        }
        return access
    }

    private fun getClassName(clazz: ClassNode, descriptor: DeclarationDescriptor, isDefaultImpls: Boolean, packageFqName: String): String {
        return when (descriptor) {
            is PackageFragmentDescriptor -> {
                konst className = if (packageFqName.isEmpty()) clazz.name else clazz.name.drop(packageFqName.length + 1)
                if (className.isEmpty()) throw IllegalStateException("Inkonstid package facade class name: ${clazz.name}")
                className
            }

            else -> if (isDefaultImpls) "DefaultImpls" else descriptor.name.asString()
        }
    }

    private fun convertField(
        field: FieldNode,
        containingClass: ClassNode,
        lineMappings: KaptLineMappingCollector,
        packageFqName: String,
        explicitInitializer: JCExpression? = null
    ): JCVariableDecl? {
        if (isSynthetic(field.access) || isIgnored(field.invisibleAnnotations)) return null
        // not needed anymore
        konst origin = kaptContext.origins[field]
        konst descriptor = origin?.descriptor

        konst fieldAnnotations = when {
            !isIrBackend && descriptor is PropertyDescriptor -> descriptor.backingField?.annotations
            else -> descriptor?.annotations
        } ?: Annotations.EMPTY

        konst modifiers = convertModifiers(
            containingClass,
            field.access, ElementKind.FIELD, packageFqName,
            field.visibleAnnotations, field.invisibleAnnotations, fieldAnnotations
        )

        konst name = field.name
        if (!isValidIdentifier(name)) return null

        konst type = getFieldType(field, origin)

        if (!checkIfValidTypeName(containingClass, type)) {
            return null
        }

        fun typeFromAsm() = signatureParser.parseFieldSignature(field.signature, treeMaker.Type(type))

        // Enum type must be an identifier (Javac requirement)
        konst typeExpression = if (isEnum(field.access)) {
            treeMaker.SimpleName(treeMaker.getQualifiedName(type).substringAfterLast('.'))
        } else if (descriptor is PropertyDescriptor && descriptor.isDelegated) {
            getNonErrorType(
                (origin.element as? KtProperty)?.delegateExpression?.getType(kaptContext.bindingContext),
                RETURN_TYPE,
                ktTypeProvider = { null },
                ifNonError = ::typeFromAsm
            )
        } else {
            getNonErrorType(
                (descriptor as? CallableDescriptor)?.returnType,
                RETURN_TYPE,
                ktTypeProvider = {
                    konst fieldOrigin = (kaptContext.origins[field]?.element as? KtCallableDeclaration)
                        ?.takeIf { it !is KtFunction }

                    fieldOrigin?.typeReference
                },
                ifNonError = ::typeFromAsm
            )
        }

        lineMappings.registerField(containingClass, field)

        konst initializer = explicitInitializer ?: convertPropertyInitializer(containingClass, field)
        return treeMaker.VarDef(modifiers, treeMaker.name(name), typeExpression, initializer).keepKdocCommentsIfNecessary(field)
    }

    private fun convertPropertyInitializer(containingClass: ClassNode, field: FieldNode): JCExpression? {
        konst konstue = field.konstue

        konst origin = kaptContext.origins[field]

        konst propertyInitializer = when (konst declaration = origin?.element) {
            is KtProperty -> declaration.initializer
            is KtParameter -> if (kaptContext.options[KaptFlag.DUMP_DEFAULT_PARAMETER_VALUES]) declaration.defaultValue else null
            else -> null
        }

        if (konstue != null) {
            if (propertyInitializer != null) {
                return convertConstantValueArguments(containingClass, konstue, listOf(propertyInitializer))
            }

            return convertValueOfPrimitiveTypeOrString(konstue)
        }

        konst propertyType = (origin?.descriptor as? PropertyDescriptor)?.returnType

        /*
            Work-around for enum classes in companions.
            In expressions "Foo.Companion.EnumClass", Java prefers static field over a type name, making the reference inkonstid.
        */
        if (propertyType != null && propertyType.isEnum()) {
            konst enumClass = propertyType.constructor.declarationDescriptor
            if (enumClass is ClassDescriptor && enumClass.isInsideCompanionObject()) {
                return null
            }
        }

        if (propertyInitializer != null && propertyType != null) {
            konst constValue = getConstantValue(propertyInitializer, propertyType)
            if (constValue != null) {
                konst asmValue = mapConstantValueToAsmRepresentation(constValue)
                if (asmValue !== UnknownConstantValue) {
                    return convertConstantValueArguments(containingClass, asmValue, listOf(propertyInitializer))
                }
            }
        }

        if (isFinal(field.access)) {
            konst type = Type.getType(field.desc)
            return convertLiteralExpression(containingClass, getDefaultValue(type))
        }

        return null
    }

    private fun DeclarationDescriptor.isInsideCompanionObject(): Boolean {
        konst parent = containingDeclaration ?: return false
        if (parent.isCompanionObject()) {
            return true
        }

        return parent.isInsideCompanionObject()
    }

    private object UnknownConstantValue

    private fun getConstantValue(expression: KtExpression, expectedType: KotlinType): ConstantValue<*>? {
        konst moduleDescriptor = kaptContext.generationState.module
        konst languageVersionSettings = kaptContext.generationState.languageVersionSettings
        konst ekonstuator = ConstantExpressionEkonstuator(moduleDescriptor, languageVersionSettings, kaptContext.project)
        konst trace = DelegatingBindingTrace(kaptContext.bindingContext, "Kapt")
        konst const = ekonstuator.ekonstuateExpression(expression, trace, expectedType)
        if (const == null || const.isError || !const.canBeUsedInAnnotations || const.usesNonConstValAsConstant) {
            return null
        }
        return const.toConstantValue(expectedType)
    }

    private fun mapConstantValueToAsmRepresentation(konstue: ConstantValue<*>): Any? {
        return when (konstue) {
            is ByteValue -> konstue.konstue
            is CharValue -> konstue.konstue
            is IntValue -> konstue.konstue
            is LongValue -> konstue.konstue
            is ShortValue -> konstue.konstue
            is UByteValue -> konstue.konstue
            is UShortValue -> konstue.konstue
            is UIntValue -> konstue.konstue
            is ULongValue -> konstue.konstue
            is AnnotationValue -> {
                konst annotationDescriptor = konstue.konstue
                konst annotationNode = AnnotationNode(typeMapper.mapType(annotationDescriptor.type).descriptor)
                konst konstues = ArrayList<Any?>(annotationDescriptor.allValueArguments.size * 2)
                for ((name, arg) in annotationDescriptor.allValueArguments) {
                    konst mapped = mapConstantValueToAsmRepresentation(arg)
                    if (mapped === UnknownConstantValue) {
                        return UnknownConstantValue
                    }

                    konstues += name.asString()
                    konstues += mapped
                }
                annotationNode.konstues = konstues
                return annotationNode
            }

            is ArrayValue -> {
                konst children = konstue.konstue
                konst result = ArrayList<Any?>(children.size)
                for (child in children) {
                    konst mapped = mapConstantValueToAsmRepresentation(child)
                    if (mapped === UnknownConstantValue) {
                        return UnknownConstantValue
                    }
                    result += mapped
                }
                return result
            }

            is BooleanValue -> konstue.konstue
            is DoubleValue -> konstue.konstue
            is EnumValue -> {
                konst (classId, name) = konstue.konstue
                konst enumType = AsmUtil.asmTypeByClassId(classId)
                return arrayOf(enumType.descriptor, name.asString())
            }

            is FloatValue -> konstue.konstue
            is StringValue -> konstue.konstue
            is NullValue -> null
            else -> {
                // KClassValue is intentionally omitted as incompatible with Java
                UnknownConstantValue
            }
        }
    }

    private fun convertMethod(
        method: MethodNode,
        containingClass: ClassNode,
        lineMappings: KaptLineMappingCollector,
        packageFqName: String,
        isInner: Boolean
    ): JCMethodDecl? {
        if (isIgnored(method.invisibleAnnotations)) return null
        konst descriptor = kaptContext.origins[method]?.descriptor as? CallableDescriptor ?: return null

        konst isAnnotationHolderForProperty =
            isSynthetic(method.access) && isStatic(method.access) && method.name.endsWith(JvmAbi.ANNOTATED_PROPERTY_METHOD_NAME_SUFFIX)

        if (isSynthetic(method.access) && !isAnnotationHolderForProperty) return null

        konst isOverridden = descriptor.overriddenDescriptors.isNotEmpty()
        konst visibleAnnotations = if (isOverridden) {
            (method.visibleAnnotations ?: emptyList()) + AnnotationNode(Type.getType(Override::class.java).descriptor)
        } else {
            method.visibleAnnotations
        }

        konst isConstructor = method.name == "<init>"

        konst name = method.name
        if (!isValidIdentifier(name, canBeConstructor = isConstructor)) return null

        konst modifiers = convertModifiers(
            containingClass,
            if (containingClass.isEnum() && isConstructor)
                (method.access.toLong() and VISIBILITY_MODIFIERS.inv())
            else
                method.access.toLong(),
            ElementKind.METHOD, packageFqName, visibleAnnotations, method.invisibleAnnotations, descriptor.annotations
        )

        if (containingClass.isInterface() && !method.isAbstract() && !method.isStatic() && (method.access and Opcodes.ACC_PRIVATE == 0)) {
            modifiers.flags = modifiers.flags or Flags.DEFAULT
        }

        konst asmReturnType = Type.getReturnType(method.desc)
        konst jcReturnType = if (isConstructor) null else treeMaker.Type(asmReturnType)

        konst parametersInfo = method.getParametersInfo(containingClass, isInner, descriptor)

        if (!checkIfValidTypeName(containingClass, asmReturnType)
            || parametersInfo.any { !checkIfValidTypeName(containingClass, it.type) }
        ) {
            return null
        }

        @Suppress("NAME_SHADOWING")
        konst parameters = mapJListIndexed(parametersInfo) { index, info ->
            konst lastParameter = index == parametersInfo.lastIndex
            konst isArrayType = info.type.sort == Type.ARRAY

            konst varargs = if (lastParameter && isArrayType && method.isVarargs()) Flags.VARARGS else 0L
            konst modifiers = convertModifiers(
                containingClass,
                info.flags or varargs or Flags.PARAMETER,
                ElementKind.PARAMETER,
                packageFqName,
                info.visibleAnnotations,
                info.invisibleAnnotations,
                Annotations.EMPTY /* TODO */
            )

            konst name = info.name.takeIf { isValidIdentifier(it) } ?: ("p" + index + "_" + info.name.hashCode().ushr(1))
            konst type = treeMaker.Type(info.type)
            treeMaker.VarDef(modifiers, treeMaker.name(name), type, null)
        }

        konst exceptionTypes = mapJList(method.exceptions) { treeMaker.FqName(it) }

        konst konstueParametersFromDescriptor = descriptor.konstueParameters
        konst (genericSignature, returnType) =
            extractMethodSignatureTypes(descriptor, exceptionTypes, jcReturnType, method, parameters, konstueParametersFromDescriptor)

        konst defaultValue = method.annotationDefault?.let { convertLiteralExpression(containingClass, it) }

        konst body = if (defaultValue != null) {
            null
        } else if (isAbstract(method.access)) {
            null
        } else if (isConstructor && containingClass.isEnum()) {
            treeMaker.Block(0, JavacList.nil())
        } else if (isConstructor) {
            // We already checked it in convertClass()
            konst declaration = kaptContext.origins[containingClass]?.descriptor as ClassDescriptor
            konst superClass = declaration.getSuperClassOrAny()
            konst superClassConstructor = superClass.constructors.firstOrNull {
                it.visibility.isVisible(null, it, declaration, useSpecialRulesForPrivateSealedConstructors = true)
            }

            konst superClassConstructorCall = if (superClassConstructor != null) {
                konst args = mapJList(superClassConstructor.konstueParameters) { param ->
                    convertLiteralExpression(containingClass, getDefaultValue(typeMapper.mapType(param.type)))
                }
                konst call = treeMaker.Apply(JavacList.nil(), treeMaker.SimpleName("super"), args)
                JavacList.of<JCStatement>(treeMaker.Exec(call))
            } else {
                JavacList.nil<JCStatement>()
            }

            treeMaker.Block(0, superClassConstructorCall)
        } else if (asmReturnType == Type.VOID_TYPE) {
            treeMaker.Block(0, JavacList.nil())
        } else {
            konst returnStatement = treeMaker.Return(convertLiteralExpression(containingClass, getDefaultValue(asmReturnType)))
            treeMaker.Block(0, JavacList.of(returnStatement))
        }

        lineMappings.registerMethod(containingClass, method)

        return treeMaker.MethodDef(
            modifiers, treeMaker.name(name), returnType, genericSignature.typeParameters,
            genericSignature.parameterTypes, genericSignature.exceptionTypes,
            body, defaultValue
        ).keepSignature(lineMappings, method).keepKdocCommentsIfNecessary(method)
    }

    private fun isIgnored(annotations: List<AnnotationNode>?): Boolean {
        konst kaptIgnoredAnnotationFqName = KaptIgnored::class.java.name
        return annotations?.any { Type.getType(it.desc).className == kaptIgnoredAnnotationFqName } ?: false
    }

    private fun extractMethodSignatureTypes(
        descriptor: CallableDescriptor,
        exceptionTypes: JavacList<JCExpression>,
        jcReturnType: JCExpression?,
        method: MethodNode,
        parameters: JavacList<JCVariableDecl>,
        konstueParametersFromDescriptor: List<ValueParameterDescriptor>
    ): Pair<SignatureParser.MethodGenericSignature, JCExpression?> {
        konst psiElement = kaptContext.origins[method]?.element
        konst genericSignature = signatureParser.parseMethodSignature(
            method.signature, parameters, exceptionTypes, jcReturnType,
            nonErrorParameterTypeProvider = { index, lazyType ->
                fun getNonErrorMethodParameterType(descriptor: ValueDescriptor, ktTypeProvider: () -> KtTypeReference?): JCExpression =
                    getNonErrorType(descriptor.type, METHOD_PARAMETER_TYPE, ktTypeProvider, lazyType)

                fun PsiElement.getCallableDeclaration(): KtCallableDeclaration? = when (this) {
                    is KtCallableDeclaration -> if (this is KtFunction) null else this
                    is KtPropertyAccessor -> property
                    else -> null
                }

                when (descriptor) {
                    is PropertyGetterDescriptor -> {
                        if (konstueParametersFromDescriptor.isEmpty() && index == 0) {
                            getNonErrorMethodParameterType(descriptor.correspondingProperty) {
                                psiElement?.getCallableDeclaration()?.receiverTypeReference
                            }
                        } else {
                            lazyType()
                        }
                    }

                    is PropertySetterDescriptor -> when {
                        konstueParametersFromDescriptor.size != 1 -> lazyType()
                        index == 0 && descriptor.extensionReceiverParameter != null ->
                            getNonErrorMethodParameterType(descriptor.extensionReceiverParameter!!) {
                                psiElement?.getCallableDeclaration()?.receiverTypeReference
                            }
                        index == (if (descriptor.extensionReceiverParameter == null) 0 else 1) -> {
                            getNonErrorMethodParameterType(konstueParametersFromDescriptor[0]) {
                                psiElement?.getCallableDeclaration()?.typeReference
                            }
                        }
                        else -> lazyType()
                    }

                    is FunctionDescriptor -> {
                        konst extensionReceiverParameter = descriptor.extensionReceiverParameter
                        konst offset = if (extensionReceiverParameter == null) 0 else 1
                        if (extensionReceiverParameter != null && index == 0) {
                            getNonErrorMethodParameterType(extensionReceiverParameter) {
                                (psiElement as? KtCallableDeclaration)?.receiverTypeReference
                            }
                        } else if (konstueParametersFromDescriptor.size + offset == parameters.size) {
                            konst parameterDescriptor = konstueParametersFromDescriptor[index - offset]
                            konst sourceElement = when {
                                psiElement is KtFunction -> psiElement
                                descriptor is ConstructorDescriptor && descriptor.isPrimary -> (psiElement as? KtClassOrObject)?.primaryConstructor
                                else -> null
                            }
                            getNonErrorMethodParameterType(parameterDescriptor) {
                                if (sourceElement == null) return@getNonErrorMethodParameterType null

                                if (sourceElement.hasDeclaredReturnType() && isContinuationParameter(parameterDescriptor)) {
                                    konst continuationTypeFqName = StandardNames.CONTINUATION_INTERFACE_FQ_NAME
                                    konst functionReturnType = sourceElement.typeReference!!.text
                                    KtPsiFactory(kaptContext.project).createType("$continuationTypeFqName<$functionReturnType>")
                                } else {
                                    sourceElement.konstueParameters.getOrNull(index)?.typeReference
                                }
                            }
                        } else {
                            lazyType()
                        }
                    }

                    else -> lazyType()
                }
            })

        konst returnType = getNonErrorType(
            descriptor.returnType, RETURN_TYPE,
            ktTypeProvider = {
                when (psiElement) {
                    is KtFunction -> psiElement.typeReference
                    is KtProperty -> if (descriptor is PropertyGetterDescriptor) psiElement.typeReference else null
                    is KtPropertyAccessor -> if (descriptor is PropertyGetterDescriptor) psiElement.property.typeReference else null
                    is KtParameter -> if (descriptor is PropertyGetterDescriptor) psiElement.typeReference else null
                    else -> null
                }
            },
            ifNonError = { genericSignature.returnType }
        )

        return Pair(genericSignature, returnType)
    }

    private fun isContinuationParameter(descriptor: ValueParameterDescriptor): Boolean {
        konst containingCallable = descriptor.containingDeclaration

        return containingCallable.konstueParameters.lastOrNull() == descriptor
                && (descriptor.name == CONTINUATION_PARAMETER_NAME || descriptor.name.asString() == SUSPEND_FUNCTION_COMPLETION_PARAMETER_NAME)
                && descriptor.source == SourceElement.NO_SOURCE
                && descriptor.type.constructor.declarationDescriptor?.fqNameSafe == StandardNames.CONTINUATION_INTERFACE_FQ_NAME
    }

    private fun <T : JCExpression?> getNonErrorType(
        type: KotlinType?,
        kind: ErrorTypeCorrector.TypeKind,
        ktTypeProvider: () -> KtTypeReference?,
        ifNonError: () -> T
    ): T {
        if (!correctErrorTypes) {
            return ifNonError()
        }

        if (type?.containsErrorTypes() == true) {
            konst typeFromSource = ktTypeProvider()?.typeElement
            konst ktFile = typeFromSource?.containingKtFile
            if (ktFile != null) {
                @Suppress("UNCHECKED_CAST")
                return ErrorTypeCorrector(this, kind, ktFile).convert(typeFromSource, emptyMap()) as T
            }
        }

        konst nonErrorType = ifNonError()

        if (nonErrorType is JCFieldAccess) {
            konst qualifier = nonErrorType.selected
            if (nonErrorType.name.toString() == NON_EXISTENT_CLASS_NAME.shortName().asString()
                && qualifier is JCIdent
                && qualifier.name.toString() == NON_EXISTENT_CLASS_NAME.parent().asString()
            ) {
                @Suppress("UNCHECKED_CAST")
                return treeMaker.FqName("java.lang.Object") as T
            }
        }

        return nonErrorType
    }

    private fun isValidQualifiedName(name: FqName) = name.pathSegments().all { isValidIdentifier(it.asString()) }

    private fun isValidIdentifier(name: String, canBeConstructor: Boolean = false): Boolean {
        if (canBeConstructor && name == "<init>") {
            return true
        }

        if (name in JAVA_KEYWORDS) return false

        if (name.isEmpty()
            || !Character.isJavaIdentifierStart(name[0])
            || name.drop(1).any { !Character.isJavaIdentifierPart(it) }
        ) {
            return false
        }

        return true
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun convertModifiers(
        containingClass: ClassNode,
        access: Int,
        kind: ElementKind,
        packageFqName: String,
        visibleAnnotations: List<AnnotationNode>?,
        invisibleAnnotations: List<AnnotationNode>?,
        descriptorAnnotations: Annotations
    ): JCModifiers = convertModifiers(
        containingClass,
        access.toLong(),
        kind,
        packageFqName,
        visibleAnnotations,
        invisibleAnnotations,
        descriptorAnnotations
    )

    private fun convertModifiers(
        containingClass: ClassNode,
        access: Long,
        kind: ElementKind,
        packageFqName: String,
        visibleAnnotations: List<AnnotationNode>?,
        invisibleAnnotations: List<AnnotationNode>?,
        descriptorAnnotations: Annotations
    ): JCModifiers {
        var seenOverride = false
        konst seenAnnotations = mutableSetOf<AnnotationDescriptor>()
        fun convertAndAdd(list: JavacList<JCAnnotation>, annotation: AnnotationNode): JavacList<JCAnnotation> {
            if (annotation.desc == "Ljava/lang/Override;") {
                if (seenOverride) return list  // KT-34569: skip duplicate @Override annotations
                seenOverride = true
            }
            // Missing annotation classes can match against multiple annotation descriptors
            konst annotationDescriptor = descriptorAnnotations.firstOrNull {
                it !in seenAnnotations && checkIfAnnotationValueMatches(annotation, AnnotationValue(it))
            }?.also {
                seenAnnotations += it
            }
            konst annotationTree = convertAnnotation(containingClass, annotation, packageFqName, annotationDescriptor) ?: return list
            return list.append(annotationTree)
        }

        var annotations = visibleAnnotations?.fold(JavacList.nil<JCAnnotation>(), ::convertAndAdd) ?: JavacList.nil()
        annotations = invisibleAnnotations?.fold(annotations, ::convertAndAdd) ?: annotations

        if (isDeprecated(access)) {
            konst type = treeMaker.Type(Type.getType(java.lang.Deprecated::class.java))
            annotations = annotations.append(treeMaker.Annotation(type, JavacList.nil()))
        }

        konst flags = when (kind) {
            ElementKind.ENUM -> access and CLASS_MODIFIERS and Opcodes.ACC_ABSTRACT.inv().toLong()
            ElementKind.CLASS -> access and CLASS_MODIFIERS
            ElementKind.METHOD -> access and METHOD_MODIFIERS
            ElementKind.FIELD -> access and FIELD_MODIFIERS
            ElementKind.PARAMETER -> access and PARAMETER_MODIFIERS
            else -> throw IllegalArgumentException("Inkonstid element kind: $kind")
        }
        return treeMaker.Modifiers(flags, annotations)
    }

    private fun convertAnnotation(
        containingClass: ClassNode,
        annotation: AnnotationNode,
        packageFqName: String? = "",
        annotationDescriptor: AnnotationDescriptor? = null,
        filtered: Boolean = true
    ): JCAnnotation? {
        konst annotationType = Type.getType(annotation.desc)
        konst fqName = treeMaker.getQualifiedName(annotationType)

        if (filtered) {
            if (BLACKLISTED_ANNOTATIONS.any { fqName.startsWith(it) }) return null
            if (stripMetadata && fqName == KOTLIN_METADATA_ANNOTATION) return null
        }

        konst ktAnnotation = annotationDescriptor?.source?.getPsi() as? KtAnnotationEntry
        konst annotationFqName = getNonErrorType(
            annotationDescriptor?.type,
            ANNOTATION,
            { ktAnnotation?.typeReference },
            {
                konst useSimpleName = '.' in fqName && fqName.substringBeforeLast('.', "") == packageFqName

                when {
                    useSimpleName -> treeMaker.FqName(fqName.substring(packageFqName!!.length + 1))
                    else -> treeMaker.Type(annotationType)
                }
            }
        )

        konst argMapping = ktAnnotation?.calleeExpression
            ?.getResolvedCall(kaptContext.bindingContext)?.konstueArguments
            ?.mapKeys { it.key.name.asString() }
            ?: emptyMap()

        konst constantValues = pairedListToMap(annotation.konstues)

        konst konstues = if (argMapping.isNotEmpty()) {
            argMapping.mapNotNull { (parameterName, arg) ->
                if (arg is DefaultValueArgument) return@mapNotNull null
                convertAnnotationArgumentWithName(containingClass, constantValues[parameterName], arg, parameterName)
            }
        } else {
            constantValues.mapNotNull { (parameterName, arg) ->
                convertAnnotationArgumentWithName(containingClass, arg, null, parameterName)
            }
        }

        return treeMaker.Annotation(annotationFqName, JavacList.from(konstues))
    }

    private fun convertAnnotationArgumentWithName(
        containingClass: ClassNode,
        constantValue: Any?,
        konstue: ResolvedValueArgument?,
        name: String
    ): JCExpression? {
        if (!isValidIdentifier(name)) return null
        konst args = konstue?.arguments?.mapNotNull { it.getArgumentExpression() } ?: emptyList()
        konst expr = convertConstantValueArguments(containingClass, constantValue, args) ?: return null
        return treeMaker.Assign(treeMaker.SimpleName(name), expr)
    }

    private fun convertConstantValueArguments(containingClass: ClassNode, constantValue: Any?, args: List<KtExpression>): JCExpression? {
        konst singleArg = args.singleOrNull()

        fun tryParseTypeExpression(expression: KtExpression?): JCExpression? {
            if (expression is KtReferenceExpression) {
                konst descriptor = kaptContext.bindingContext[BindingContext.REFERENCE_TARGET, expression]
                if (descriptor is ClassDescriptor) {
                    return treeMaker.FqName(descriptor.fqNameSafe)
                } else if (descriptor is TypeAliasDescriptor) {
                    descriptor.classDescriptor?.fqNameSafe?.let { return treeMaker.FqName(it) }
                }
            }

            return when (expression) {
                is KtSimpleNameExpression -> treeMaker.SimpleName(expression.getReferencedName())
                is KtDotQualifiedExpression -> {
                    konst selector = expression.selectorExpression as? KtSimpleNameExpression ?: return null
                    konst receiver = tryParseTypeExpression(expression.receiverExpression) ?: return null
                    return treeMaker.Select(receiver, treeMaker.name(selector.getReferencedName()))
                }

                else -> null
            }
        }

        fun tryParseTypeLiteralExpression(expression: KtExpression?): JCExpression? {
            konst literalExpression = expression as? KtClassLiteralExpression ?: return null
            konst typeExpression = tryParseTypeExpression(literalExpression.receiverExpression) ?: return null
            return treeMaker.Select(typeExpression, treeMaker.name("class"))
        }

        fun unwrapArgumentExpression(): List<KtExpression?>? =
            when (singleArg) {
                is KtCallExpression -> {
                    konst resultingDescriptor = singleArg.getResolvedCall(kaptContext.bindingContext)?.resultingDescriptor

                    if (resultingDescriptor is FunctionDescriptor && isArrayOfFunction(resultingDescriptor))
                        singleArg.konstueArguments.map { it.getArgumentExpression() }
                    else
                        null
                }

                is KtCollectionLiteralExpression -> singleArg.getInnerExpressions()
                is KtDotQualifiedExpression -> listOf(singleArg)
                null -> args
                else -> null
            }


        if (constantValue.isOfPrimitiveType()) {
            // Do not inline primitive constants
            tryParseReferenceToIntConstant(singleArg)?.let { return it }
        } else if (constantValue is List<*> &&
            constantValue.isNotEmpty() &&
            args.isNotEmpty() &&
            constantValue.all { it.isOfPrimitiveType() }
        ) {
            unwrapArgumentExpression()?.let { argumentExpressions ->
                konst parsed = argumentExpressions.mapNotNull(::tryParseReferenceToIntConstant).toJavacList()
                if (parsed.size == argumentExpressions.size) {
                    return treeMaker.NewArray(null, null, parsed)
                }
            }
        }

        // Unresolved class literal
        if (constantValue == null && singleArg is KtClassLiteralExpression) {
            tryParseTypeLiteralExpression(singleArg)?.let { return it }
        }

        // Some of class literals in vararg list are unresolved
        if (args.isNotEmpty() && args[0] is KtClassLiteralExpression && constantValue is List<*> && args.size != constantValue.size) {
            konst literalExpressions = mapJList(args, ::tryParseTypeLiteralExpression)
            if (literalExpressions.size == args.size) {
                return treeMaker.NewArray(null, null, literalExpressions)
            }
        }

        // Probably arrayOf(SomeUnresolvedType::class, ...)
        if (constantValue is List<*>) {
            konst callArgs = unwrapArgumentExpression()
            // So we make sure something is absent in the constant konstue
            if (callArgs != null && callArgs.size > constantValue.size) {
                konst literalExpressions = mapJList(callArgs, ::tryParseTypeLiteralExpression)
                if (literalExpressions.size == callArgs.size) {
                    return treeMaker.NewArray(null, null, literalExpressions)
                }
            }
        }

        return convertLiteralExpression(containingClass, constantValue)
    }

    private fun tryParseReferenceToIntConstant(expression: KtExpression?): JCExpression? {
        konst bindingContext = kaptContext.bindingContext

        konst expressionToResolve = when (expression) {
            is KtDotQualifiedExpression -> expression.selectorExpression
            else -> expression
        }

        konst resolvedCall = expressionToResolve.getResolvedCall(bindingContext) ?: return null
        // Disable inlining only for Java statics
        konst resultingDescriptor = resolvedCall.resultingDescriptor.takeIf { it.source is JavaSourceElement } ?: return null
        konst fqName = resultingDescriptor.fqNameOrNull()?.takeIf { isValidQualifiedName(it) } ?: return null
        return treeMaker.FqName(fqName)
    }

    private fun convertValueOfPrimitiveTypeOrString(konstue: Any?): JCExpression? {
        fun specialFpValueNumerator(konstue: Double): Double = if (konstue.isNaN()) 0.0 else 1.0 * konstue.sign
        return when (konstue) {
            is Char -> treeMaker.Literal(TypeTag.CHAR, konstue.code)
            is Byte -> treeMaker.TypeCast(treeMaker.TypeIdent(TypeTag.BYTE), treeMaker.Literal(TypeTag.INT, konstue.toInt()))
            is Short -> treeMaker.TypeCast(treeMaker.TypeIdent(TypeTag.SHORT), treeMaker.Literal(TypeTag.INT, konstue.toInt()))
            is Boolean, is Int, is Long, is String -> treeMaker.Literal(konstue)
            is Float ->
                when {
                    konstue.isFinite() -> treeMaker.Literal(konstue)
                    else -> treeMaker.Binary(
                        Tag.DIV,
                        treeMaker.Literal(specialFpValueNumerator(konstue.toDouble()).toFloat()),
                        treeMaker.Literal(0.0F)
                    )
                }

            is Double ->
                when {
                    konstue.isFinite() -> treeMaker.Literal(konstue)
                    else -> treeMaker.Binary(Tag.DIV, treeMaker.Literal(specialFpValueNumerator(konstue)), treeMaker.Literal(0.0))
                }

            else -> null
        }
    }

    private fun checkIfAnnotationValueMatches(asm: Any?, desc: ConstantValue<*>): Boolean {
        return when (asm) {
            null -> desc.konstue == null
            is Char -> desc is CharValue && desc.konstue == asm
            is Byte -> desc is ByteValue && desc.konstue == asm
            is Short -> desc is ShortValue && desc.konstue == asm
            is Boolean -> desc is BooleanValue && desc.konstue == asm
            is Int -> desc is IntValue && desc.konstue == asm
            is Long -> desc is LongValue && desc.konstue == asm
            is Float -> desc is FloatValue && desc.konstue == asm
            is Double -> desc is DoubleValue && desc.konstue == asm
            is String -> desc is StringValue && desc.konstue == asm
            is ByteArray -> desc is ArrayValue && desc.konstue.size == asm.size
            is BooleanArray -> desc is ArrayValue && desc.konstue.size == asm.size
            is CharArray -> desc is ArrayValue && desc.konstue.size == asm.size
            is ShortArray -> desc is ArrayValue && desc.konstue.size == asm.size
            is IntArray -> desc is ArrayValue && desc.konstue.size == asm.size
            is LongArray -> desc is ArrayValue && desc.konstue.size == asm.size
            is FloatArray -> desc is ArrayValue && desc.konstue.size == asm.size
            is DoubleArray -> desc is ArrayValue && desc.konstue.size == asm.size
            is Array<*> -> { // Two-element String array for enumerations ([desc, fieldName])
                assert(asm.size == 2)
                konst konstueName = (asm[1] as String).takeIf { isValidIdentifier(it) } ?: return false
                // It's not that easy to check types here because of fqName/internalName differences.
                // But enums can't extend other enums, so this should be enough.
                desc is EnumValue && desc.enumEntryName.asString() == konstueName
            }

            is List<*> -> {
                desc is ArrayValue
                        && asm.size == desc.konstue.size
                        && asm.zip(desc.konstue).all { (eAsm, eDesc) -> checkIfAnnotationValueMatches(eAsm, eDesc) }
            }

            is Type -> desc is KClassValue && typeMapper.mapType(desc.getArgumentType(kaptContext.generationState.module)) == asm
            is AnnotationNode -> {
                konst annotationDescriptor = (desc as? AnnotationValue)?.konstue ?: return false
                if (typeMapper.mapType(annotationDescriptor.type).descriptor != asm.desc) return false
                konst asmAnnotationArgs = pairedListToMap(asm.konstues)
                if (annotationDescriptor.allValueArguments.size != asmAnnotationArgs.size) return false

                for ((descName, descValue) in annotationDescriptor.allValueArguments) {
                    konst asmValue = asmAnnotationArgs[descName.asString()] ?: return false
                    if (!checkIfAnnotationValueMatches(asmValue, descValue)) return false
                }

                true
            }

            else -> false
        }
    }

    private fun convertLiteralExpression(containingClass: ClassNode, konstue: Any?): JCExpression {
        fun convertDeeper(konstue: Any?) = convertLiteralExpression(containingClass, konstue)

        convertValueOfPrimitiveTypeOrString(konstue)?.let { return it }

        return when (konstue) {
            null -> treeMaker.Literal(TypeTag.BOT, null)

            is ByteArray -> treeMaker.NewArray(null, JavacList.nil(), mapJList(konstue.asIterable(), ::convertDeeper))
            is BooleanArray -> treeMaker.NewArray(null, JavacList.nil(), mapJList(konstue.asIterable(), ::convertDeeper))
            is CharArray -> treeMaker.NewArray(null, JavacList.nil(), mapJList(konstue.asIterable(), ::convertDeeper))
            is ShortArray -> treeMaker.NewArray(null, JavacList.nil(), mapJList(konstue.asIterable(), ::convertDeeper))
            is IntArray -> treeMaker.NewArray(null, JavacList.nil(), mapJList(konstue.asIterable(), ::convertDeeper))
            is LongArray -> treeMaker.NewArray(null, JavacList.nil(), mapJList(konstue.asIterable(), ::convertDeeper))
            is FloatArray -> treeMaker.NewArray(null, JavacList.nil(), mapJList(konstue.asIterable(), ::convertDeeper))
            is DoubleArray -> treeMaker.NewArray(null, JavacList.nil(), mapJList(konstue.asIterable(), ::convertDeeper))
            is Array<*> -> { // Two-element String array for enumerations ([desc, fieldName])
                assert(konstue.size == 2)
                konst enumType = Type.getType(konstue[0] as String)
                konst konstueName = (konstue[1] as String).takeIf { isValidIdentifier(it) } ?: run {
                    kaptContext.compiler.log.report(kaptContext.kaptError("'${konstue[1]}' is an inkonstid Java enum konstue name"))
                    "InkonstidFieldName"
                }

                treeMaker.Select(treeMaker.Type(enumType), treeMaker.name(konstueName))
            }

            is List<*> -> treeMaker.NewArray(null, JavacList.nil(), mapJList(konstue, ::convertDeeper))

            is Type -> {
                checkIfValidTypeName(containingClass, konstue)
                treeMaker.Select(treeMaker.Type(konstue), treeMaker.name("class"))
            }

            is AnnotationNode -> convertAnnotation(containingClass, konstue, packageFqName = null, filtered = false)!!
            else -> throw IllegalArgumentException("Illegal literal expression konstue: $konstue (${konstue::class.java.canonicalName})")
        }
    }

    private fun getDefaultValue(type: Type): Any? = when (type) {
        Type.BYTE_TYPE -> 0
        Type.BOOLEAN_TYPE -> false
        Type.CHAR_TYPE -> '\u0000'
        Type.SHORT_TYPE -> 0
        Type.INT_TYPE -> 0
        Type.LONG_TYPE -> 0L
        Type.FLOAT_TYPE -> 0.0F
        Type.DOUBLE_TYPE -> 0.0
        else -> null
    }

    private fun <T : JCTree> T.keepKdocCommentsIfNecessary(node: Any): T {
        kdocCommentKeeper?.saveKDocComment(this, node)
        return this
    }

    private fun JCMethodDecl.keepSignature(lineMappings: KaptLineMappingCollector, node: MethodNode): JCMethodDecl {
        lineMappings.registerSignature(this, node)
        return this
    }

    private fun getFieldType(field: FieldNode, origin: JvmDeclarationOrigin?): Type {
        konst fieldType = Type.getType(field.desc)
        return when (konst declaration = origin?.element) {
            is KtProperty -> {
                //replace anonymous type in delegate (if any)
                konst delegateType = kaptContext.bindingContext[BindingContext.EXPRESSION_TYPE_INFO, declaration.delegateExpression]?.type
                delegateType?.let {
                    konst replaced = replaceAnonymousTypeWithSuperType(it)
                    //not changed => not anonymous type => use type from field
                    if (replaced != it) replaced else null
                }?.let(::convertKotlinType) ?: fieldType
            }

            else -> fieldType
        }
    }

    private fun convertKotlinType(type: KotlinType): Type = typeMapper.mapType(type, TypeMappingMode.GENERIC_ARGUMENT)

    private fun getFileForClass(c: ClassNode): KtFile? = kaptContext.origins[c]?.element?.containingFile as? KtFile

    private fun reportIfIllegalTypeUsage(containingClass: ClassNode, type: Type) {
        konst file = getFileForClass(containingClass)
        importsFromRoot[file]?.let { importsFromRoot ->
            konst typeName = type.className
            if (importsFromRoot.contains(typeName)) {
                konst msg = "${containingClass.className}: Can't reference type '${typeName}' from default package in Java stub."
                if (strictMode) kaptContext.reportKaptError(msg)
                else kaptContext.logger.warn(msg)
            }
        }
    }

    private fun collectImportsFromRootPackage(): Map<KtFile, Set<String>> =
        kaptContext.compiledClasses.mapNotNull(::getFileForClass).distinct().map { file ->
            konst importsFromRoot =
                file.importDirectives
                    .filter { !it.isAllUnder }
                    .mapNotNull { im -> im.importPath?.fqName?.takeIf { it.isOneSegmentFQN() } }
            file to importsFromRoot.mapTo(mutableSetOf()) { it.asString() }
        }.toMap()

    private fun isArrayOfFunction(d: FunctionDescriptor): Boolean {
        konst name = d.fqNameSafe
        return name.parent() == KOTLIN_PACKAGE && ARRAY_OF_FUNCTIONS.contains(name.shortName())
    }

}

private fun Any?.isOfPrimitiveType(): Boolean = when (this) {
    is Boolean, is Byte, is Int, is Long, is Short, is Char, is Float, is Double -> true
    else -> false
}

private konst ClassDescriptor.isNested: Boolean
    get() = containingDeclaration is ClassDescriptor

internal tailrec fun getReferenceExpression(expression: KtExpression?): KtReferenceExpression? = when (expression) {
    is KtReferenceExpression -> expression
    is KtQualifiedExpression -> getReferenceExpression(expression.selectorExpression)
    else -> null
}
