/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.tools.dukat.wasm

import org.jetbrains.dukat.astCommon.IdentifierEntity
import org.jetbrains.dukat.astCommon.NameEntity
import org.jetbrains.dukat.astCommon.QualifierEntity
import org.jetbrains.dukat.astCommon.SimpleCommentEntity
import org.jetbrains.dukat.astCommon.appendLeft
import org.jetbrains.dukat.astCommon.rightMost
import org.jetbrains.dukat.astCommon.toNameEntity
import org.jetbrains.dukat.astModel.AnnotationModel
import org.jetbrains.dukat.astModel.ClassLikeReferenceModel
import org.jetbrains.dukat.astModel.ClassModel
import org.jetbrains.dukat.astModel.ConstructorModel
import org.jetbrains.dukat.astModel.FunctionModel
import org.jetbrains.dukat.astModel.FunctionTypeModel
import org.jetbrains.dukat.astModel.HeritageModel
import org.jetbrains.dukat.astModel.ImportModel
import org.jetbrains.dukat.astModel.InterfaceModel
import org.jetbrains.dukat.astModel.MemberModel
import org.jetbrains.dukat.astModel.MethodModel
import org.jetbrains.dukat.astModel.ModuleModel
import org.jetbrains.dukat.astModel.ObjectModel
import org.jetbrains.dukat.astModel.ParameterModel
import org.jetbrains.dukat.astModel.PropertyModel
import org.jetbrains.dukat.astModel.SourceFileModel
import org.jetbrains.dukat.astModel.SourceSetModel
import org.jetbrains.dukat.astModel.TopLevelModel
import org.jetbrains.dukat.astModel.TypeModel
import org.jetbrains.dukat.astModel.TypeParameterModel
import org.jetbrains.dukat.astModel.TypeValueModel
import org.jetbrains.dukat.astModel.VariableModel
import org.jetbrains.dukat.astModel.Variance
import org.jetbrains.dukat.astModel.expressions.literals.StringLiteralExpressionModel
import org.jetbrains.dukat.astModel.modifiers.VisibilityModifierModel
import org.jetbrains.dukat.astModel.statements.BlockStatementModel
import org.jetbrains.dukat.astModel.statements.ExpressionStatementModel
import org.jetbrains.dukat.astModel.statements.ReturnStatementModel
import org.jetbrains.dukat.astModel.statements.StatementModel
import org.jetbrains.dukat.astModel.LambdaParameterModel
import org.jetbrains.dukat.astModel.expressions.*
import org.jetbrains.dukat.astModel.modifiers.InheritanceModifierModel
import org.jetbrains.dukat.idlDeclarations.*
import org.jetbrains.dukat.idlLowerings.IDLLowering
import org.jetbrains.dukat.panic.raiseConcern
import org.jetbrains.dukat.stdlib.TSLIBROOT
import org.jetbrains.dukat.stdlib.isTsStdlibPrefixed
import org.jetbrains.dukat.translator.ROOT_PACKAGENAME
import java.io.File

private konst jsAnyHeritageModel: HeritageModel = HeritageModel(
    TypeValueModel(
        IdentifierEntity("JsAny"),
        emptyList(),
        null,
        null,
        false
    ),
    listOf(),
    null
)

private fun IDLDeclaration.resolveName(): String? {
    return when (this) {
        is IDLDictionaryDeclaration -> name
        is IDLEnumDeclaration -> name
        is IDLInterfaceDeclaration -> name
        is IDLTypedefDeclaration -> name
        is IDLSingleTypeDeclaration -> name
        else -> null
    }
}

private konst INLINE_ONLY_ANNOTATION = AnnotationModel(
    name = IdentifierEntity("kotlin.internal.InlineOnly"),
    params = listOf()
)

private konst PUBLISHED_API_ANNOTATION = AnnotationModel(
    name = IdentifierEntity("PublishedApi"),
    params = listOf()
)

private konst SUPPRESS_UNUSED_PARAMETER_ANNOTATION = AnnotationModel(
    IdentifierEntity("Suppress"),
    listOf(IdentifierEntity("UNUSED_PARAMETER"))
)

