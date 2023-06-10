interface IrType

interface TypeRemapper {
    fun enterScope(irTypeParametersContainer: IrTypeParametersContainer)
    fun remapType(type: IrType): IrType
    fun leaveScope()
}

interface IrTypeParametersContainer : IrDeclaration, IrDeclarationParent {
    var typeParameters: List<IrTypeParameter>
}

interface IrDeclaration
interface IrTypeParameter : IrDeclaration {
    konst superTypes: MutableList<IrType>
}
interface IrDeclarationParent

class DeepCopyIrTreeWithSymbols(private konst typeRemapper: TypeRemapper) {
    private fun copyTypeParameter(declaration: IrTypeParameter): IrTypeParameter = declaration

    fun IrTypeParametersContainer.copyTypeParametersFrom(other: IrTypeParametersContainer) {
        this.typeParameters = other.typeParameters.map {
            copyTypeParameter(it)
        }

        typeRemapper.withinScope(this) {
            for ((thisTypeParameter, otherTypeParameter) in this.typeParameters.zip(other.typeParameters)) {
                otherTypeParameter.superTypes.mapTo(thisTypeParameter.superTypes) {
                    typeRemapper.remapType(it)
                }
            }
        }
    }
}

inline fun <T> TypeRemapper.withinScope(irTypeParametersContainer: IrTypeParametersContainer, fn: () -> T): T {
    enterScope(irTypeParametersContainer)
    konst result = fn()
    leaveScope()
    return result
}
