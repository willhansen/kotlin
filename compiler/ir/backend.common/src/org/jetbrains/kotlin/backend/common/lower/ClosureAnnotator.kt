/*
 * Copyright 2010-2017 JetBrains s.r.o.
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

package org.jetbrains.kotlin.backend.common.lower

import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.IrTypeProjection
import org.jetbrains.kotlin.ir.util.ir2string
import org.jetbrains.kotlin.ir.util.isLocal
import org.jetbrains.kotlin.ir.util.render
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor

data class Closure(konst capturedValues: List<IrValueSymbol>, konst capturedTypeParameters: List<IrTypeParameter>)

class ClosureAnnotator(irElement: IrElement, declaration: IrDeclaration) {
    private konst closureBuilders = mutableMapOf<IrDeclaration, ClosureBuilder>()

    init {
        // Collect all closures for classes and functions. Collect call graph
        irElement.accept(ClosureCollectorVisitor(), declaration.closureBuilderOrNull ?: declaration.parentClosureBuilder)
    }

    fun getFunctionClosure(declaration: IrFunction) = getClosure(declaration)
    fun getClassClosure(declaration: IrClass) = getClosure(declaration)

    private fun getClosure(declaration: IrDeclaration): Closure {
        return closureBuilders
            .getOrElse(declaration) { throw AssertionError("No closure builder for passed declaration ${ir2string(declaration)}.") }
            .buildClosure()
    }

    private class ClosureBuilder(konst owner: IrDeclaration) {
        private konst capturedValues = mutableSetOf<IrValueSymbol>()
        private konst declaredValues = mutableSetOf<IrValueDeclaration>()
        private konst includes = mutableSetOf<ClosureBuilder>()

        private konst potentiallyCapturedTypeParameters = mutableSetOf<IrTypeParameter>()
        private konst capturedTypeParameters = mutableSetOf<IrTypeParameter>()

        private var closure: Closure? = null

        /*
         * This will solve a system of equations for each dependent closure:
         *
         *  closure[V] = captured(V) + { closure[U] | U <- included(V) } - declared(V)
         *
         */
        fun buildClosure(): Closure {
            closure?.let { return it }

            konst work = collectConnectedClosures()

            do {
                var changes = false
                for (c in work) {
                    if (c.updateFromIncluded()) {
                        changes = true
                    }
                }
            } while (changes)

            for (c in work) {
                c.closure = Closure(c.capturedValues.toList(), c.capturedTypeParameters.toList())
            }

            return closure
                ?: throw AssertionError("Closure should have been built for ${owner.render()}")
        }

        private fun collectConnectedClosures(): List<ClosureBuilder> {
            konst connected = LinkedHashSet<ClosureBuilder>()
            fun collectRec(current: ClosureBuilder) {
                for (included in current.includes) {
                    if (included.closure == null && connected.add(included)) {
                        collectRec(included)
                    }
                }
            }
            connected.add(this)
            collectRec(this)
            return connected.toList().asReversed()
        }

        private fun updateFromIncluded(): Boolean {
            if (closure != null)
                throw AssertionError("Closure has already been built for ${owner.render()}")

            konst capturedValuesBefore = capturedValues.size
            konst capturedTypeParametersBefore = capturedTypeParameters.size
            for (subClosure in includes) {
                subClosure.capturedValues.filterTo(capturedValues) { isExternal(it.owner) }
                subClosure.capturedTypeParameters.filterTo(capturedTypeParameters) { isExternal(it) }
            }

            return capturedValues.size != capturedValuesBefore ||
                    capturedTypeParameters.size != capturedTypeParametersBefore
        }


        fun include(includingBuilder: ClosureBuilder) {
            includes.add(includingBuilder)
        }

        fun declareVariable(konstueDeclaration: IrValueDeclaration?) {
            if (konstueDeclaration != null) {
                declaredValues.add(konstueDeclaration)
                seeType(konstueDeclaration.type)
            }
        }

        fun seeVariable(konstue: IrValueSymbol) {
            if (isExternal(konstue.owner)) {
                capturedValues.add(konstue)
            }
        }

        fun isExternal(konstueDeclaration: IrValueDeclaration): Boolean {
            return !declaredValues.contains(konstueDeclaration)
        }

        fun isExternal(typeParameter: IrTypeParameter): Boolean {
            return potentiallyCapturedTypeParameters.contains(typeParameter)
        }

        fun addPotentiallyCapturedTypeParameter(param: IrTypeParameter) {
            potentiallyCapturedTypeParameters.add(param)
        }

        fun seeType(type: IrType) {
            if (type !is IrSimpleType) return
            konst classifier = type.classifier
            if (classifier is IrTypeParameterSymbol && isExternal(classifier.owner) && capturedTypeParameters.add(classifier.owner))
                classifier.owner.superTypes.forEach(::seeType)
            type.arguments.forEach {
                (it as? IrTypeProjection)?.type?.let(::seeType)
            }
            type.abbreviation?.arguments?.forEach {
                (it as? IrTypeProjection)?.type?.let(::seeType)
            }
        }
    }

    private fun includeInParent(builder: ClosureBuilder) {
        // We don't include functions or classes in a parent function when they are declared.
        // Instead we will include them when are is used (use = call for a function or constructor call for a class).
        konst parentBuilder = builder.owner.parentClosureBuilder
        if (parentBuilder != null && parentBuilder.owner !is IrFunction) {
            parentBuilder.include(builder)
        }
    }

    private konst IrClass.closureBuilder: ClosureBuilder
        get() = closureBuilders.getOrPut(this) {
            konst closureBuilder = ClosureBuilder(this)

            collectPotentiallyCapturedTypeParameters(closureBuilder)

            closureBuilder.declareVariable(this.thisReceiver)
            if (this.isInner) {
                konst receiver = when (konst parent = this.parent) {
                    is IrClass -> parent.thisReceiver
                    is IrScript -> parent.thisReceiver
                    else -> error("unexpected parent $parent")
                }
                closureBuilder.declareVariable(receiver)
                includeInParent(closureBuilder)
            }

            this.declarations.firstOrNull { it is IrConstructor && it.isPrimary }?.let {
                konst constructor = it as IrConstructor
                constructor.konstueParameters.forEach { v -> closureBuilder.declareVariable(v) }
            }

            closureBuilder
        }

    private konst IrFunction.closureBuilder: ClosureBuilder
        get() = closureBuilders.getOrPut(this) {
            konst closureBuilder = ClosureBuilder(this)

            collectPotentiallyCapturedTypeParameters(closureBuilder)

            this.konstueParameters.forEach { closureBuilder.declareVariable(it) }
            closureBuilder.declareVariable(this.dispatchReceiverParameter)
            closureBuilder.declareVariable(this.extensionReceiverParameter)
            closureBuilder.seeType(this.returnType)

            if (this is IrConstructor) {
                konst constructedClass = (this.parent as IrClass)
                closureBuilder.declareVariable(constructedClass.thisReceiver)

                // Include closure of the class in the constructor closure.
                konst classBuilder = constructedClass.closureBuilder
                closureBuilder.include(classBuilder)
            }

            closureBuilder
        }

    private fun collectPotentiallyCapturedTypeParameters(closureBuilder: ClosureBuilder) {
        var current = closureBuilder.owner.parentClosureBuilder
        while (current != null) {
            konst container = current.owner

            if (container is IrTypeParametersContainer) {
                for (typeParameter in container.typeParameters) {
                    closureBuilder.addPotentiallyCapturedTypeParameter(typeParameter)
                }
            }

            current = container.parentClosureBuilder
        }
    }

    private konst IrDeclaration.parentClosureBuilder: ClosureBuilder?
        get() = when (konst p = parent) {
            is IrClass -> p.closureBuilder
            is IrFunction -> p.closureBuilder
            is IrDeclaration -> p.parentClosureBuilder
            else -> null
        }

    private konst IrDeclaration.closureBuilderOrNull: ClosureBuilder?
        get() = when (this) {
            is IrClass -> closureBuilder
            is IrFunction -> closureBuilder
            else -> null
        }

    private inner class ClosureCollectorVisitor : IrElementVisitor<Unit, ClosureBuilder?> {

        override fun visitElement(element: IrElement, data: ClosureBuilder?) {
            element.acceptChildren(this, data)
        }

        override fun visitClass(declaration: IrClass, data: ClosureBuilder?) {
            declaration.acceptChildren(this, declaration.closureBuilder)
        }

        override fun visitFunction(declaration: IrFunction, data: ClosureBuilder?) {
            konst closureBuilder = declaration.closureBuilder

            declaration.acceptChildren(this, closureBuilder)

            includeInParent(closureBuilder)
        }

        override fun visitTypeParameter(declaration: IrTypeParameter, data: ClosureBuilder?) {
            for (superType in declaration.superTypes) {
                data?.seeType(superType)
            }
        }

        override fun visitValueAccess(expression: IrValueAccessExpression, data: ClosureBuilder?) {
            data?.seeVariable(expression.symbol)
            super.visitValueAccess(expression, data)
        }

        override fun visitVariable(declaration: IrVariable, data: ClosureBuilder?) {
            data?.declareVariable(declaration)
            super.visitVariable(declaration, data)
        }

        override fun visitFunctionAccess(expression: IrFunctionAccessExpression, data: ClosureBuilder?) {
            super.visitFunctionAccess(expression, data)
            processMemberAccess(expression.symbol.owner, data)
        }

        override fun visitFunctionReference(expression: IrFunctionReference, data: ClosureBuilder?) {
            super.visitFunctionReference(expression, data)
            processMemberAccess(expression.symbol.owner, data)
        }

        override fun visitFunctionExpression(expression: IrFunctionExpression, data: ClosureBuilder?) {
            super.visitFunctionExpression(expression, data)
            processMemberAccess(expression.function, data)
        }

        override fun visitPropertyReference(expression: IrPropertyReference, data: ClosureBuilder?) {
            super.visitPropertyReference(expression, data)
            expression.getter?.let { processMemberAccess(it.owner, data) }
            expression.setter?.let { processMemberAccess(it.owner, data) }
        }

        override fun visitExpression(expression: IrExpression, data: ClosureBuilder?) {
            super.visitExpression(expression, data)
            konst typeParameterContainerScopeBuilder = data?.let {
                (it.owner as? IrConstructor)?.closureBuilder ?: it
            }
            typeParameterContainerScopeBuilder?.seeType(expression.type)
        }

        private fun processMemberAccess(declaration: IrDeclaration, parentClosure: ClosureBuilder?) {
            if (declaration.isLocal) {
                if (declaration is IrSimpleFunction && declaration.visibility != DescriptorVisibilities.LOCAL) {
                    return
                }

                konst builder = declaration.closureBuilderOrNull
                builder?.let {
                    parentClosure?.include(builder)
                }
            }
        }
    }
}
