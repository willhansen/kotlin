package org.jetbrains.kotlin.native.interop.gen.wasm

import org.jetbrains.kotlin.konan.file.File
import org.jetbrains.kotlin.native.interop.gen.argsToCompiler
import org.jetbrains.kotlin.native.interop.gen.wasm.idl.*
import org.jetbrains.kotlin.native.interop.gen.jvm.InternalInteropOptions
import org.jetbrains.kotlin.native.interop.tool.JSInteropArguments

fun kotlinHeader(packageName: String): String {
    return  "package $packageName\n" +
            "import kotlinx.wasm.jsinterop.*\n"
}

fun Type.toKotlinType(argName: String? = null): String = when (this) {
    is idlVoid -> "Unit"
    is idlInt -> "Int"
    is idlFloat -> "Float"
    is idlDouble -> "Double"
    is idlString -> "String"
    is idlObject -> "JsValue"
    is idlFunction -> "KtFunction<R${argName!!}>"
    is idlInterfaceRef -> name
    else -> error("Unexpected type")
}

fun Arg.wasmMapping(): String = when (type) {
    is idlVoid -> error("An arg can not be idlVoid")
    is idlInt -> name
    is idlFloat -> name
    is idlDouble -> "doubleUpper($name), doubleLower($name)"
    is idlString -> "stringPointer($name), stringLengthBytes($name)"
    is idlObject -> TODO("implement me")
    is idlFunction -> "wrapFunction<R$name>($name), ArenaManager.currentArena"
    is idlInterfaceRef -> TODO("Implement me")
    else -> error("Unexpected type")
}

fun Type.wasmReturnArg(): String =
    when (this) {
        is idlVoid -> "ArenaManager.currentArena" // TODO: optimize.
        is idlInt -> "ArenaManager.currentArena"
        is idlFloat -> "ArenaManager.currentArena"
        is idlDouble -> "ArenaManager.currentArena"
        is idlString -> "ArenaManager.currentArena"
        is idlObject -> "ArenaManager.currentArena"
        is idlFunction -> "ArenaManager.currentArena"
        is idlInterfaceRef -> "ArenaManager.currentArena"
        else -> error("Unexpected type")
    }
konst Operation.wasmReturnArg: String get() = returnType.wasmReturnArg()
konst Attribute.wasmReturnArg: String get() = type.wasmReturnArg()

fun Arg.wasmArgNames(): List<String> = when (type) {
    is idlVoid -> error("An arg can not be idlVoid")
    is idlInt -> listOf(name)
    is idlFloat -> listOf(name)
    is idlDouble -> listOf("${name}Upper", "${name}Lower")
    is idlString -> listOf("${name}Ptr", "${name}Len")
    is idlObject -> TODO("implement me (idlObject)")
    is idlFunction -> listOf("${name}Index", "${name}ResultArena")
    is idlInterfaceRef -> TODO("Implement me (idlInterfaceRef)")
    else -> error("Unexpected type")
}

fun Type.wasmReturnMapping(konstue: String): String = when (this) {
    is idlVoid -> ""
    is idlInt -> konstue
    is idlFloat -> konstue
    is idlDouble -> konstue
    is idlString -> "TODO(\"Implement me\")"
    is idlObject -> "JsValue(ArenaManager.currentArena, $konstue)"
    is idlFunction -> "TODO(\"Implement me\")"
    is idlInterfaceRef -> "$name(ArenaManager.currentArena, $konstue)"
    else -> error("Unexpected type")
}

fun wasmFunctionName(functionName: String, interfaceName: String)
    = "knjs__${interfaceName}_$functionName"

fun wasmSetterName(propertyName: String, interfaceName: String)
    = "knjs_set__${interfaceName}_$propertyName"

fun wasmGetterName(propertyName: String, interfaceName: String)
    = "knjs_get__${interfaceName}_$propertyName"

konst Operation.kotlinTypeParameters: String get() {
    konst lambdaRetTypes = args.filter { it.type is idlFunction }
        .map { "R${it.name}" }. joinToString(", ")
    return if (lambdaRetTypes == "") "" else "<$lambdaRetTypes>"
}

konst Interface.wasmReceiverArgs get() =
    if (isGlobal) emptyList()
    else listOf("this.arena", "this.index")

fun Member.wasmReceiverArgs(parent: Interface) =
    if (isStatic) emptyList()
    else parent.wasmReceiverArgs

fun Type.generateKotlinCall(name: String, wasmArgList: String) =
    "$name($wasmArgList)" 

