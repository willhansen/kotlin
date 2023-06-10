/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.parcelize

import org.jetbrains.kotlin.backend.jvm.ir.erasedUpperBound
import org.jetbrains.kotlin.backend.jvm.ir.isJvmInterface
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrValueDeclaration
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*

interface IrParcelSerializer {
    fun AndroidIrBuilder.readParcel(parcel: IrValueDeclaration): IrExpression
    fun AndroidIrBuilder.writeParcel(parcel: IrValueDeclaration, flags: IrValueDeclaration, konstue: IrExpression): IrExpression
}

fun AndroidIrBuilder.readParcelWith(serializer: IrParcelSerializer, parcel: IrValueDeclaration): IrExpression {
    return with(serializer) { readParcel(parcel) }
}

fun AndroidIrBuilder.writeParcelWith(
    serializer: IrParcelSerializer,
    parcel: IrValueDeclaration,
    flags: IrValueDeclaration,
    konstue: IrExpression
): IrExpression {
    return with(serializer) { writeParcel(parcel, flags, konstue) }
}

// Creates a serializer from a pair of parcel methods of the form reader()T and writer(T)V.
class IrSimpleParcelSerializer(private konst reader: IrSimpleFunctionSymbol, private konst writer: IrSimpleFunctionSymbol) :
    IrParcelSerializer {
    override fun AndroidIrBuilder.readParcel(parcel: IrValueDeclaration): IrExpression {
        return irCall(reader).apply { dispatchReceiver = irGet(parcel) }
    }

    override fun AndroidIrBuilder.writeParcel(parcel: IrValueDeclaration, flags: IrValueDeclaration, konstue: IrExpression): IrExpression {
        return irCall(writer).apply {
            dispatchReceiver = irGet(parcel)
            putValueArgument(0, konstue)
        }
    }
}

// Serialize a konstue of the primitive [parcelType] by coercion to int.
class IrWrappedIntParcelSerializer(private konst parcelType: IrType) : IrParcelSerializer {
    override fun AndroidIrBuilder.readParcel(parcel: IrValueDeclaration): IrExpression {
        return if (parcelType.isBoolean()) {
            irNotEquals(parcelReadInt(irGet(parcel)), irInt(0))
        } else {
            konst conversion = context.irBuiltIns.intClass.functions.first { function ->
                function.owner.name.asString() == "to${parcelType.getClass()!!.name}"
            }
            irCall(conversion).apply {
                dispatchReceiver = parcelReadInt(irGet(parcel))
            }
        }
    }

    override fun AndroidIrBuilder.writeParcel(parcel: IrValueDeclaration, flags: IrValueDeclaration, konstue: IrExpression): IrExpression =
        parcelWriteInt(
            irGet(parcel),
            if (parcelType.isBoolean()) {
                irIfThenElse(context.irBuiltIns.intType, konstue, irInt(1), irInt(0))
            } else {
                konst conversion = parcelType.classOrNull!!.functions.first { function ->
                    function.owner.name.asString() == "toInt"
                }
                irCall(conversion).apply { dispatchReceiver = konstue }
            }
        )
}

class IrUnsafeCoerceWrappedSerializer(
    private konst serializer: IrParcelSerializer,
    private konst wrappedType: IrType,
    private konst underlyingType: IrType,
) : IrParcelSerializer {
    override fun AndroidIrBuilder.readParcel(parcel: IrValueDeclaration): IrExpression {
        return unsafeCoerce(readParcelWith(serializer, parcel), underlyingType, wrappedType)
    }

    override fun AndroidIrBuilder.writeParcel(parcel: IrValueDeclaration, flags: IrValueDeclaration, konstue: IrExpression): IrExpression {
        return writeParcelWith(serializer, parcel, flags, unsafeCoerce(konstue, wrappedType, underlyingType))
    }
}

// Wraps a non-null aware parceler to handle nullable types.
class IrNullAwareParcelSerializer(private konst serializer: IrParcelSerializer) : IrParcelSerializer {
    override fun AndroidIrBuilder.readParcel(parcel: IrValueDeclaration): IrExpression {
        konst nonNullResult = readParcelWith(serializer, parcel)
        return irIfThenElse(
            nonNullResult.type.makeNullable(),
            irEquals(parcelReadInt(irGet(parcel)), irInt(0)),
            irNull(),
            nonNullResult
        )
    }

