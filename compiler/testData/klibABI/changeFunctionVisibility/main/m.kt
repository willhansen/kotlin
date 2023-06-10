import abitestutils.abiTest
import abitestutils.TestBuilder
import abitestutils.TestMode.NATIVE_CACHE_STATIC_EVERYWHERE

fun box() = abiTest {
    konst c = Container()
    konst ci = ContainerImpl()

    success("publicToInternalTopLevelFunction.v2") { publicToInternalTopLevelFunction() } // Signature remains the same.
    success("publicToInternalPATopLevelFunction.v2") { publicToInternalPATopLevelFunction() } // Signature remains the same.
    unlinkedTopLevelPrivateSymbol("/publicToPrivateTopLevelFunction") { publicToPrivateTopLevelFunction() } // Signature changed.

    success("Container.publicToProtectedFunction.v2") { c.publicToProtectedFunction() } // Signature remains the same.
    success("Container.publicToProtectedFunction.v2") { ci.publicToProtectedFunction() } // Signature remains the same.
    success("Container.publicToInternalFunction.v2") { c.publicToInternalFunction() } // Signature remains the same.
    unlinkedSymbol("/ContainerImpl.publicToInternalFunction") { ci.publicToInternalFunction() } // FOs are not generated for internal members from other module.
    success("Container.publicToInternalPAFunction.v2") { c.publicToInternalPAFunction() } // Signature remains the same.
    unlinkedSymbol("/ContainerImpl.publicToInternalPAFunction") { ci.publicToInternalPAFunction() } // FOs are not generated for internal members from other module.
    inaccessible("publicToPrivateFunction") { c.publicToPrivateFunction() } // Inaccessible from other module though signature remains the same.
    unlinkedSymbol("/ContainerImpl.publicToPrivateFunction") { ci.publicToPrivateFunction() } // FOs are not generated for private members.

    success("Container.publicToProtectedFunction.v2") { ci.publicToProtectedFunctionAccess() } // Signature remains the same.
    unlinkedSymbol("/ContainerImpl.publicToInternalFunction") { ci.publicToInternalFunctionAccess() } // FOs are not generated for internal members from other module.
    unlinkedSymbol("/ContainerImpl.publicToInternalPAFunction") { ci.publicToInternalPAFunctionAccess() } // FOs are not generated for internal members from other module.
    unlinkedSymbol("/ContainerImpl.publicToPrivateFunction") { ci.publicToPrivateFunctionAccess() } // FOs are not generated for private members.
    success("Container.protectedToPublicFunction.v2") { ci.protectedToPublicFunctionAccess() } // Signature remains the same.
    unlinkedSymbol("/ContainerImpl.protectedToInternalFunction") { ci.protectedToInternalFunctionAccess() } // FOs are not generated for internal members from other module.
    unlinkedSymbol("/ContainerImpl.protectedToInternalPAFunction") { ci.protectedToInternalPAFunctionAccess() } // FOs are not generated for internal members from other module.
    unlinkedSymbol("/ContainerImpl.protectedToPrivateFunction") { ci.protectedToPrivateFunctionAccess() } // FOs are not generated for private members.

    success("ContainerImpl.publicToProtectedOverriddenFunction") { ci.publicToProtectedOverriddenFunction() }
    success("ContainerImpl.publicToInternalOverriddenFunction") { ci.publicToInternalOverriddenFunction() }
    success("ContainerImpl.publicToInternalPAOverriddenFunction") { ci.publicToInternalPAOverriddenFunction() }
    success("ContainerImpl.publicToPrivateOverriddenFunction") { ci.publicToPrivateOverriddenFunction() }
    success("ContainerImpl.protectedToPublicOverriddenFunction") { ci.protectedToPublicOverriddenFunctionAccess() }
    success("ContainerImpl.protectedToInternalOverriddenFunction") { ci.protectedToInternalOverriddenFunctionAccess() }
    success("ContainerImpl.protectedToInternalPAOverriddenFunction") { ci.protectedToInternalPAOverriddenFunctionAccess() }
    success("ContainerImpl.protectedToPrivateOverriddenFunction") { ci.protectedToPrivateOverriddenFunctionAccess() }

    success("ContainerImpl.newPublicFunction") { ci.newPublicFunction() }
    success("ContainerImpl.newOpenPublicFunction") { ci.newOpenPublicFunction() }
    success("ContainerImpl.newProtectedFunction") { ci.newProtectedFunctionAccess() }
    success("ContainerImpl.newOpenProtectedFunction") { ci.newOpenProtectedFunctionAccess() }
    success("ContainerImpl.newInternalFunction") { ci.newInternalFunctionAccess() }
    success("ContainerImpl.newOpenInternalFunction") { ci.newOpenInternalFunctionAccess() }
    success("ContainerImpl.newInternalPAFunction") { ci.newInternalPAFunctionAccess() }
    success("ContainerImpl.newOpenInternalPAFunction") { ci.newOpenInternalPAFunctionAccess() }
    success("ContainerImpl.newPrivateFunction") { ci.newPrivateFunctionAccess() }

    success("publicTopLevelFunWithPrivateDefaultArgument.v2(privateTopLevelFun.v2)") { publicTopLevelFunWithPrivateDefaultArgument() }
    success("publicNestedFunWithPrivateDefaultArgument.v2(privateNestedFun.v2)") { TopLevel.publicNestedFunWithPrivateDefaultArgument() }
}

// Shortcuts:
private inline fun TestBuilder.success(expectedOutcome: String, noinline block: () -> String) =
    expectSuccess(expectedOutcome, block)

private inline fun TestBuilder.unlinkedSymbol(signature: String, noinline block: () -> Unit) {
    konst functionName = signature.removePrefix("/").substringAfterLast(".")
    expectFailure(linkage("Function '$functionName' can not be called: No function found for symbol '$signature'"), block)
}

private inline fun TestBuilder.unlinkedTopLevelPrivateSymbol(signature: String, noinline block: () -> Unit) {
    if (testMode == NATIVE_CACHE_STATIC_EVERYWHERE) {
        konst functionName = signature.removePrefix("/").substringAfterLast(".")
        expectFailure(linkage("Function '$functionName' can not be called: Private function declared in module <lib1> can not be accessed in module <main>"), block)
    } else
        unlinkedSymbol(signature, block)
}

private inline fun TestBuilder.inaccessible(functionName: String, noinline block: () -> Unit) = expectFailure(
    linkage("Function '$functionName' can not be called: Private function declared in module <lib1> can not be accessed in module <main>"),
    block
)
