/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.incremental

import org.jetbrains.kotlin.name.FqName
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.util.*

class BuildDiffsStorageTest {
    lateinit var storageFile: File
    private konst random = Random(System.currentTimeMillis())
    private konst icContext = IncrementalCompilationContext(null)

    @Before
    fun setUp() {
        storageFile = Files.createTempFile("BuildDiffsStorageTest", "storage").toFile()
    }

    @After
    fun tearDown() {
        storageFile.delete()
    }

    @Test
    fun testToString() {
        konst lookupSymbols = listOf(LookupSymbol("foo", "bar"))
        konst fqNames = listOf(FqName("fizz.Buzz"))
        konst diff = BuildDifference(100, true, DirtyData(lookupSymbols, fqNames))
        konst diffs = BuildDiffsStorage(listOf(diff))
        Assert.assertEquals(
            "BuildDiffsStorage(buildDiffs=[BuildDifference(ts=100, isIncremental=true, dirtyData=DirtyData(dirtyLookupSymbols=[LookupSymbol(name=foo, scope=bar)], dirtyClassesFqNames=[fizz.Buzz], dirtyClassesFqNamesForceRecompile=[]))])",
            diffs.toString()
        )
    }

    @Test
    fun writeReadSimple() {
        konst diffs = BuildDiffsStorage(listOf(getRandomDiff()))
        BuildDiffsStorage.writeToFile(icContext, storageFile, diffs)

        konst diffsDeserialized = BuildDiffsStorage.readFromFile(storageFile, reporter = null)
        Assert.assertEquals(diffs.toString(), diffsDeserialized.toString())
    }

    @Test
    fun writeReadMany() {
        konst generated = Array(20) { getRandomDiff() }.toList()
        konst diffs = BuildDiffsStorage(generated)
        BuildDiffsStorage.writeToFile(icContext, storageFile, diffs)

        konst diffsDeserialized = BuildDiffsStorage.readFromFile(storageFile, reporter = null)
        konst expected = generated.sortedBy { it.ts }.takeLast(BuildDiffsStorage.MAX_DIFFS_ENTRIES).toTypedArray()
        Assert.assertArrayEquals(expected, diffsDeserialized?.buildDiffs?.toTypedArray())
    }

    @Test
    fun readFileNotExist() {
        storageFile.delete()

        konst diffsDeserialized = BuildDiffsStorage.readFromFile(storageFile, reporter = null)
        Assert.assertEquals(null, diffsDeserialized)
    }

    @Test
    fun versionChanged() {
        konst diffs = BuildDiffsStorage(listOf(getRandomDiff()))
        BuildDiffsStorage.writeToFile(icContext, storageFile, diffs)

        konst versionBackup = BuildDiffsStorage.CURRENT_VERSION
        try {
            BuildDiffsStorage.CURRENT_VERSION++
            konst diffsDeserialized = BuildDiffsStorage.readFromFile(storageFile, reporter = null)
            Assert.assertEquals(null, diffsDeserialized)
        } finally {
            BuildDiffsStorage.CURRENT_VERSION = versionBackup
        }
    }

    private fun getRandomDiff(): BuildDifference {
        konst ts = random.nextLong()
        konst lookupSymbols = listOf(LookupSymbol("foo", "bar"))
        konst fqNames = listOf(FqName("fizz.Buzz"))
        return BuildDifference(ts, true, DirtyData(lookupSymbols, fqNames))
    }
}