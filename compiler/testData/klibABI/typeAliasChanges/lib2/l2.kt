open class OpenClassRemovedTAImpl(x: Int) : OpenClassRemovedTA(-x)
data class OpenClassRemovedTATypeParameterHolder<T : OpenClassRemovedTA>(konst t: T)
data class OpenClassRemovedTAImplTypeParameterHolder<T : OpenClassRemovedTAImpl>(konst t: T)

fun getOpenClassRemovedTA(x: Int): OpenClassRemovedTA = OpenClassRemovedTA(x)
fun setOpenClassRemovedTA(konstue: OpenClassRemovedTA?): String = konstue?.toString() ?: "setOpenClassRemovedTA"
fun getOpenClassRemovedTAImpl(x: Int): OpenClassRemovedTA = OpenClassRemovedTAImpl(x)
fun setOpenClassRemovedTAImpl(konstue: OpenClassRemovedTAImpl?): String = konstue?.toString() ?: "setOpenClassRemovedTAImpl"

fun getOpenClassRemovedTATypeParameterHolder1(x: Int): OpenClassRemovedTATypeParameterHolder<OpenClassRemovedTA> = OpenClassRemovedTATypeParameterHolder(OpenClassRemovedTA(x))
fun getOpenClassRemovedTATypeParameterHolder2(x: Int): OpenClassRemovedTATypeParameterHolder<OpenClassRemovedTAImpl> = OpenClassRemovedTATypeParameterHolder(OpenClassRemovedTAImpl(x))
fun setOpenClassRemovedTATypeParameterHolder1(konstue: OpenClassRemovedTATypeParameterHolder<OpenClassRemovedTA>?): String = konstue?.toString() ?: "setOpenClassRemovedTATypeParameterHolder1"
fun setOpenClassRemovedTATypeParameterHolder2(konstue: OpenClassRemovedTATypeParameterHolder<OpenClassRemovedTAImpl>?): String = konstue?.toString() ?: "setOpenClassRemovedTATypeParameterHolder2"

fun getOpenClassRemovedTAImplTypeParameterHolder(x: Int): OpenClassRemovedTAImplTypeParameterHolder<OpenClassRemovedTAImpl> = OpenClassRemovedTAImplTypeParameterHolder(OpenClassRemovedTAImpl(x))
fun setOpenClassRemovedTAImplTypeParameterHolder(konstue: OpenClassRemovedTAImplTypeParameterHolder<OpenClassRemovedTAImpl>?): String = konstue?.toString() ?: "setOpenClassRemovedTAImplTypeParameterHolder"

open class OpenClassChangedTAImpl(x: Int) : OpenClassChangedTA(-x)
data class OpenClassChangedTATypeParameterHolder<T : OpenClassChangedTA>(konst t: T)
data class OpenClassChangedTAImplTypeParameterHolder<T : OpenClassChangedTAImpl>(konst t: T)

fun getOpenClassChangedTA(x: Int): OpenClassChangedTA = OpenClassChangedTA(x)
fun setOpenClassChangedTA(konstue: OpenClassChangedTA?): String = konstue?.toString() ?: "setOpenClassChangedTA"
fun getOpenClassChangedTAImpl(x: Int): OpenClassChangedTA = OpenClassChangedTAImpl(x)
fun setOpenClassChangedTAImpl(konstue: OpenClassChangedTAImpl?): String = konstue?.toString() ?: "setOpenClassChangedTAImpl"

fun getOpenClassChangedTATypeParameterHolder1(x: Int): OpenClassChangedTATypeParameterHolder<OpenClassChangedTA> = OpenClassChangedTATypeParameterHolder(OpenClassChangedTA(x))
fun getOpenClassChangedTATypeParameterHolder2(x: Int): OpenClassChangedTATypeParameterHolder<OpenClassChangedTAImpl> = OpenClassChangedTATypeParameterHolder(OpenClassChangedTAImpl(x))
fun setOpenClassChangedTATypeParameterHolder1(konstue: OpenClassChangedTATypeParameterHolder<OpenClassChangedTA>?): String = konstue?.toString() ?: "setOpenClassChangedTATypeParameterHolder1"
fun setOpenClassChangedTATypeParameterHolder2(konstue: OpenClassChangedTATypeParameterHolder<OpenClassChangedTAImpl>?): String = konstue?.toString() ?: "setOpenClassChangedTATypeParameterHolder2"

