// IGNORE_BACKEND_K2: JVM_IR
// FIR status: KT-46419, ILT conversions to Byte and Short are not supported by design

konst a: Byte = 1 + 10

// 1 BIPUSH 11