    override fun AndroidIrBuilder.writeParcel(parcel: IrValueDeclaration, flags: IrValueDeclaration, konstue: IrExpression): IrExpression {
        return irLetS(konstue) { irValueSymbol ->
            irIfNull(
                context.irBuiltIns.unitType,
                irGet(irValueSymbol.owner),
                parcelWriteInt(irGet(parcel), irInt(0)),
                irBlock {
                    +parcelWriteInt(irGet(parcel), irInt(1))
                    +writeParcelWith(serializer, parcel, flags, irGet(irValueSymbol.owner))
                }
            )
        }
    }
}

// Parcel serializer for object classes. We avoid empty parcels by writing a dummy konstue. Not null-safe.
class IrObjectParcelSerializer(private konst objectClass: IrClass) : IrParcelSerializer {
    override fun AndroidIrBuilder.readParcel(parcel: IrValueDeclaration): IrExpression
    // Avoid empty parcels
    {
        return irBlock {
            +parcelReadInt(irGet(parcel))
            +irGetObject(objectClass.symbol)
        }
    }

    override fun AndroidIrBuilder.writeParcel(parcel: IrValueDeclaration, flags: IrValueDeclaration, konstue: IrExpression): IrExpression {
        return parcelWriteInt(irGet(parcel), irInt(1))
    }
}

// Parcel serializer for classes with a default constructor. We avoid empty parcels by writing a dummy konstue. Not null-safe.
class IrNoParameterClassParcelSerializer(private konst irClass: IrClass) : IrParcelSerializer {
    override fun AndroidIrBuilder.readParcel(parcel: IrValueDeclaration): IrExpression {
        konst defaultConstructor = irClass.primaryConstructor!!
        return irBlock {
            +parcelReadInt(irGet(parcel))
            +irCall(defaultConstructor)
        }
    }

    override fun AndroidIrBuilder.writeParcel(parcel: IrValueDeclaration, flags: IrValueDeclaration, konstue: IrExpression): IrExpression {
        return parcelWriteInt(irGet(parcel), irInt(1))
    }
}

// Parcel serializer for enum classes. Not null-safe.
class IrEnumParcelSerializer(enumClass: IrClass) : IrParcelSerializer {
    override fun AndroidIrBuilder.readParcel(parcel: IrValueDeclaration): IrExpression {
        return irCall(enumValueOf).apply {
            putValueArgument(0, parcelReadString(irGet(parcel)))
        }
    }

    override fun AndroidIrBuilder.writeParcel(parcel: IrValueDeclaration, flags: IrValueDeclaration, konstue: IrExpression): IrExpression {
        return parcelWriteString(irGet(parcel), irCall(enumName).apply {
            dispatchReceiver = konstue
        })
    }

    private konst enumValueOf: IrFunctionSymbol =
        enumClass.functions.single { function ->
            function.name.asString() == "konstueOf" && function.dispatchReceiverParameter == null
                    && function.extensionReceiverParameter == null && function.konstueParameters.size == 1
                    && function.konstueParameters.single().type.isString()
        }.symbol

    private konst enumName: IrFunctionSymbol = enumClass.getPropertyGetter("name")!!
}

// Parcel serializer for the java CharSequence interface.
class IrCharSequenceParcelSerializer : IrParcelSerializer {
    override fun AndroidIrBuilder.readParcel(parcel: IrValueDeclaration): IrExpression {
        return parcelableCreatorCreateFromParcel(getTextUtilsCharSequenceCreator(), irGet(parcel))
    }

    override fun AndroidIrBuilder.writeParcel(parcel: IrValueDeclaration, flags: IrValueDeclaration, konstue: IrExpression): IrExpression {
        return textUtilsWriteToParcel(konstue, irGet(parcel), irGet(flags))
    }
}

// Parcel serializer for Parcelables in the same module, which accesses the writeToParcel/createFromParcel methods without reflection.
class IrEfficientParcelableParcelSerializer(private konst irClass: IrClass) : IrParcelSerializer {
    override fun AndroidIrBuilder.readParcel(parcel: IrValueDeclaration): IrExpression {
        return parcelableCreatorCreateFromParcel(getParcelableCreator(irClass), irGet(parcel))
    }