fun getOpenClassChangedTAImplTypeParameterHolder(x: Int): OpenClassChangedTAImplTypeParameterHolder<OpenClassChangedTAImpl> = OpenClassChangedTAImplTypeParameterHolder(OpenClassChangedTAImpl(x))
fun setOpenClassChangedTAImplTypeParameterHolder(konstue: OpenClassChangedTAImplTypeParameterHolder<OpenClassChangedTAImpl>?): String = konstue?.toString() ?: "setOpenClassChangedTAImplTypeParameterHolder"

open class OpenClassNarrowedVisibilityTAImpl(x: Int) : OpenClassNarrowedVisibilityTA(-x)
data class OpenClassNarrowedVisibilityTATypeParameterHolder<T : OpenClassNarrowedVisibilityTA>(konst t: T)
data class OpenClassNarrowedVisibilityTAImplTypeParameterHolder<T : OpenClassNarrowedVisibilityTAImpl>(konst t: T)

fun getOpenClassNarrowedVisibilityTA(x: Int): OpenClassNarrowedVisibilityTA = OpenClassNarrowedVisibilityTA(x)
fun setOpenClassNarrowedVisibilityTA(konstue: OpenClassNarrowedVisibilityTA?): String = konstue?.toString() ?: "setOpenClassNarrowedVisibilityTA"
fun getOpenClassNarrowedVisibilityTAImpl(x: Int): OpenClassNarrowedVisibilityTA = OpenClassNarrowedVisibilityTAImpl(x)
fun setOpenClassNarrowedVisibilityTAImpl(konstue: OpenClassNarrowedVisibilityTAImpl?): String = konstue?.toString() ?: "setOpenClassNarrowedVisibilityTAImpl"

fun getOpenClassNarrowedVisibilityTATypeParameterHolder1(x: Int): OpenClassNarrowedVisibilityTATypeParameterHolder<OpenClassNarrowedVisibilityTA> = OpenClassNarrowedVisibilityTATypeParameterHolder(OpenClassNarrowedVisibilityTA(x))
fun getOpenClassNarrowedVisibilityTATypeParameterHolder2(x: Int): OpenClassNarrowedVisibilityTATypeParameterHolder<OpenClassNarrowedVisibilityTAImpl> = OpenClassNarrowedVisibilityTATypeParameterHolder(OpenClassNarrowedVisibilityTAImpl(x))
fun setOpenClassNarrowedVisibilityTATypeParameterHolder1(konstue: OpenClassNarrowedVisibilityTATypeParameterHolder<OpenClassNarrowedVisibilityTA>?): String = konstue?.toString() ?: "setOpenClassNarrowedVisibilityTATypeParameterHolder1"
fun setOpenClassNarrowedVisibilityTATypeParameterHolder2(konstue: OpenClassNarrowedVisibilityTATypeParameterHolder<OpenClassNarrowedVisibilityTAImpl>?): String = konstue?.toString() ?: "setOpenClassNarrowedVisibilityTATypeParameterHolder2"

fun getOpenClassNarrowedVisibilityTAImplTypeParameterHolder(x: Int): OpenClassNarrowedVisibilityTAImplTypeParameterHolder<OpenClassNarrowedVisibilityTAImpl> = OpenClassNarrowedVisibilityTAImplTypeParameterHolder(OpenClassNarrowedVisibilityTAImpl(x))
fun setOpenClassNarrowedVisibilityTAImplTypeParameterHolder(konstue: OpenClassNarrowedVisibilityTAImplTypeParameterHolder<OpenClassNarrowedVisibilityTAImpl>?): String = konstue?.toString() ?: "setOpenClassNarrowedVisibilityTAImplTypeParameterHolder"