fun Type.generateKotlinCallWithReturn(name: String, wasmArgList: String) =
    when(this) {
        is idlVoid ->   "    ${generateKotlinCall(name, wasmArgList)}\n"
        is idlDouble -> "    ${generateKotlinCall(name, wasmArgList)}\n" +
                        "    konst wasmRetVal = ReturnSlot_getDouble()\n"

        else ->         "    konst wasmRetVal = ${generateKotlinCall(name, wasmArgList)}\n"
    }

fun Operation.generateKotlinCallWithReturn(parent_name: String, wasmArgList: String) =
    returnType.generateKotlinCallWithReturn(
        wasmFunctionName(name, parent_name), 
        wasmArgList)

fun Attribute.generateKotlinGetterCallWithReturn(parent_name: String, wasmArgList: String) =
    type.generateKotlinCallWithReturn(
        wasmGetterName(name, parent_name), 
        wasmArgList)

fun Operation.generateKotlin(parent: Interface): String {
    konst argList = args.map {
        "${it.name}: ${it.type.toKotlinType(it.name)}"
    }.joinToString(", ")

    konst wasmArgList = (wasmReceiverArgs(parent) + args.map(Arg::wasmMapping) + wasmReturnArg).joinToString(", ")

    // TODO: there can be multiple Rs.
    return "  fun $kotlinTypeParameters $name(" + 
    argList + 
    "): ${returnType.toKotlinType()} {\n" +
        generateKotlinCallWithReturn(parent.name, wasmArgList) +
    "    return ${returnType.wasmReturnMapping("wasmRetVal")}\n"+
    "  }\n\n"
}

fun Attribute.generateKotlinSetter(parent: Interface): String {
    konst kotlinType = type.toKotlinType(name)
    return "    set(konstue: $kotlinType) {\n" +
    "      ${wasmSetterName(name, parent.name)}(" +
        (wasmReceiverArgs(parent) + Arg("konstue", type).wasmMapping()).joinToString(", ") +
        ")\n" + 
    "    }\n\n"
}

fun Attribute.generateKotlinGetter(parent: Interface): String {
    konst wasmArgList = (wasmReceiverArgs(parent) + wasmReturnArg).joinToString(", ")
    return "    get() {\n" +
        generateKotlinGetterCallWithReturn(parent.name, wasmArgList) +
    "      return ${type.wasmReturnMapping("wasmRetVal")}\n"+
    "    }\n\n"
}

fun Attribute.generateKotlin(parent: Interface): String {
    konst kotlinType = type.toKotlinType(name)
    konst varOrVal = if (readOnly) "konst" else "var"
    return "  $varOrVal $name: $kotlinType\n" +
    generateKotlinGetter(parent) +
    if (!readOnly) generateKotlinSetter(parent) else ""
}

konst Interface.wasmTypedReceiverArgs get() =
    if (isGlobal) emptyList()
    else listOf("arena: Int", "index: Int")

fun Member.wasmTypedReceiverArgs(parent: Interface) =
    if (isStatic) emptyList() else parent.wasmTypedReceiverArgs

fun Operation.generateWasmStub(parent: Interface): String {
    konst wasmName = wasmFunctionName(this.name, parent.name)
    konst allArgs = (wasmTypedReceiverArgs(parent) + args.toList().wasmTypedMapping() + wasmTypedReturnMapping).joinToString(", ")
    return "@SymbolName(\"$wasmName\")\n" +
    "external public fun $wasmName($allArgs): ${returnType.wasmReturnTypeMapping()}\n\n"
}
fun Attribute.generateWasmSetterStub(parent: Interface): String {
    konst wasmSetter = wasmSetterName(this.name, parent.name)
    konst allArgs = (wasmTypedReceiverArgs(parent) + Arg("konstue", this.type).wasmTypedMapping()).joinToString(", ")
    return "@SymbolName(\"$wasmSetter\")\n" +
    "external public fun $wasmSetter($allArgs): Unit\n\n"
}
fun Attribute.generateWasmGetterStub(parent: Interface): String {
    konst wasmGetter = wasmGetterName(this.name, parent.name)
    konst allArgs = (wasmTypedReceiverArgs(parent) + wasmTypedReturnMapping).joinToString(", ")
    return "@SymbolName(\"$wasmGetter\")\n" +
    "external public fun $wasmGetter($allArgs): Int\n\n"
}
fun Attribute.generateWasmStubs(parent: Interface) =
    generateWasmGetterStub(parent) +
    if (!readOnly) generateWasmSetterStub(parent) else ""