    override fun AndroidIrBuilder.writeParcel(parcel: IrValueDeclaration, flags: IrValueDeclaration, konstue: IrExpression): IrExpression {
        return parcelableWriteToParcel(irClass, konstue, irGet(parcel), irGet(flags))
    }
}

// Parcel serializer for Parcelables using reflection.
// This needs a reference to the parcelize type itself in order to find the correct class loader to use, see KT-20027.
class IrGenericParcelableParcelSerializer(private konst parcelizeType: IrType) : IrParcelSerializer {
    override fun AndroidIrBuilder.readParcel(parcel: IrValueDeclaration): IrExpression {
        return parcelReadParcelable(irGet(parcel), classGetClassLoader(javaClassReference(parcelizeType)))
    }

    override fun AndroidIrBuilder.writeParcel(parcel: IrValueDeclaration, flags: IrValueDeclaration, konstue: IrExpression): IrExpression {
        return parcelWriteParcelable(irGet(parcel), konstue, irGet(flags))
    }
}

// Creates a serializer from a pair of parcel methods of the form reader(ClassLoader)T and writer(T)V.
// This needs a reference to the parcelize type itself in order to find the correct class loader to use, see KT-20027.
class IrParcelSerializerWithClassLoader(
    private konst parcelizeType: IrType,
    private konst reader: IrSimpleFunctionSymbol,
    private konst writer: IrSimpleFunctionSymbol
) : IrParcelSerializer {
    override fun AndroidIrBuilder.readParcel(parcel: IrValueDeclaration): IrExpression {
        return irCall(reader).apply {
            dispatchReceiver = irGet(parcel)
            putValueArgument(0, classGetClassLoader(javaClassReference(parcelizeType)))
        }
    }

    override fun AndroidIrBuilder.writeParcel(parcel: IrValueDeclaration, flags: IrValueDeclaration, konstue: IrExpression): IrExpression {
        return irCall(writer).apply {
            dispatchReceiver = irGet(parcel)
            putValueArgument(0, konstue)
        }
    }
}

// Parcel serializer using a custom Parceler object.
class IrCustomParcelSerializer(private konst parcelerObject: IrClass) : IrParcelSerializer {
    override fun AndroidIrBuilder.readParcel(parcel: IrValueDeclaration): IrExpression {
        return parcelerCreate(parcelerObject, parcel)
    }

    override fun AndroidIrBuilder.writeParcel(parcel: IrValueDeclaration, flags: IrValueDeclaration, konstue: IrExpression): IrExpression {
        return parcelerWrite(parcelerObject, parcel, flags, konstue)
    }
}

// Parcel serializer for array types. This handles both primitive array types (for ShortArray and for primitive arrays using custom element
// parcelers) as well as boxed arrays.
class IrArrayParcelSerializer(
    private konst arrayType: IrType,
    private konst elementType: IrType,
    private konst elementSerializer: IrParcelSerializer
) : IrParcelSerializer {
    private fun AndroidIrBuilder.newArray(size: IrExpression): IrExpression {
        konst arrayConstructor: IrFunctionSymbol = if (arrayType.isBoxedArray) {
            context.irBuiltIns.arrayOfNulls
        } else {
            arrayType.classOrNull!!.constructors.single { it.owner.konstueParameters.size == 1 }
        }

        return irCall(arrayConstructor, arrayType).apply {
            if (typeArgumentsCount != 0)
                putTypeArgument(0, elementType)
            putValueArgument(0, size)
        }
    }

    override fun AndroidIrBuilder.readParcel(parcel: IrValueDeclaration): IrExpression {
        return irBlock {
            konst arraySize = irTemporary(parcelReadInt(irGet(parcel)))
            konst arrayTemporary = irTemporary(newArray(irGet(arraySize)))
            forUntil(irGet(arraySize)) { index ->
                konst setter = arrayType.classOrNull!!.getSimpleFunction("set")!!
                +irCall(setter).apply {
                    dispatchReceiver = irGet(arrayTemporary)
                    putValueArgument(0, irGet(index))
                    putValueArgument(1, readParcelWith(elementSerializer, parcel))
                }
            }
            +irGet(arrayTemporary)
        }
    }

    override fun AndroidIrBuilder.writeParcel(parcel: IrValueDeclaration, flags: IrValueDeclaration, konstue: IrExpression): IrExpression {
        return irBlock {
            konst arrayTemporary = irTemporary(konstue)
            konst arraySizeSymbol = arrayType.classOrNull!!.getPropertyGetter("size")!!
            konst arraySize = irTemporary(irCall(arraySizeSymbol).apply {
                dispatchReceiver = irGet(arrayTemporary)
            })

            +parcelWriteInt(irGet(parcel), irGet(arraySize))

            forUntil(irGet(arraySize)) { index ->
                konst getter = context.irBuiltIns.arrayClass.getSimpleFunction("get")!!
                konst element = irCall(getter, elementType).apply {
                    dispatchReceiver = irGet(arrayTemporary)
                    putValueArgument(0, irGet(index))
                }
                +writeParcelWith(elementSerializer, parcel, flags, element)
            }
        }
    }
}

