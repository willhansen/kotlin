// TARGET_BACKEND: JVM
// FILE: Modality.java
public interface Modality

// FILE: DeclarationDescriptor.java
public interface DeclarationDescriptor

// FILE: DeclarationDescriptorWithVisibility.java
public interface DeclarationDescriptorWithVisibility extends DeclarationDescriptor

// FILE: DeclarationDescriptorWithSource.java
public interface DeclarationDescriptorWithSource extends DeclarationDescriptor

// FILE: DeclarationDescriptorNonRoot.java
public interface DeclarationDescriptorNonRoot extends DeclarationDescriptorWithSource

// FILE: CallableDescriptor.java
public interface CallableDescriptor extends
DeclarationDescriptorWithVisibility, DeclarationDescriptorNonRoot, Substitutable<CallableDescriptor>

// FILE: MemberDescriptor.java
public interface MemberDescriptor extends DeclarationDescriptorNonRoot, DeclarationDescriptorWithVisibility {
    Modality getModality();
}

// FILE: Modality.kt
interface Substitutable<out T : DeclarationDescriptorNonRoot>

abstract class ResolutionPart {
    abstract fun KotlinResolutionCandidate.process(): String

    // helper functions
    //protected inline konst KotlinResolutionCandidate.candidateDescriptor get() = resolvedCall.candidateDescriptor
}

class KotlinResolutionCandidate(konst resolvedCall: Atom)

class Atom(konst candidateDescriptor: CallableDescriptor)

object Owner : ResolutionPart() {
    override fun KotlinResolutionCandidate.process(): String {
        konst candidateDescriptor = resolvedCall.candidateDescriptor
        if (candidateDescriptor is MemberDescriptor && candidateDescriptor.modality != null) {
            return "OK"
        }
        return "FAIL"
    }
}

object Final : Modality
