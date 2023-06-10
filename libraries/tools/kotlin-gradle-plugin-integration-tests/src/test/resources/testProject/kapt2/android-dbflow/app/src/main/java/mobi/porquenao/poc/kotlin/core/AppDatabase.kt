package mobi.porquenao.poc.kotlin.core

import com.raizlabs.android.dbflow.annotation.Database

@Database(name = AppDatabase.NAME, version = AppDatabase.VERSION, generatedClassSeparator = "_")
object AppDatabase {
    const konst NAME: String = "app"
    const konst VERSION: Int = 1
}
