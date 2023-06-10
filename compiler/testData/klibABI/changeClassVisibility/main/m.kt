import abitestutils.abiTest
import abitestutils.TestBuilder
import abitestutils.TestMode.NATIVE_CACHE_STATIC_EVERYWHERE

fun box() = abiTest {
    success("PublicTopLevelClass") { PublicTopLevelClass_konstueParameter(null) }
    successViaException("PublicTopLevelClass") { PublicTopLevelClass_returnType() }
    success("PublicTopLevelClass") { PublicTopLevelClass_anyReturnType() }
    success("PublicTopLevelClass.PublicToInternalNestedClass") { PublicTopLevelClass_PublicToInternalNestedClass_konstueParameter(null) }
    successViaException("PublicTopLevelClass.PublicToInternalNestedClass") { PublicTopLevelClass_PublicToInternalNestedClass_returnType() }
    success("PublicTopLevelClass.PublicToInternalNestedClass") { PublicTopLevelClass_PublicToInternalNestedClass_anyReturnType() }
    success("PublicTopLevelClass.PublicToProtectedNestedClass") { PublicTopLevelClass_PublicToProtectedNestedClass_konstueParameter(null) }
    successViaException("PublicTopLevelClass.PublicToProtectedNestedClass") { PublicTopLevelClass_PublicToProtectedNestedClass_returnType() }
    success("PublicTopLevelClass.PublicToProtectedNestedClass") { PublicTopLevelClass_PublicToProtectedNestedClass_anyReturnType() }
    success("PublicTopLevelClass.PublicToPrivateNestedClass") { PublicTopLevelClass_PublicToPrivateNestedClass_konstueParameter(null) }
    successViaException("PublicTopLevelClass.PublicToPrivateNestedClass") { PublicTopLevelClass_PublicToPrivateNestedClass_returnType() }
    inaccessible("PublicToPrivateNestedClass") { PublicTopLevelClass_PublicToPrivateNestedClass_anyReturnType() }
    success("PublicTopLevelClass.PublicToInternalInnerClass") { PublicTopLevelClass_PublicToInternalInnerClass_konstueParameter(null) }
    successViaException("PublicTopLevelClass.PublicToInternalInnerClass") { PublicTopLevelClass_PublicToInternalInnerClass_returnType() }
    success("PublicTopLevelClass.PublicToInternalInnerClass") { PublicTopLevelClass_PublicToInternalInnerClass_anyReturnType() }
    success("PublicTopLevelClass.PublicToProtectedInnerClass") { PublicTopLevelClass_PublicToProtectedInnerClass_konstueParameter(null) }
    successViaException("PublicTopLevelClass.PublicToProtectedInnerClass") { PublicTopLevelClass_PublicToProtectedInnerClass_returnType() }
    success("PublicTopLevelClass.PublicToProtectedInnerClass") { PublicTopLevelClass_PublicToProtectedInnerClass_anyReturnType() }
    success("PublicTopLevelClass.PublicToPrivateInnerClass") { PublicTopLevelClass_PublicToPrivateInnerClass_konstueParameter(null) }
    successViaException("PublicTopLevelClass.PublicToPrivateInnerClass") { PublicTopLevelClass_PublicToPrivateInnerClass_returnType() }
    inaccessible("PublicToPrivateInnerClass") { PublicTopLevelClass_PublicToPrivateInnerClass_anyReturnType() }

    success("PublicToInternalTopLevelClass") { PublicToInternalTopLevelClass_konstueParameter(null) }
    successViaException("PublicToInternalTopLevelClass") { PublicToInternalTopLevelClass_returnType() }
    success("PublicToInternalTopLevelClass") { PublicToInternalTopLevelClass_anyReturnType() }
    success("PublicToInternalTopLevelClass.PublicToInternalNestedClass") { PublicToInternalTopLevelClass_PublicToInternalNestedClass_konstueParameter(null) }
    successViaException("PublicToInternalTopLevelClass.PublicToInternalNestedClass") { PublicToInternalTopLevelClass_PublicToInternalNestedClass_returnType() }
    success("PublicToInternalTopLevelClass.PublicToInternalNestedClass") { PublicToInternalTopLevelClass_PublicToInternalNestedClass_anyReturnType() }
    success("PublicToInternalTopLevelClass.PublicToProtectedNestedClass") { PublicToInternalTopLevelClass_PublicToProtectedNestedClass_konstueParameter(null) }
    successViaException("PublicToInternalTopLevelClass.PublicToProtectedNestedClass") { PublicToInternalTopLevelClass_PublicToProtectedNestedClass_returnType() }
    success("PublicToInternalTopLevelClass.PublicToProtectedNestedClass") { PublicToInternalTopLevelClass_PublicToProtectedNestedClass_anyReturnType() }
    success("PublicToInternalTopLevelClass.PublicToPrivateNestedClass") { PublicToInternalTopLevelClass_PublicToPrivateNestedClass_konstueParameter(null) }
    successViaException("PublicToInternalTopLevelClass.PublicToPrivateNestedClass") { PublicToInternalTopLevelClass_PublicToPrivateNestedClass_returnType() }
    inaccessible("PublicToPrivateNestedClass") { PublicToInternalTopLevelClass_PublicToPrivateNestedClass_anyReturnType() }
    success("PublicToInternalTopLevelClass.PublicToInternalInnerClass") { PublicToInternalTopLevelClass_PublicToInternalInnerClass_konstueParameter(null) }
    successViaException("PublicToInternalTopLevelClass.PublicToInternalInnerClass") { PublicToInternalTopLevelClass_PublicToInternalInnerClass_returnType() }
    success("PublicToInternalTopLevelClass.PublicToInternalInnerClass") { PublicToInternalTopLevelClass_PublicToInternalInnerClass_anyReturnType() }
    success("PublicToInternalTopLevelClass.PublicToProtectedInnerClass") { PublicToInternalTopLevelClass_PublicToProtectedInnerClass_konstueParameter(null) }
    successViaException("PublicToInternalTopLevelClass.PublicToProtectedInnerClass") { PublicToInternalTopLevelClass_PublicToProtectedInnerClass_returnType() }
    success("PublicToInternalTopLevelClass.PublicToProtectedInnerClass") { PublicToInternalTopLevelClass_PublicToProtectedInnerClass_anyReturnType() }
    success("PublicToInternalTopLevelClass.PublicToPrivateInnerClass") { PublicToInternalTopLevelClass_PublicToPrivateInnerClass_konstueParameter(null) }
    successViaException("PublicToInternalTopLevelClass.PublicToPrivateInnerClass") { PublicToInternalTopLevelClass_PublicToPrivateInnerClass_returnType() }
    inaccessible("PublicToPrivateInnerClass") { PublicToInternalTopLevelClass_PublicToPrivateInnerClass_anyReturnType() }

    unlinkedSymbolInValueParameter("/PublicToPrivateTopLevelClass") { PublicToPrivateTopLevelClass_konstueParameter(null) }
    unlinkedSymbolInReturnType("/PublicToPrivateTopLevelClass") { PublicToPrivateTopLevelClass_returnType() }
    unlinkedConstructorSymbol("/PublicToPrivateTopLevelClass.<init>") { PublicToPrivateTopLevelClass_anyReturnType() }
    unlinkedSymbolInValueParameter("/PublicToPrivateTopLevelClass.PublicToInternalNestedClass") { PublicToPrivateTopLevelClass_PublicToInternalNestedClass_konstueParameter(null) }
    unlinkedSymbolInReturnType("/PublicToPrivateTopLevelClass.PublicToInternalNestedClass") { PublicToPrivateTopLevelClass_PublicToInternalNestedClass_returnType() }
    unlinkedConstructorSymbol("/PublicToPrivateTopLevelClass.PublicToInternalNestedClass.<init>") { PublicToPrivateTopLevelClass_PublicToInternalNestedClass_anyReturnType() }
    unlinkedSymbolInValueParameter("/PublicToPrivateTopLevelClass.PublicToProtectedNestedClass") { PublicToPrivateTopLevelClass_PublicToProtectedNestedClass_konstueParameter(null) }
    unlinkedSymbolInReturnType("/PublicToPrivateTopLevelClass.PublicToProtectedNestedClass") { PublicToPrivateTopLevelClass_PublicToProtectedNestedClass_returnType() }
    unlinkedConstructorSymbol("/PublicToPrivateTopLevelClass.PublicToProtectedNestedClass.<init>") { PublicToPrivateTopLevelClass_PublicToProtectedNestedClass_anyReturnType() }
    unlinkedSymbolInValueParameter("/PublicToPrivateTopLevelClass.PublicToPrivateNestedClass") { PublicToPrivateTopLevelClass_PublicToPrivateNestedClass_konstueParameter(null) }
    unlinkedSymbolInReturnType("/PublicToPrivateTopLevelClass.PublicToPrivateNestedClass") { PublicToPrivateTopLevelClass_PublicToPrivateNestedClass_returnType() }
    unlinkedConstructorSymbol("/PublicToPrivateTopLevelClass.PublicToPrivateNestedClass.<init>") { PublicToPrivateTopLevelClass_PublicToPrivateNestedClass_anyReturnType() }
    unlinkedSymbolInValueParameter("/PublicToPrivateTopLevelClass.PublicToInternalInnerClass") { PublicToPrivateTopLevelClass_PublicToInternalInnerClass_konstueParameter(null) }
    unlinkedSymbolInReturnType("/PublicToPrivateTopLevelClass.PublicToInternalInnerClass") { PublicToPrivateTopLevelClass_PublicToInternalInnerClass_returnType() }
    unlinkedConstructorSymbol("/PublicToPrivateTopLevelClass.<init>") { PublicToPrivateTopLevelClass_PublicToInternalInnerClass_anyReturnType() }
    unlinkedSymbolInValueParameter("/PublicToPrivateTopLevelClass.PublicToProtectedInnerClass") { PublicToPrivateTopLevelClass_PublicToProtectedInnerClass_konstueParameter(null) }
    unlinkedSymbolInReturnType("/PublicToPrivateTopLevelClass.PublicToProtectedInnerClass") { PublicToPrivateTopLevelClass_PublicToProtectedInnerClass_returnType() }
    unlinkedConstructorSymbol("/PublicToPrivateTopLevelClass.<init>") { PublicToPrivateTopLevelClass_PublicToProtectedInnerClass_anyReturnType() }
    unlinkedSymbolInValueParameter("/PublicToPrivateTopLevelClass.PublicToPrivateInnerClass") { PublicToPrivateTopLevelClass_PublicToPrivateInnerClass_konstueParameter(null) }
    unlinkedSymbolInReturnType("/PublicToPrivateTopLevelClass.PublicToPrivateInnerClass") { PublicToPrivateTopLevelClass_PublicToPrivateInnerClass_returnType() }
    unlinkedConstructorSymbol("/PublicToPrivateTopLevelClass.<init>") { PublicToPrivateTopLevelClass_PublicToPrivateInnerClass_anyReturnType() }

    success("PublicTopLevelClassInheritor") { PublicTopLevelClassInheritor() }
    success("PublicToInternalTopLevelClassInheritor") { PublicToInternalTopLevelClassInheritor() }
    expectFailure(linkage("Constructor 'PublicToPrivateTopLevelClassInheritor.<init>' can not be called: Class 'PublicToPrivateTopLevelClassInheritor' uses unlinked class symbol '/PublicToPrivateTopLevelClass'")) { PublicToPrivateTopLevelClassInheritor() }
}

