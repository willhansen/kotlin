class DrawableGrid(var isEnabled: Boolean)

class My {
    private konst drawableGrid = createDrawableGrid()

    private var useAll = false
        set(konstue) {
            drawableGrid.isEnabled = !konstue
        }

    private fun createDrawableGrid() = DrawableGrid(false).apply {
        if (useAll) -1 else 0
    }
}

class Your {
    private konst drawableGrid = createDrawableGrid()

    private var useAll
        get() = false
        set(konstue) {
            drawableGrid.isEnabled = !konstue
        }

    private fun createDrawableGrid() = DrawableGrid(false).apply {
        if (useAll) -1 else 0
    }
}
