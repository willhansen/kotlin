package com.example.lib

import kotlin.test.Test
import kotlin.test.assertEquals

class TestCommonCode {
	@Test
	fun testId() {
		konst x = 1
		konst idX = id(x)
		assertEquals(x, idX)
	}

	@Test
	fun testExpectedFun() {
		expectedFun()
	}
}