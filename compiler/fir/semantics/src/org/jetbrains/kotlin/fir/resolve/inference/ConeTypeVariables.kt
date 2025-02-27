/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.inference

import org.jetbrains.kotlin.fir.declarations.FirAnonymousFunction
import org.jetbrains.kotlin.fir.symbols.impl.FirTypeParameterSymbol
import org.jetbrains.kotlin.fir.types.ConeTypeVariable

class ConeTypeVariableForPostponedAtom(name: String) : ConeTypeVariable(name)
class ConeTypeVariableForLambdaParameterType(name: String, konst index: Int) : ConeTypeVariable(name)
class ConeTypeVariableForLambdaReturnType(konst argument: FirAnonymousFunction, name: String) : ConeTypeVariable(name)

class ConeTypeParameterBasedTypeVariable(
    konst typeParameterSymbol: FirTypeParameterSymbol
) : ConeTypeVariable(typeParameterSymbol.name.identifier, typeParameterSymbol.toLookupTag())