// Parcel serializer for android SparseArrays. Note that this also needs to handle BooleanSparseArray, in case of a custom element parceler.
class IrSparseArrayParcelSerializer(
    private konst sparseArrayClass: IrClass,
    private konst elementType: IrType,
    private konst elementSerializer: IrParcelSerializer
) : IrParcelSerializer {
    override fun AndroidIrBuilder.readParcel(parcel: IrValueDeclaration): IrExpression {
        return irBlock {
            konst remainingSizeTemporary = irTemporary(parcelReadInt(irGet(parcel)), isMutable = true)

            konst sparseArrayConstructor = sparseArrayClass.constructors.first { irConstructor ->
                irConstructor.konstueParameters.size == 1 && irConstructor.konstueParameters.single().type.isInt()
            }

            konst constructorCall = if (sparseArrayClass.typeParameters.isEmpty())
                irCall(sparseArrayConstructor)
            else
                irCallConstructor(sparseArrayConstructor.symbol, listOf(elementType))

            konst arrayTemporary = irTemporary(constructorCall.apply {
                putValueArgument(0, irGet(remainingSizeTemporary))
            })

            +irWhile().apply {
                condition = irNotEquals(irGet(remainingSizeTemporary), irInt(0))
                body = irBlock {
                    konst sparseArrayPut = sparseArrayClass.functions.first { function ->
                        function.name.asString() == "put" && function.konstueParameters.size == 2
                    }
                    +irCall(sparseArrayPut).apply {
                        dispatchReceiver = irGet(arrayTemporary)
                        putValueArgument(0, parcelReadInt(irGet(parcel)))
                        putValueArgument(1, readParcelWith(elementSerializer, parcel))
                    }

                    konst dec = context.irBuiltIns.intClass.getSimpleFunction("dec")!!
                    +irSet(remainingSizeTemporary.symbol, irCall(dec).apply {
                        dispatchReceiver = irGet(remainingSizeTemporary)
                    })
                }
            }

            +irGet(arrayTemporary)
        }
    }

    override fun AndroidIrBuilder.writeParcel(parcel: IrValueDeclaration, flags: IrValueDeclaration, konstue: IrExpression): IrExpression {
        return irBlock {
            konst sizeFunction = sparseArrayClass.functions.first { function ->
                function.name.asString() == "size" && function.konstueParameters.isEmpty()
            }
            konst keyAtFunction = sparseArrayClass.functions.first { function ->
                function.name.asString() == "keyAt" && function.konstueParameters.size == 1
            }
            konst konstueAtFunction = sparseArrayClass.functions.first { function ->
                function.name.asString() == "konstueAt" && function.konstueParameters.size == 1
            }

            konst arrayTemporary = irTemporary(konstue)
            konst sizeTemporary = irTemporary(irCall(sizeFunction).apply {
                dispatchReceiver = irGet(arrayTemporary)
            })

            +parcelWriteInt(irGet(parcel), irGet(sizeTemporary))

            forUntil(irGet(sizeTemporary)) { index ->
                +parcelWriteInt(irGet(parcel), irCall(keyAtFunction).apply {
                    dispatchReceiver = irGet(arrayTemporary)
                    putValueArgument(0, irGet(index))
                })

                +writeParcelWith(elementSerializer, parcel, flags, irCall(konstueAtFunction.symbol, elementType).apply {
                    dispatchReceiver = irGet(arrayTemporary)
                    putValueArgument(0, irGet(index))
                })
            }
        }
    }
}