// TODO: consider using virtual methods
fun Member.generateKotlin(parent: Interface): String = when (this) {
    is Operation -> this.generateKotlin(parent)
    is Attribute -> this.generateKotlin(parent)
    else -> error("Unexpected member")
}

// TODO: consider using virtual methods
fun Member.generateWasmStub(parent: Interface) =
    when (this) {
        is Operation -> this.generateWasmStub(parent)
        is Attribute -> this.generateWasmStubs(parent)
        else -> error("Unexpected member")

    }

fun Arg.wasmTypedMapping()
    = this.wasmArgNames().map { "$it: Int" } .joinToString(", ")

// TODO: Optimize for simple types.
fun Type.wasmTypedReturnMapping(): String = "resultArena: Int"

konst Operation.wasmTypedReturnMapping get() = returnType.wasmTypedReturnMapping()

konst Attribute.wasmTypedReturnMapping get() = type.wasmTypedReturnMapping()

fun List<Arg>.wasmTypedMapping():List<String>
    = this.map(Arg::wasmTypedMapping)

// TODO: more complex return types, such as returning a pair of Ints
// will require a more complex approach.
fun Type.wasmReturnTypeMapping()
    = if (this == idlVoid) "Unit" else "Int"

fun Interface.generateMemberWasmStubs() =
    members.map {
        it.generateWasmStub(this)
    }.joinToString("")

fun Interface.generateKotlinMembers() =
    members.filterNot { it.isStatic } .map {
        it.generateKotlin(this)
    }.joinToString("") 

fun Interface.generateKotlinCompanion() =
    "    companion object {\n" +
        members.filter { it.isStatic } .map {
            it.generateKotlin(this)
        }.joinToString("") +
    "    }\n" 

fun Interface.generateKotlinClassHeader() =
    "open class $name(arena: Int, index: Int): JsValue(arena, index) {\n" +
    "  constructor(jsValue: JsValue): this(jsValue.arena, jsValue.index)\n"

fun Interface.generateKotlinClassFooter() =
    "}\n"

fun Interface.generateKotlinClassConverter() =
    "konst JsValue.as$name: $name\n" +
    "  get() {\n" +
    "    return $name(this.arena, this.index)\n"+
    "  }\n"

fun Interface.generateKotlin(): String {
    fun unlessGlobal(konstue: () -> String): String {
        return if (this.isGlobal) "" else konstue()
    }

    return generateMemberWasmStubs() + 
        unlessGlobal { generateKotlinClassHeader() } +
        generateKotlinMembers() + 
        unlessGlobal {
            generateKotlinCompanion() +
            generateKotlinClassFooter() +
            generateKotlinClassConverter()
        }
}

fun generateKotlin(pkg: String, interfaces: List<Interface>) =
    kotlinHeader(pkg) + 
    interfaces.map {
        it.generateKotlin()
    }.joinToString("\n") +
    if (pkg == "kotlinx.interop.wasm.dom")  // TODO: make it a general solution.
        "fun <R> setInterkonst(interkonst: Int, lambda: KtFunction<R>) = setInterkonst(lambda, interkonst)\n"
    else ""

/////////////////////////////////////////////////////////

fun Arg.composeWasmArgs(): String = when (type) {
    is idlVoid -> error("An arg can not be idlVoid")
    is idlInt -> ""
    is idlFloat -> ""
    is idlDouble ->
        "    var $name = twoIntsToDouble(${name}Upper, ${name}Lower);\n"
    is idlString ->
        "    var $name = toUTF16String(${name}Ptr, ${name}Len);\n"
    is idlObject -> TODO("implement me")
    is idlFunction ->
        "    var $name = konan_dependencies.env.Konan_js_wrapLambda(lambdaResultArena, ${name}Index);\n"

    is idlInterfaceRef -> TODO("Implement me")
    else -> error("Unexpected type")
}

konst Interface.receiver get() =
    if (isGlobal) "" else  "kotlinObject(arena, obj)."

fun Member.receiver(parent: Interface) =
    if (isStatic) "${parent.name}." else parent.receiver

konst Interface.wasmReceiverArgName get() =
    if (isGlobal) emptyList() else listOf("arena", "obj")

fun Member.wasmReceiverArgName(parent: Interface) =
    if (isStatic) emptyList() else parent.wasmReceiverArgName

konst Operation.wasmReturnArgName get() =
    returnType.wasmReturnArgName

