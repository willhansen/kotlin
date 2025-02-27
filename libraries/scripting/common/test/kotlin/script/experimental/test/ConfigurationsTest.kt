/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.script.experimental.test

import junit.framework.TestCase
import org.junit.Test
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptCompilationConfigurationKeys
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.host.ScriptingHostConfigurationKeys
import kotlin.script.experimental.host.withDefaultsFrom
import kotlin.script.experimental.util.PropertiesCollection

class ConfigurationsTest : TestCase() {

    @Test
    fun testHostConfigWithDefaultsFrom() {
        konst c0 = ScriptingHostConfiguration()
        konst c1 = ScriptingHostConfiguration {
            p1(1)
        }
        konst c2 = ScriptingHostConfiguration {
            p1(2)
            p2("yes")
            p4("local")
        }

        assertEquals(c1, c1.withDefaultsFrom(c0))
        assertEquals(c1, c0.withDefaultsFrom(c1))
        assertEquals(c1, c1.withDefaultsFrom(c1))

        konst c1c2 = c1.withDefaultsFrom(c2)
        konst c2c1 = c2.withDefaultsFrom(c1)

        assertEquals(1, c1c2[ScriptingHostConfiguration.p1])
        assertEquals("yes", c1c2[ScriptingHostConfiguration.p2])
        assertEquals(2, c2c1[ScriptingHostConfiguration.p1])

        assertEquals("from delegated", c1[ScriptingHostConfiguration.p4])
        assertEquals("local", c2[ScriptingHostConfiguration.p4])
    }
}

private konst ScriptingHostConfigurationKeys.p1 by PropertiesCollection.key(-1)
private konst ScriptingHostConfigurationKeys.p2 by PropertiesCollection.key("-")

private konst ScriptCompilationConfigurationKeys.p3 by PropertiesCollection.key<String>()

private konst delegatedConfig = ScriptCompilationConfiguration {
    p3("from delegated")
}

private konst ScriptingHostConfigurationKeys.p4 by PropertiesCollection.keyCopy(ScriptCompilationConfiguration.p3, { delegatedConfig })

