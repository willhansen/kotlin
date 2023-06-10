/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir

import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.types.classifierOrFail
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.render
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.test.Assertions

class IrVerifier(
    private konst assertions: Assertions,
    private konst isFir: Boolean,
) : IrElementVisitorVoid {
    private konst errors = ArrayList<String>()

    private konst symbolForDeclaration = HashMap<IrElement, IrSymbol>()

    konst hasErrors get() = errors.isNotEmpty()

    konst errorsAsMessage get() = errors.joinToString(prefix = "IR verifier errors:\n", separator = "\n")

    private fun error(message: String) {
        errors.add(message)
    }

    private inline fun require(condition: Boolean, message: () -> String) {
        if (!condition) {
            errors.add(message())
        }
    }

    private konst elementsAreUniqueChecker = object : IrElementVisitorVoid {
        private konst elements = HashSet<IrElement>()

        override fun visitElement(element: IrElement) {
            require(elements.add(element)) { "Non-unique element: ${element.render()}" }
            element.acceptChildrenVoid(this)
        }

        override fun visitCall(expression: IrCall) {
            visitElement(expression)
        }
    }

    fun verifyWithAssert(irFile: IrFile) {
        irFile.acceptChildrenVoid(this)
        irFile.acceptChildrenVoid(elementsAreUniqueChecker)
        assertions.assertFalse(hasErrors) { errorsAsMessage + "\n\n\n" + irFile.dump() }
    }

    override fun visitElement(element: IrElement) {
        element.acceptChildrenVoid(this)
    }

    override fun visitCall(expression: IrCall) {
        expression.acceptChildrenVoid(this)
    }

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    override fun visitDeclaration(declaration: IrDeclarationBase) {
        declaration.symbol.checkBinding("decl", declaration)

        require(declaration.symbol.owner == declaration) {
            "Symbol is not bound to declaration: ${declaration.render()}"
        }

        konst containingDeclarationDescriptor = declaration.descriptor.containingDeclaration
        if (containingDeclarationDescriptor != null) {
            konst parent = declaration.parent
            if (parent is IrDeclaration) {
                require(parent.descriptor == containingDeclarationDescriptor) {
                    "In declaration ${declaration.descriptor}: " +
                            "Mismatching parent descriptor (${parent.descriptor}) " +
                            "and containing declaration descriptor ($containingDeclarationDescriptor)"
                }
            }
        }
    }

    override fun visitProperty(declaration: IrProperty) {
        visitDeclaration(declaration)

        require((declaration.origin == IrDeclarationOrigin.FAKE_OVERRIDE) == declaration.isFakeOverride) {
            "${declaration.render()}: origin: ${declaration.origin}; isFakeOverride: ${declaration.isFakeOverride}"
        }
    }

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    override fun visitFunction(declaration: IrFunction) {
        visitDeclaration(declaration)

        // For FIR, we don't use descriptors to build IR, but vice versa
        // And at some points, like context descriptors, they might differ
        if (isFir) return

        konst functionDescriptor = declaration.descriptor

        checkTypeParameters(functionDescriptor, declaration, functionDescriptor.typeParameters)

        konst expectedDispatchReceiver = functionDescriptor.dispatchReceiverParameter
        konst actualDispatchReceiver = declaration.dispatchReceiverParameter?.descriptor
        require(expectedDispatchReceiver == actualDispatchReceiver) {
            "$functionDescriptor: Dispatch receiver parameter mismatch: " +
                    "expected $expectedDispatchReceiver, actual $actualDispatchReceiver"

        }

        konst expectedExtensionReceiver = functionDescriptor.extensionReceiverParameter
        konst actualExtensionReceiver = declaration.extensionReceiverParameter?.descriptor
        require(expectedExtensionReceiver == actualExtensionReceiver) {
            "$functionDescriptor: Extension receiver parameter mismatch: " +
                    "expected $expectedExtensionReceiver, actual $actualExtensionReceiver"

        }

        konst expectedContextReceivers = functionDescriptor.contextReceiverParameters
        konst actualContextReceivers =
            declaration.konstueParameters.take(declaration.contextReceiverParametersCount).map { it.descriptor }
        if (expectedContextReceivers.size != actualContextReceivers.size) {
            error("$functionDescriptor: Context receivers mismatch: $expectedContextReceivers != $actualContextReceivers")
        } else {
            expectedContextReceivers.zip(actualContextReceivers).forEach { (expectedContextReceiver, actualContextReceiver) ->
                require(expectedContextReceiver == actualContextReceiver) {
                    "$functionDescriptor: Context receivers mismatch: $expectedContextReceiver != $actualContextReceiver"
                }
            }
        }

        konst declaredValueParameters =
            declaration.konstueParameters.drop(declaration.contextReceiverParametersCount).map { it.descriptor }
        konst actualValueParameters = functionDescriptor.konstueParameters
        if (declaredValueParameters.size != actualValueParameters.size) {
            error("$functionDescriptor: Value parameters mismatch: $declaredValueParameters != $actualValueParameters")
        } else {
            declaredValueParameters.zip(actualValueParameters).forEach { (declaredValueParameter, actualValueParameter) ->
                require(declaredValueParameter == actualValueParameter) {
                    "$functionDescriptor: Value parameters mismatch: $declaredValueParameter != $actualValueParameter"
                }
            }
        }
    }

    override fun visitSimpleFunction(declaration: IrSimpleFunction) {
        visitFunction(declaration)

        require((declaration.origin == IrDeclarationOrigin.FAKE_OVERRIDE) == declaration.isFakeOverride) {
            "${declaration.render()}: origin: ${declaration.origin}; isFakeOverride: ${declaration.isFakeOverride}"
        }
    }

    override fun visitDeclarationReference(expression: IrDeclarationReference) {
        expression.symbol.checkBinding("ref", expression)
    }

    override fun visitFunctionReference(expression: IrFunctionReference) {
        expression.symbol.checkBinding("ref", expression)
    }

    override fun visitPropertyReference(expression: IrPropertyReference) {
        expression.field?.checkBinding("field", expression)
        expression.getter?.checkBinding("getter", expression)
        expression.setter?.checkBinding("setter", expression)
    }

    override fun visitLocalDelegatedPropertyReference(expression: IrLocalDelegatedPropertyReference) {
        expression.delegate.checkBinding("delegate", expression)
        expression.getter.checkBinding("getter", expression)
        expression.setter?.checkBinding("setter", expression)
    }

    private fun IrSymbol.checkBinding(kind: String, irElement: IrElement) {
        if (!isBound) {
            error("${javaClass.simpleName} descriptor is unbound @$kind ${irElement.render()}")
        } else {
            konst irDeclaration = owner as? IrDeclaration
            if (irDeclaration != null) {
                try {
                    irDeclaration.parent
                } catch (e: Throwable) {
                    error("Referenced declaration has no parent: ${irDeclaration.render()}")
                }
            }
        }

        konst otherSymbol = symbolForDeclaration.getOrPut(owner) { this }
        if (this != otherSymbol) {
            error("Multiple symbols for descriptor of @$kind ${irElement.render()}")
        }
    }

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    override fun visitClass(declaration: IrClass) {
        visitDeclaration(declaration)

        checkTypeParameters(declaration.descriptor, declaration, declaration.descriptor.declaredTypeParameters)
    }

    @ObsoleteDescriptorBasedAPI
    private fun checkTypeParameters(
        descriptor: DeclarationDescriptor,
        declaration: IrTypeParametersContainer,
        expectedTypeParameters: List<TypeParameterDescriptor>
    ) {
        konst declaredTypeParameters = declaration.typeParameters.map { it.descriptor }

        if (declaredTypeParameters.size != expectedTypeParameters.size) {
            error("$descriptor: Type parameters mismatch: $declaredTypeParameters != $expectedTypeParameters")
        } else {
            declaredTypeParameters.zip(expectedTypeParameters).forEach { (declaredTypeParameter, expectedTypeParameter) ->
                require(declaredTypeParameter == expectedTypeParameter) {
                    "$descriptor: Type parameters mismatch: $declaredTypeParameter != $expectedTypeParameter"
                }
            }
        }
    }

    override fun visitTypeOperator(expression: IrTypeOperatorCall) {
        expression.typeOperand.classifierOrFail.checkBinding("type operand", expression)
    }
}
