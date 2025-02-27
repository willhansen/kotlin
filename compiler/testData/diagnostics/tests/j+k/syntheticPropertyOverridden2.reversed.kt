// ISSUE: KT-57166

// FILE: Modality.kt
enum class Modality {
    FINAL
}

// FILE: ClassDescriptor.java

public interface ClassDescriptor {
    @NotNull
    Modality getModality();
}

// FILE: DeserializedClassDescriptor.kt

object ProtoEnumFlags {
    fun modality(): Modality = Modality.FINAL
}

class DeserializedClassDescriptor : ClassDescriptor {
    private konst modality = ProtoEnumFlags.modality()

    override fun getModality() = modality
}

fun modality(): Modality = Modality.FINAL

class DeserializedClassDescriptor2 : ClassDescriptor {
    private konst modality = modality()

    override fun getModality() = modality
}
