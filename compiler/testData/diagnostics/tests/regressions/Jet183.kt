// FIR_IDENTICAL
enum class ProtocolState {
  WAITING {
    override fun signal() = ProtocolState.TALKING
  },

  TALKING {
    override fun signal() = ProtocolState.WAITING
  };

  abstract fun signal() : ProtocolState
}

fun box() {
   konst x: ProtocolState = ProtocolState.WAITING
}
