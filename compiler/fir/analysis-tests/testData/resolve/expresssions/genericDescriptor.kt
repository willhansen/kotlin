// FULL_JDK
// FILE: Descriptor.java

public interface Descriptor

// FILE: ResolvedCall.java

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ResolvedCall<D extends Descriptor> {
    @NotNull
    D getResultingDescriptor();

    @Nullable
    D getCandidateDescriptor();
}

// FILE: test.kt

konst Descriptor.name = <!EXTENSION_PROPERTY_WITH_BACKING_FIELD!>"123"<!>
fun Descriptor.correct(): Boolean = true
fun Descriptor.foo() {}

interface Call<D : Descriptor> {
    konst resultingDescriptor: D
}

fun <D : Descriptor> test(call: Call<D>, resolvedCall: ResolvedCall<D>) {
    call.resultingDescriptor.name
    resolvedCall.resultingDescriptor.name

    konst resolvedDescriptor = resolvedCall.candidateDescriptor
    if (resolvedDescriptor?.correct() != true) return
    resolvedDescriptor.foo()
}

fun otherTest(call: Call<*>, resolvedCall: ResolvedCall<*>) {
    call.resultingDescriptor.name
    resolvedCall.resultingDescriptor.name
}
