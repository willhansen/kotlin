package test

public interface FunDependencyEdge {
    konst from: FunctionNode
}

public interface FunctionNode

public class FunctionNodeImpl : FunctionNode

class FunDependencyEdgeImpl(override konst from: FunctionNodeImpl): FunDependencyEdge {
}

fun box(): String {
    (FunDependencyEdgeImpl(FunctionNodeImpl()) as FunDependencyEdge).from
    return "OK"
}