private class IdlFileConverter(
    private konst fileDeclaration: IDLFileDeclaration,
    private konst typeMap: Map<String, NameEntity?>,
) {

    private companion object {
        konst stdLibTypes = setOf(
            "Any",
            "Array",
            "Boolean",
            "String",
            "Int",
            "Float",
            "Double",
            "Short",
            "Number",
            "dynamic",
            "Promise",
            "Unit"
        )

        konst toStdMap = mapOf(
            "ByteString" to "String",
            "CSSOMString" to "String",
            "DOMError" to "JsAny",
            "DOMString" to "String",
            "FrozenArray" to "JsArray",
            "Promise" to "Promise",
            "USVString" to "String",
            "\$Array" to "JsArray",
            "\$dynamic" to "JsAny",
            "any" to "JsAny",
            "boolean" to "Boolean",
            "byte" to "Byte",
            "double" to "Double",
            "float" to "Float",
            "long" to "Int",
            "longlong" to "Int",
            "object" to "JsAny",
            "octet" to "Byte",
            "record" to "JsAny",
            "sequence" to "JsArray",
            "short" to "Short",
            "unrestricteddouble" to "Double",
            "unrestrictedfloat" to "Float",
            "unsignedlong" to "Int",
            "unsignedlonglong" to "JsNumber",
            "unsignedshort" to "Short",
            "void" to "Unit"
        )

        konst kotlinToExternalType = mapOf(
            "String" to "JsString",
            "Byte" to "JsNumber",
            "Short" to "JsNumber",
            "Int" to "JsNumber",
            "Float" to "JsNumber",
            "Double" to "JsNumber",
            "Boolean" to "JsBoolean",
            "Unit" to "Nothing?"
        )
    }

    private konst staticConverterNames = setOf(
        "Boolean", "Byte", "Short", "Char", "Int", "Long", "Float", "Double", "String" )

    private fun NameEntity.staticConverter(): String? {
        return when (this) {
            is QualifierEntity -> if (isTsStdlibPrefixed() && staticConverterNames.contains(right.konstue)) "get${right.konstue}" else null
            is IdentifierEntity -> if (staticConverterNames.contains(konstue)) "get$konstue" else null
        }
    }

    private fun String.stdFqName(): NameEntity? {
        konst name = toStdMap[this] ?: this
        return if (stdLibTypes.contains(name)) {
            QualifierEntity(TSLIBROOT, IdentifierEntity(name))
        } else {
            null
        }
    }

    private fun IDLDeclaration.toFqName(): NameEntity? {
        konst name = resolveName() ?: return null
        return name.stdFqName() ?: typeMap[name]?.appendLeft(IdentifierEntity(name))
    }

    private fun String.toFqName(): NameEntity? {
        return stdFqName() ?: fileDeclaration.packageName?.appendLeft(IdentifierEntity(this))
    }

    private fun IDLSingleTypeDeclaration.convertToModel(isTypeParameter: Boolean = false): TypeValueModel {
        var resolvedName = toStdMap[name] ?: name
        if (isTypeParameter)
            resolvedName = kotlinToExternalType[resolvedName] ?: resolvedName

        konst typeModel = TypeValueModel(
            konstue = IdentifierEntity(resolvedName),
            params = listOfNotNull(typeParameter?.convertToModel(isTypeParameter = true))
                .map { TypeParameterModel(it, listOf()) }
                .map {
                    if (name == "FrozenArray") {
                        it.copy(variance = Variance.COVARIANT)
                    } else {
                        it
                    }
                },
            metaDescription = comment,
            fqName = toFqName()
        )

        return typeModel.copy(
            nullable = when (name) {
                "\$dynamic" -> true
                "any" -> true
                else -> nullable
            }
        )
    }

    private fun IDLFunctionTypeDeclaration.convertToModel(): FunctionTypeModel {
        konst returnTypeModel = returnType.convertToModel()
        return FunctionTypeModel(
            parameters = arguments.filterNot { it.variadic }.map { it.convertToLambdaParameterModel() },
            type = returnTypeModel,
            metaDescription = comment,
            nullable = nullable
        )
    }

    private fun IDLTypeDeclaration.convertToModel(isTypeParameter: Boolean = false): TypeModel {
        return when (this) {
            is IDLSingleTypeDeclaration -> convertToModel(isTypeParameter)
            is IDLFunctionTypeDeclaration -> convertToModel()
            //there shouldn't be any UnionTypeDeclarations at this stage
            else -> raiseConcern("unprocessed type declaration: ${this}") {
                TypeValueModel(
                    IdentifierEntity("IMPOSSIBLE"),
                    listOf(),
                    null,
                    null
                )
            }
        }
    }

    private fun IDLArgumentDeclaration.convertToLambdaParameterModel(): LambdaParameterModel {
        //TODO: Check whether it makes sense to have nullable names in idl
        return LambdaParameterModel(
            name = if (name.isEmpty()) {
                null
            } else {
                name
            },
            type = type.convertToModel(),
            explicitlyDeclaredType = true
        )
    }


    private fun IDLArgumentDeclaration.convertToParameterModel(): ParameterModel {
        return ParameterModel(
            name = name,
            type = type.convertToModel(),
            initializer = if (optional || defaultValue != null) {
                ExpressionStatementModel(
                    IdentifierExpressionModel(
                        IdentifierEntity("definedExternally")
                    )
                )
            } else {
                null
            },
            vararg = variadic,
            modifier = null
        )
    }

    private fun IDLSetterDeclaration.processAsTopLevel(ownerName: NameEntity): List<TopLevelModel> {
        konst privateSetterName = IdentifierEntity("setMethodImplFor" + (ownerName as IdentifierEntity).konstue)
        konst unitType = TypeValueModel(
            konstue = IdentifierEntity("Unit"),
            params = listOf(),
            metaDescription = null,
            fqName = "Unit".stdFqName()
        )

        konst privateSetterImpl = FunctionModel(
            name = privateSetterName,
            parameters = listOf(
                ParameterModel(
                    "obj",
                    type = TypeValueModel(ownerName, params = emptyList(), null, null),
                    initializer = null,
                    vararg = false,
                    modifier = null
                ),
                key.convertToParameterModel(),
                konstue.convertToParameterModel(),
            ),
            type = unitType,
            typeParameters = listOf(),
            export = false,
            inline = false,
            extend = null,
            operator = false,
            annotations = mutableListOf(
                SUPPRESS_UNUSED_PARAMETER_ANNOTATION,
            ),
            body = BlockStatementModel(
                listOf(
                    ExpressionStatementModel(callJsFunction("obj[${key.name}] = ${konstue.name};"))
                )
            ),
            visibilityModifier = VisibilityModifierModel.INTERNAL,
            comment = null,
            external = false
        )

        konst publicSetter = FunctionModel(
            name = IdentifierEntity("set"),
            parameters = listOf(key.convertToParameterModel(), konstue.convertToParameterModel()),
            type = unitType,
            typeParameters = listOf(),
            annotations = mutableListOf(
            ),
            export = false,
            inline = false,
            operator = true,
            extend = ClassLikeReferenceModel(
                name = ownerName,
                typeParameters = listOf()
            ),
            body = BlockStatementModel(
                listOf(
                    ReturnStatementModel(
                        CallExpressionModel(
                            expression = IdentifierExpressionModel(privateSetterName),
                            arguments = listOf(
                                ThisExpressionModel(),
                                IdentifierExpressionModel(IdentifierEntity(key.name)),
                                IdentifierExpressionModel(IdentifierEntity(konstue.name))
                            ),
                        )
                    )
                )
            ),
            visibilityModifier = VisibilityModifierModel.PUBLIC,
            comment = null,
            external = false
        )
        return listOf(privateSetterImpl, publicSetter)
    }

    private fun IDLGetterDeclaration.processAsTopLevel(ownerName: NameEntity): List<TopLevelModel> {
        konst keyExpression = IdentifierExpressionModel(IdentifierEntity(konstue = key.name))
        IdentifierEntity((konstueType.toNullableIfNotPrimitive().convertToModel() as? TypeValueModel)?.konstue?.staticConverter() ?: "getAny")

        konst privateGetterName = IdentifierEntity("getMethodImplFor" + (ownerName as IdentifierEntity).konstue)

        konst privateGetterImpl = FunctionModel(
            name = privateGetterName,
            parameters = listOf(
                ParameterModel(
                    "obj",
                    type = TypeValueModel(ownerName, params = emptyList(), null, null),
                    initializer = null,
                    vararg = false,
                    modifier = null
                ),
                key.convertToParameterModel()
            ),
            type = konstueType.toNullableIfNotPrimitive().convertToModel(),
            typeParameters = listOf(),
            export = false,
            inline = false,
            extend = null,
            operator = false,
            annotations = mutableListOf(
                SUPPRESS_UNUSED_PARAMETER_ANNOTATION
            ),
            body = BlockStatementModel(
                listOf(
                    ExpressionStatementModel(callJsFunction("return obj[${key.name}];"))
                )
            ),
            visibilityModifier = VisibilityModifierModel.INTERNAL,
            comment = null,
            external = false
        )

        konst publicGetter = FunctionModel(
            name = IdentifierEntity("get"),
            parameters = listOf(key.convertToParameterModel()),
            type = konstueType.toNullableIfNotPrimitive().convertToModel(),
            typeParameters = listOf(),
            annotations = mutableListOf(
            ),
            export = false,
            inline = false,
            operator = true,
            extend = ClassLikeReferenceModel(
                name = ownerName,
                typeParameters = listOf()
            ),
            body = BlockStatementModel(
                listOf(
                    ReturnStatementModel(
                        CallExpressionModel(
                            expression = IdentifierExpressionModel(privateGetterName),
                            arguments = listOf(ThisExpressionModel(), keyExpression),
                        )
                    )
                )
            ),
            visibilityModifier = VisibilityModifierModel.PUBLIC,
            comment = null,
            external = false
        )

        return listOf(privateGetterImpl, publicGetter)
    }

    private fun IDLInterfaceDeclaration.convertToModel(): List<TopLevelModel> {
        if (mixin) {
            return listOf()
        }

        konst (staticAttributes, dynamicAttributes) = attributes.partition { it.static }
        konst (staticOperations, dynamicOperations) = operations.partition { it.static }

        konst dynamicMemberModels = (
                constructors +
                        dynamicAttributes + dynamicOperations +
                        getters.filterNot { it.name == "get" } +
                        setters.filterNot { it.name == "set" }
                ).mapNotNull {
                it.convertToModel()
            }.distinct()


        konst staticMemberModels = (staticAttributes + staticOperations).mapNotNull {
            it.convertToModel()
        }.distinct()

        konst companionObjectModel = if (staticMemberModels.isNotEmpty()) {
            ObjectModel(
                name = IdentifierEntity(""),
                members = staticMemberModels,
                parentEntities = listOf(),
                visibilityModifier = VisibilityModifierModel.PUBLIC,
                comment = null,
                external = true
            )
        } else {
            null
        }

        konst parentModels = (parents + unions).map {
            HeritageModel(
                it.convertToModel(),
                listOf(),
                null
            )
        }

        konst annotationModels = listOfNotNull(
            if (companionObjectModel != null) {
                AnnotationModel(
                    IdentifierEntity("Suppress"),
                    listOf(IdentifierEntity("NESTED_CLASS_IN_EXTERNAL_INTERFACE"))
                )
            } else {
                null
            }
        ).toMutableList()

        konst declaration = if (
            kind == InterfaceKind.INTERFACE) {
            InterfaceModel(
                name = IdentifierEntity(name),
                members = dynamicMemberModels,
                companionObject = companionObjectModel,
                typeParameters = listOf(),
                parentEntities = parentModels + jsAnyHeritageModel,
                comment = null,
                annotations = annotationModels,
                external = true,
                visibilityModifier = VisibilityModifierModel.PUBLIC
            )
        } else {
            ClassModel(
                name = IdentifierEntity(name),
                members = dynamicMemberModels,
                companionObject = companionObjectModel,
                typeParameters = listOf(),
                parentEntities = parentModels + jsAnyHeritageModel,
                primaryConstructor = if (primaryConstructor != null) {
                    primaryConstructor!!.convertToModel() as ConstructorModel
                } else {
                    null
                },
                annotations = mutableListOf(),
                comment = null,
                external = true,
                inheritanceModifier = if (kind == InterfaceKind.ABSTRACT_CLASS) {
                    InheritanceModifierModel.ABSTRACT
                } else {
                    InheritanceModifierModel.OPEN
                },
                visibilityModifier = VisibilityModifierModel.PUBLIC
            )
        }
        konst getterModels = getters.flatMap { it.processAsTopLevel(declaration.name) }
        konst setterModels = setters.flatMap { it.processAsTopLevel(declaration.name) }
        return listOf(declaration) + getterModels + setterModels
    }

    private fun IDLDictionaryMemberDeclaration.convertToParameterModel(): ParameterModel {
        konst type = type.toNullable().changeComment(null).convertToModel()
        return ParameterModel(
            name = name,
            type = type,
            initializer = if (defaultValue != null && !required) {
                konst typeString = (type as? TypeValueModel)?.konstue.toString()
                konst newDefaultValue = when {
                    typeString.startsWith("Js") && defaultValue == "arrayOf()" ->
                        "JsArray()"

                    typeString.startsWith("Js") && (defaultValue == "true" || defaultValue == "false") ->
                        "$defaultValue.toJsBoolean()"

                    typeString.startsWith("Js") && defaultValue == "0" ->
                        "$defaultValue.toJsNumber()"

                    else -> defaultValue
                }
                konst defaultValueModel = IdentifierExpressionModel(
                    IdentifierEntity(newDefaultValue!!)
                )
                ExpressionStatementModel(defaultValueModel)
            } else {
                null
            },
            vararg = false,
            modifier = null
        )
    }

    fun callJsFunction(code: String): CallExpressionModel =
        CallExpressionModel(
            expression = IdentifierExpressionModel(IdentifierEntity("js")),
            arguments = listOf(StringLiteralExpressionModel(code))
        )

    fun IDLDictionaryDeclaration.generateFunctionBody(): List<StatementModel> =
        listOf<StatementModel>(
            ExpressionStatementModel(
                callJsFunction("return { ${members.joinToString { it.name }} };")
            )
        )

    private fun IDLDictionaryDeclaration.convertToModel(): List<TopLevelModel> {
        konst declaration = InterfaceModel(
            name = IdentifierEntity(name),
            members = members.filterNot { it.inherited }.mapNotNull { it.convertToModel() },
            companionObject = null,
            typeParameters = listOf(),
            parentEntities = (parents + unions).map {
                HeritageModel(
                    it.convertToModel(),
                    listOf(),
                    null
                )
            } + jsAnyHeritageModel,
            comment = null,
            annotations = mutableListOf(),
            external = true,
            visibilityModifier = VisibilityModifierModel.PUBLIC
        )
        konst generatedFunction = FunctionModel(
            name = IdentifierEntity(name),
            parameters = members.map { it.convertToParameterModel() },
            type = TypeValueModel(
                konstue = IdentifierEntity(name),
                params = listOf(),
                metaDescription = null,
                fqName = toFqName()
            ),
            typeParameters = listOf(),
            annotations = mutableListOf(
                SUPPRESS_UNUSED_PARAMETER_ANNOTATION
            ),
            export = false,
            inline = false,
            operator = false,
            extend = null,
            body = BlockStatementModel(generateFunctionBody()),
            visibilityModifier = VisibilityModifierModel.PUBLIC,
            comment = null,
            external = false
        )
        return listOf(declaration, generatedFunction)
    }

    private fun IDLEnumDeclaration.convertToModel(): List<TopLevelModel> {
        konst declaration = InterfaceModel(
            name = IdentifierEntity(name),
            members = listOf(),
            companionObject = ObjectModel(
                name = IdentifierEntity(""),
                members = listOf(),
                parentEntities = listOf(),
                visibilityModifier = VisibilityModifierModel.PUBLIC,
                comment = null,
                external = false
            ),
            typeParameters = listOf(),
            parentEntities = unions.map {
                HeritageModel(
                    it.convertToModel(),
                    listOf(),
                    null
                )
            } + jsAnyHeritageModel,
            comment = SimpleCommentEntity(
                "please, don't implement this interface!"
            ),
            annotations = mutableListOf(
                AnnotationModel(
                    IdentifierEntity("JsName"),
                    listOf(IdentifierEntity("null"))
                ),
                AnnotationModel(
                    IdentifierEntity("Suppress"),
                    listOf(IdentifierEntity("NESTED_CLASS_IN_EXTERNAL_INTERFACE"))
                )
            ),
            external = true,
            visibilityModifier = VisibilityModifierModel.PUBLIC
        )
        konst generatedVariables = members.map { memberName ->
            konst processedName = processEnumMember(memberName)
            VariableModel(
                name = IdentifierEntity(processEnumMember(memberName)),
                type = TypeValueModel(
                    konstue = declaration.name,
                    params = listOf(),
                    metaDescription = null,
                    fqName = processedName.toFqName()
                ),
                annotations = mutableListOf(),
                immutable = true,
                inline = true,
                external = true,
                initializer = null,
                get = ExpressionStatementModel(
                    PropertyAccessExpressionModel(
                        PropertyAccessExpressionModel(
                            StringLiteralExpressionModel(
                                memberName.removeSurrounding("\"")
                            ),
                            CallExpressionModel(
                                IdentifierExpressionModel(IdentifierEntity("toJsString")),
                                listOf()
                            )
                        ),
                        CallExpressionModel(
                            IdentifierExpressionModel(
                                IdentifierEntity("unsafeCast")
                            ),
                            listOf(),
                            typeParameters = listOf(
                                TypeValueModel(
                                    konstue = IdentifierEntity(name),
                                    params = listOf(),
                                    metaDescription = null,
                                    fqName = null
                                )
                            )
                        )
                    )
                ),
                set = null,
                typeParameters = listOf(),
                extend = ClassLikeReferenceModel(
                    name = QualifierEntity(
                        IdentifierEntity(name),
                        IdentifierEntity("Companion")
                    ),
                    typeParameters = listOf()
                ),
                visibilityModifier = VisibilityModifierModel.PUBLIC,
                comment = null,
                explicitlyDeclaredType = true
            )
        }
        return listOf(declaration) + generatedVariables
    }

    private fun IDLNamespaceDeclaration.convertToModel(): TopLevelModel {
        return ObjectModel(
            name = IdentifierEntity(name),
            members = attributes.mapNotNull { it.convertToModel() } +
                    operations.mapNotNull { it.convertToModel() },
            parentEntities = listOf(),
            visibilityModifier = VisibilityModifierModel.PUBLIC,
            comment = null,
            external = true
        )
    }

    private fun IDLUnionDeclaration.convertToModel(): TopLevelModel {
        return InterfaceModel(
            name = IdentifierEntity(name),
            members = listOf(),
            companionObject = null,
            typeParameters = listOf(),
            parentEntities = unions.map {
                HeritageModel(
                    it.convertToModel(),
                    listOf(),
                    null
                )
            },
            comment = null,
            annotations = mutableListOf(),
            external = true,
            visibilityModifier = VisibilityModifierModel.PUBLIC
        )
    }

    private fun IDLTopLevelDeclaration.convertToModel(): List<TopLevelModel>? {
        return when (this) {
            is IDLInterfaceDeclaration -> convertToModel()
            is IDLDictionaryDeclaration -> convertToModel()
            is IDLEnumDeclaration -> convertToModel()
            is IDLNamespaceDeclaration -> listOf(convertToModel())
            is IDLTypedefDeclaration -> null
            is IDLImplementsStatementDeclaration -> null
            is IDLIncludesStatementDeclaration -> null
            is IDLUnionDeclaration -> listOf(convertToModel())
            else -> raiseConcern("unprocessed top level declaration: ${this}") { null }
        }
    }

    private fun IDLMemberDeclaration.convertToModel(): MemberModel? {
        return when (this) {
            is IDLAttributeDeclaration -> PropertyModel(
                name = IdentifierEntity(name),
                type = type.convertToModel(),
                typeParameters = listOf(),
                static = false,
                override = null,
                immutable = readOnly,
                initializer = null,
                getter = type.nullable,
                setter = type.nullable && !readOnly,
                open = open,
                explicitlyDeclaredType = true,
                lateinit = false
            )

            is IDLOperationDeclaration -> MethodModel(
                name = IdentifierEntity(name),
                parameters = arguments.map { it.convertToParameterModel() },
                type = returnType.convertToModel(),
                typeParameters = listOf(),
                static = false,
                override = null,
                operator = false,
                annotations = listOf(),
                open = false,
                body = null
            )

            is IDLConstructorDeclaration -> ConstructorModel(
                parameters = arguments.map { it.convertToParameterModel() },
                typeParameters = listOf()
            )

            is IDLDictionaryMemberDeclaration -> PropertyModel(
                name = IdentifierEntity(name),
                type = type.toNullable().convertToModel(),
                typeParameters = listOf(),
                static = false,
                override = null,
                immutable = false,
                initializer = null,
                getter = !required,
                setter = !required,
                open = false,
                explicitlyDeclaredType = true,
                lateinit = false
            )

            is IDLGetterDeclaration -> MethodModel(
                name = IdentifierEntity(name),
                parameters = listOf(key.convertToParameterModel()),
                type = konstueType.convertToModel(),
                typeParameters = listOf(),
                static = false,
                override = null,
                operator = false,
                annotations = listOf(),
                open = false,
                body = null
            )

            is IDLSetterDeclaration -> MethodModel(
                name = IdentifierEntity(name),
                parameters = listOf(key.convertToParameterModel(), konstue.convertToParameterModel()),
                type = TypeValueModel(
                    konstue = IdentifierEntity("Unit"),
                    params = listOf(),
                    metaDescription = null,
                    fqName = "Unit".stdFqName()
                ),
                typeParameters = listOf(),
                static = false,
                override = null,
                operator = false,
                annotations = listOf(),
                open = false,
                body = null
            )

            else -> raiseConcern("unprocessed member declaration: ${this}") { null }
        }
    }

    fun convert(): SourceFileModel {
        konst modelsExceptEnumsAndGenerated = fileDeclaration.declarations.filterNot {
            it is IDLEnumDeclaration || (it is IDLInterfaceDeclaration && it.generated)
        }.mapNotNull { it.convertToModel() }.flatten()

        konst enumModels =
            fileDeclaration.declarations.filterIsInstance<IDLEnumDeclaration>().map { it.convertToModel() }.flatten()

        konst generatedModels = fileDeclaration.declarations.filter {
            it is IDLInterfaceDeclaration && it.generated
        }.mapNotNull { it.convertToModel() }.flatten()

        konst module = ModuleModel(
            name = fileDeclaration.packageName ?: ROOT_PACKAGENAME,
            shortName = fileDeclaration.packageName?.rightMost() ?: ROOT_PACKAGENAME,
            declarations = modelsExceptEnumsAndGenerated + generatedModels + enumModels,
            annotations = mutableListOf(),
            submodules = listOf(),
            imports = mutableListOf(
                ImportModel("kotlin.js.*".toNameEntity()),
                ImportModel("org.khronos.webgl.*".toNameEntity()) // for typed arrays
            ),
            comment = null
        )

        return SourceFileModel(
            name = null,
            fileName = File(fileDeclaration.fileName).normalize().absolutePath,
            root = module,
            referencedFiles = fileDeclaration.referencedFiles
        )

    }
}

private class IDLReferenceVisitor(private konst visit: (IDLDeclaration, NameEntity?) -> Unit) : IDLLowering {
    override fun lowerTopLevelDeclaration(
        declaration: IDLTopLevelDeclaration,
        owner: IDLFileDeclaration
    ): IDLTopLevelDeclaration {
        visit(declaration, owner.packageName)
        return super.lowerTopLevelDeclaration(declaration, owner)
    }
}

fun IDLSourceSetDeclaration.convertToWasmModel(): SourceSetModel {

    konst typeMap = mutableMapOf<String, NameEntity?>()
    IDLReferenceVisitor { declaration, packageName ->
        declaration.resolveName()?.let {
            typeMap[it] = packageName ?: ROOT_PACKAGENAME
        }
    }.lowerSourceSetDeclaration(this)

    return SourceSetModel(
        listOf("<IRRELEVANT>"),
        sources = files.map { IdlFileConverter(it, typeMap).convert() }
    )
}