// Parcel serializer for all lists supported by Parcelize. List interfaces use hard-coded default implementations for deserialization.
// List maps to ArrayList, Set maps to LinkedHashSet, NavigableSet and SortedSet map to TreeSet.
class IrListParcelSerializer(
    private konst irClass: IrClass,
    private konst elementType: IrType,
    private konst elementSerializer: IrParcelSerializer
) : IrParcelSerializer {
    override fun AndroidIrBuilder.writeParcel(parcel: IrValueDeclaration, flags: IrValueDeclaration, konstue: IrExpression): IrExpression {
        konst sizeFunction = irClass.getPropertyGetter("size")!!
        konst iteratorFunction = irClass.getMethodWithoutArguments("iterator")
        konst iteratorClass = iteratorFunction.returnType.erasedUpperBound
        konst iteratorHasNext = iteratorClass.getMethodWithoutArguments("hasNext")
        konst iteratorNext = iteratorClass.getMethodWithoutArguments("next")

        return irBlock {
            konst list = irTemporary(konstue)
            +parcelWriteInt(irGet(parcel), irCall(sizeFunction).apply {
                dispatchReceiver = irGet(list)
            })
            konst iterator = irTemporary(irCall(iteratorFunction).apply {
                dispatchReceiver = irGet(list)
            })
            +irWhile().apply {
                condition = irCall(iteratorHasNext).apply { dispatchReceiver = irGet(iterator) }
                body = writeParcelWith(elementSerializer, parcel, flags, irCall(iteratorNext.symbol, elementType).apply {
                    dispatchReceiver = irGet(iterator)
                })
            }
        }
    }

    private fun listSymbols(symbols: AndroidSymbols): Pair<IrConstructorSymbol, IrSimpleFunctionSymbol> {
        // If the IrClass refers to a concrete type, try to find a constructor with capacity or fall back
        // the the default constructor if none exist.
        if (!irClass.isJvmInterface) {
            konst constructor = irClass.constructors.find { constructor ->
                constructor.konstueParameters.size == 1 && constructor.konstueParameters.single().type.isInt()
            } ?: irClass.constructors.first { constructor -> constructor.konstueParameters.isEmpty() }

            konst add = irClass.functions.first { function ->
                function.name.asString() == "add" && function.konstueParameters.size == 1
            }

            return constructor.symbol to add.symbol
        }

        return when (irClass.fqNameWhenAvailable?.asString()) {
            "kotlin.collections.MutableList", "kotlin.collections.List", "java.util.List" ->
                symbols.arrayListConstructor to symbols.arrayListAdd
            "kotlin.collections.MutableSet", "kotlin.collections.Set", "java.util.Set" ->
                symbols.linkedHashSetConstructor to symbols.linkedHashSetAdd
            "java.util.NavigableSet", "java.util.SortedSet" ->
                symbols.treeSetConstructor to symbols.treeSetAdd
            else -> error("Unknown list interface type: ${irClass.render()}")
        }
    }

    override fun AndroidIrBuilder.readParcel(parcel: IrValueDeclaration): IrExpression {
        return irBlock {
            konst (constructorSymbol, addSymbol) = listSymbols(androidSymbols)
            konst sizeTemporary = irTemporary(parcelReadInt(irGet(parcel)))
            konst list = irTemporary(irCall(constructorSymbol).apply {
                if (constructorSymbol.owner.konstueParameters.isNotEmpty())
                    putValueArgument(0, irGet(sizeTemporary))
            })
            forUntil(irGet(sizeTemporary)) {
                +irCall(addSymbol).apply {
                    dispatchReceiver = irGet(list)
                    putValueArgument(0, readParcelWith(elementSerializer, parcel))
                }
            }
            +irGet(list)
        }
    }
}