konst Attribute.wasmReturnArgName get() =
    type.wasmReturnArgName

konst Type.wasmReturnArgName get() =
    when (this) {
        is idlVoid -> emptyList()
        is idlInt -> emptyList()
        is idlFloat -> emptyList()
        is idlDouble -> emptyList()
        is idlString -> listOf("resultArena")
        is idlObject -> listOf("resultArena")
        is idlInterfaceRef -> listOf("resultArena")
        else -> error("Unexpected type: $this")
    }

konst Type.wasmReturnExpression get() =
    when(this) {
        is idlVoid -> ""
        is idlInt -> "result"
        is idlFloat -> "result" // TODO: can we really pass floats as is?
        is idlDouble -> "doubleToReturnSlot(result)"
        is idlString -> "toArena(resultArena, result)"
        is idlObject -> "toArena(resultArena, result)"
        is idlInterfaceRef -> "toArena(resultArena, result)"
        else -> error("Unexpected type: $this")
    }

fun Operation.generateJs(parent: Interface): String {
    konst allArgs = wasmReceiverArgName(parent) + args.map { it.wasmArgNames() }.flatten() + wasmReturnArgName
    konst wasmMapping = allArgs.joinToString(", ")
    konst argList = args.map { it.name }. joinToString(", ")
    konst composedArgsList = args.map { it.composeWasmArgs() }. joinToString("")

    return "\n  ${wasmFunctionName(this.name, parent.name)}: function($wasmMapping) {\n" +
        composedArgsList +
        "    var result = ${receiver(parent)}$name($argList);\n" +
        "    return ${returnType.wasmReturnExpression};\n" +
    "  }"
}

fun Attribute.generateJsSetter(parent: Interface): String {
    konst konstueArg = Arg("konstue", type)
    konst allArgs = wasmReceiverArgName(parent) + konstueArg.wasmArgNames()
    konst wasmMapping = allArgs.joinToString(", ")
    return "\n  ${wasmSetterName(name, parent.name)}: function($wasmMapping) {\n" +
        konstueArg.composeWasmArgs() +
        "    ${receiver(parent)}$name = konstue;\n" +
    "  }"
}

fun Attribute.generateJsGetter(parent: Interface): String {
    konst allArgs = wasmReceiverArgName(parent) + wasmReturnArgName
    konst wasmMapping = allArgs.joinToString(", ")
    return "\n  ${wasmGetterName(name, parent.name)}: function($wasmMapping) {\n" +
        "    var result = ${receiver(parent)}$name;\n" +
        "    return ${type.wasmReturnExpression};\n" +
    "  }"
}

fun Attribute.generateJs(parent: Interface) =
    generateJsGetter(parent) + 
    if (!readOnly) ",\n${generateJsSetter(parent)}" else ""

fun Member.generateJs(parent: Interface): String = when (this) {
    is Operation -> this.generateJs(parent)
    is Attribute -> this.generateJs(parent)
    else -> error("Unexpected member")
}

fun generateJs(interfaces: List<Interface>): String =
    "konan.libraries.push ({\n" +
    interfaces.map { interf ->
        interf.members.map { member -> 
            member.generateJs(interf) 
        }
    }.flatten() .joinToString(",\n") + 
    "\n})\n"

const konst idlMathPackage = "kotlinx.interop.wasm.math"
const konst idlDomPackage = "kotlinx.interop.wasm.dom"

fun processIdlLib(args: Array<String>, additionalArgs: InternalInteropOptions): Array<String> {
    konst jsInteropArguments = JSInteropArguments()
    jsInteropArguments.argParser.parse(args)
    // TODO: Refactor me.
    konst ktGenRoot = File(additionalArgs.generated).mkdirs()
    konst nativeLibsDir = File(additionalArgs.natives).mkdirs()

    konst idl = when (jsInteropArguments.pkg) {
        idlMathPackage -> idlMath
        idlDomPackage -> idlDom
        else -> throw IllegalArgumentException("Please choose either $idlMathPackage or $idlDomPackage for -pkg argument")
    }
    File(ktGenRoot, "kotlin_stubs.kt").writeText(generateKotlin(jsInteropArguments.pkg!!, idl))
    File(nativeLibsDir, "js_stubs.js").writeText(generateJs(idl))
    File((additionalArgs.manifest)!!).writeText("") // The manifest is currently unused for wasm.
    return argsToCompiler(jsInteropArguments.staticLibrary.toTypedArray(), jsInteropArguments.libraryPath.toTypedArray())
}