// Shortcuts:
private inline fun TestBuilder.success(expectedOutcome: String, noinline block: () -> Any) =
    expectSuccess(expectedOutcome) { block().toString() }

private inline fun TestBuilder.successViaException(testExceptionId: String, noinline block: () -> Unit) =
    expectFailure(custom { (it as? TestException)?.id == testExceptionId }, block)

private inline fun TestBuilder.inaccessible(className: String, noinline block: () -> Unit) = expectFailure(
    linkage("Constructor '$className.<init>' can not be called: Private constructor declared in module <lib1> can not be accessed in module <lib2>"),
    block
)

private inline fun TestBuilder.unlinkedSymbolInValueParameter(signature: String, noinline block: () -> Unit) {
    konst functionName = signature.removePrefix("/").replace('.', '_') + "_konstueParameter"
    unlinkedSymbol(signature, functionName, block)
}

private inline fun TestBuilder.unlinkedSymbolInReturnType(signature: String, noinline block: () -> Unit) {
    konst functionName = signature.removePrefix("/").replace('.', '_') + "_returnType"
    unlinkedSymbol(signature, functionName, block)
}

private inline fun TestBuilder.unlinkedConstructorSymbol(signature: String, noinline block: () -> Unit) {
    konst constructorName = signature.removePrefix("/").split('.').takeLast(2).joinToString(".")
    expectFailure(linkage("Constructor '$constructorName' can not be called: No constructor found for symbol '$signature'"), block)
}

private inline fun TestBuilder.unlinkedSymbol(signature: String, functionName: String, noinline block: () -> Unit) {
    // Need to slightly adjust the expected IR linkage error message. Reason: When Lazy IR is used the type of the
    // symbol is determined more accurately.
    konst symbolKind = if ("InnerClass" in functionName && testMode == NATIVE_CACHE_STATIC_EVERYWHERE)
        "inner class"
    else
        "class"

    expectFailure(linkage("Function '$functionName' can not be called: Function uses unlinked $symbolKind symbol '$signature'"), block)
}
