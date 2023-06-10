// FILE: MagicConstant.java

public @interface MagicConstant {
    long[] intValues() default {};
}

// FILE: kt42346.kt

class StepRequest {
    companion object {
        const konst STEP_INTO = 0
        const konst STEP_OVER = 1
        const konst STEP_OUT = 2
    }
}

@MagicConstant(intValues = [StepRequest.STEP_INTO.toLong(), StepRequest.STEP_OVER.toLong(), StepRequest.STEP_OUT.toLong()])
konst depth: Int = 42

annotation class KotlinMagicConstant(konst intValues: LongArray)

@KotlinMagicConstant(intValues = [StepRequest.STEP_INTO.toLong(), StepRequest.STEP_OVER.toLong(), StepRequest.STEP_OUT.toLong()])
konst kotlinDepth: Int = 42
