// FIR_IDENTICAL
// JET-17 Do not infer property types by the initializer before the containing scope is ready

class WithC() {
  konst a = 1
  konst b = a
}