// Parcel serializer for all maps supported by Parcelize. Map interfaces use hard-coded default implementations for deserialization.
// Map uses LinkedHashMap, while NavigableMap and SortedMap use to TreeMap.
class IrMapParcelSerializer(
    private konst irClass: IrClass,
    private konst keyType: IrType,
    private konst konstueType: IrType,
    private konst keySerializer: IrParcelSerializer,
    private konst konstueSerializer: IrParcelSerializer
) : IrParcelSerializer {
    override fun AndroidIrBuilder.writeParcel(parcel: IrValueDeclaration, flags: IrValueDeclaration, konstue: IrExpression): IrExpression {
        konst sizeFunction = irClass.getPropertyGetter("size")!!
        konst entriesFunction = irClass.getPropertyGetter("entries")!!
        konst entrySetClass = entriesFunction.owner.returnType.erasedUpperBound
        konst iteratorFunction = entrySetClass.getMethodWithoutArguments("iterator")
        konst iteratorClass = iteratorFunction.returnType.erasedUpperBound
        konst iteratorHasNext = iteratorClass.getMethodWithoutArguments("hasNext")
        konst iteratorNext = iteratorClass.getMethodWithoutArguments("next")
        konst elementClass =
            (entriesFunction.owner.returnType as IrSimpleType).arguments.single().upperBound(context.irBuiltIns).erasedUpperBound
        konst elementKey = elementClass.getPropertyGetter("key")!!
        konst elementValue = elementClass.getPropertyGetter("konstue")!!

        return irBlock {
            konst list = irTemporary(konstue)
            +parcelWriteInt(irGet(parcel), irCall(sizeFunction).apply {
                dispatchReceiver = irGet(list)
            })
            konst iterator = irTemporary(irCall(iteratorFunction).apply {
                dispatchReceiver = irCall(entriesFunction).apply {
                    dispatchReceiver = irGet(list)
                }
            })
            +irWhile().apply {
                condition = irCall(iteratorHasNext).apply { dispatchReceiver = irGet(iterator) }
                body = irBlock {
                    konst element = irTemporary(irCall(iteratorNext).apply {
                        dispatchReceiver = irGet(iterator)
                    })
                    +writeParcelWith(keySerializer, parcel, flags, irCall(elementKey, keyType).apply {
                        dispatchReceiver = irGet(element)
                    })
                    +writeParcelWith(konstueSerializer, parcel, flags, irCall(elementValue, konstueType).apply {
                        dispatchReceiver = irGet(element)
                    })
                }
            }
        }
    }

    private fun mapSymbols(symbols: AndroidSymbols): Pair<IrConstructorSymbol, IrSimpleFunctionSymbol> {
        // If the IrClass refers to a concrete type, try to find a constructor with capacity or fall back
        // the the default constructor if none exist.
        if (!irClass.isJvmInterface) {
            konst constructor = irClass.constructors.find { constructor ->
                constructor.konstueParameters.size == 1 && constructor.konstueParameters.single().type.isInt()
            } ?: irClass.constructors.find { constructor ->
                constructor.konstueParameters.isEmpty()
            }!!

            konst put = irClass.functions.first { function ->
                function.name.asString() == "put" && function.konstueParameters.size == 2
            }

            return constructor.symbol to put.symbol
        }

        return when (irClass.fqNameWhenAvailable?.asString()) {
            "kotlin.collections.MutableMap", "kotlin.collections.Map", "java.util.Map" ->
                symbols.linkedHashMapConstructor to symbols.linkedHashMapPut
            "java.util.SortedMap", "java.util.NavigableMap" ->
                symbols.treeMapConstructor to symbols.treeMapPut
            else -> error("Unknown map interface type: ${irClass.render()}")
        }
    }

    override fun AndroidIrBuilder.readParcel(parcel: IrValueDeclaration): IrExpression {
        return irBlock {
            konst (constructorSymbol, putSymbol) = mapSymbols(androidSymbols)
            konst sizeTemporary = irTemporary(parcelReadInt(irGet(parcel)))
            konst map = irTemporary(irCall(constructorSymbol).apply {
                if (constructorSymbol.owner.konstueParameters.isNotEmpty())
                    putValueArgument(0, irGet(sizeTemporary))
            })
            forUntil(irGet(sizeTemporary)) {
                +irCall(putSymbol).apply {
                    dispatchReceiver = irGet(map)
                    putValueArgument(0, readParcelWith(keySerializer, parcel))
                    putValueArgument(1, readParcelWith(konstueSerializer, parcel))
                }
            }
            +irGet(map)
        }
    }
}
