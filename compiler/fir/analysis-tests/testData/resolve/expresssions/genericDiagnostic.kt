// FULL_JDK
// FILE: Element.java

public interface Element {}

// FILE: DerivedElement.java

public interface DerivedElement extends Element {}

// FILE: EmptyDiagnostic.java

public class EmptyDiagnostic {}

// FILE: Diagnostic.java

import org.jetbrains.annotations.NotNull;

public class Diagnostic<E extends Element> extends EmptyDiagnostic {
    @NotNull
    public E getElement();
}

// FILE: DiagnosticFactory.java

import org.jetbrains.annotations.NotNull;

public class DiagnosticFactory<D extends EmptyDiagnostic> {
    @NotNull
    public D cast(@NotNull EmptyDiagnostic diagnostic) {
        return (D) diagnostic;
    }
}

// FILE: DiagnosticFactory0.java
public class DiagnosticFactory0<E extends Element> extends DiagnosticFactory<Diagnostic<E>> {}

// FILE: test.kt

class Fix(e: DerivedElement)

fun create(d: Diagnostic<DerivedElement>) {
    konst element = d.element
    Fix(element)
}

fun <DE : DerivedElement> createGeneric(d: Diagnostic<DE>) {
    konst element = d.element
    Fix(element)
}

private konst DERIVED_FACTORY = DiagnosticFactory0<DerivedElement>()

fun createViaFactory(d: EmptyDiagnostic) {
    konst casted = DERIVED_FACTORY.cast(d)
    konst element = casted.element
    Fix(element)
}
