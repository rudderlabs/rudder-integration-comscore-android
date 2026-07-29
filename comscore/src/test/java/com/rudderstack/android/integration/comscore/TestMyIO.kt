package com.rudderstack.android.integration.comscore

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue

internal class TestMyIO(
    private val classLoader: ClassLoader?,
    private val gson: Gson = GsonBuilder().create()
) {
    fun <I, O> test(
        inputJsonPath: String,
        expectedOutputJsonPath: String,
        inputClass: Class<I>,
        outputClass: Class<O>,
        operation: (I) -> O
    ) {
        val inputJson = readResource(inputJsonPath)
        assertThat("Input path is wrong", inputJson, notNullValue())

        val expectedOutputJson = readResource(expectedOutputJsonPath)
        assertThat("Output path is wrong", expectedOutputJson, notNullValue())

        val input = gson.fromJson(inputJson, inputClass)
        assertThat("Input can't be parsed: $inputJson", input, notNullValue())

        val expectedOutput = gson.fromJson(expectedOutputJson, outputClass)
        assertThat(
            "Expected output can't be parsed: $expectedOutputJson",
            expectedOutput,
            notNullValue()
        )

        assertThat(operation(input), equalTo(expectedOutput))
    }

    private fun readResource(path: String): String? =
        classLoader?.getResourceAsStream(path)?.bufferedReader()?.use { it.readText() }
}
