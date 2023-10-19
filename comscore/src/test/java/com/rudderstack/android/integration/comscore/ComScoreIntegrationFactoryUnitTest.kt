package com.rudderstack.android.integration.comscore

import com.comscore.Analytics
import com.comscore.Configuration
import com.google.gson.GsonBuilder
import com.rudderstack.android.sdk.core.RudderMessage
import com.rudderstack.android.test.testio.TestMyIO
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.doNothing
import org.mockito.MockitoAnnotations
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader


class ComscoreIntegrationFactoryTest {
    var comscoreIntegrationFactory: ComscoreIntegrationFactory? = null

    private val TEST_IDENTIFY_JSON_PATH_MAP_1 = mapOf(
        "identify_input.json" to "identify_output.json"
    )
    private val TEST_TRACK_JSON_PATH_MAP = mapOf(
        "track_input.json" to "track_output.json"
    )
    private val TEST_SCREEN_JSON_PATH_MAP = mapOf(
        "screen_input.json" to "screen_output.json"
    )

    val gson = GsonBuilder().create()
    var closeable: AutoCloseable? = null

    @Mock
    var configuration: Configuration? = null

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        closeable = MockitoAnnotations.openMocks(this)
        val configJson = getJsonFromPath("destinationConfig_whenAppNameIsEmpty.json")
            ?: throw Exception("Config json is null")
        val configObject = gson.fromJson(configJson, Map::class.java)
        this.comscoreIntegrationFactory = ComscoreIntegrationFactory(configuration, configObject)
    }

    @After
    fun destroy() {
        closeable?.close()
    }

    fun getJsonFromPath(path: String?): String? {
        val inputStream: InputStream =
            this.javaClass.classLoader?.getResourceAsStream(path) ?: return null
        val reader = BufferedReader(InputStreamReader(inputStream))
        val builder = StringBuilder()
        var line: String?
        try {
            while (reader.readLine().also { line = it } != null) {
                builder.append(line)
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
        return builder.toString()
    }

    @Test
    fun assertJsonIsReadCorrectly() {
        val firstInput = TEST_IDENTIFY_JSON_PATH_MAP_1.keys.toTypedArray()[0]
        MatcherAssert.assertThat(firstInput, Matchers.notNullValue())
        val inputStream = javaClass.classLoader?.getResourceAsStream(firstInput)
        MatcherAssert.assertThat(inputStream, Matchers.notNullValue())
    }

    private val testMyIO = TestMyIO(javaClass.classLoader)

    @Test
    fun testDestinationConfig_whenAppNameIsEmpty() {
        val configJson = getJsonFromPath("destinationConfig_whenAppNameIsEmpty.json")
        val config = gson.fromJson(configJson, Map::class.java)
        val destinationConfig: ComscoreDestinationConfig? = Utils.createConfig(config)

        assertEquals("1234566778", destinationConfig?.publisherID)
        assertNull(destinationConfig?.appName)
        assertEquals(false, destinationConfig?.isForegroundAndBackground)
        assertEquals(60, destinationConfig?.autoUpdateInterval)
        assertEquals(true, destinationConfig?.isUseHTTPS)
        assertEquals(true, destinationConfig?.isForegroundOnly)
    }

    @Test
    fun testDestinationConfig_whenAppNameIsNotEmpty() {
        val configJson = getJsonFromPath("destinationConfig_whenAppNameIsNotEmpty.json")
        val config = gson.fromJson(configJson, Map::class.java)
        val destinationConfig: ComscoreDestinationConfig? = Utils.createConfig(config)

        assertEquals("1234566778", destinationConfig?.publisherID)
        assertEquals("Sample Android app", destinationConfig?.appName)
        assertEquals(false, destinationConfig?.isForegroundAndBackground)
        assertEquals(60, destinationConfig?.autoUpdateInterval)
        assertEquals(true, destinationConfig?.isUseHTTPS)
        assertEquals(true, destinationConfig?.isForegroundOnly)
    }

    @Test
    fun testDestinationConfig_whenAutoUpdateIntervalIsNotEmpty() {
        val configJson = getJsonFromPath("destinationConfig_whenAutoUpdateIntervalIsNotEmpty.json")
        val config = gson.fromJson(configJson, Map::class.java)
        val destinationConfig: ComscoreDestinationConfig? = Utils.createConfig(config)

        assertEquals("1234566778", destinationConfig?.publisherID)
        assertEquals("Sample Android app", destinationConfig?.appName)
        assertEquals(false, destinationConfig?.isForegroundAndBackground)
        assertEquals(67, destinationConfig?.autoUpdateInterval)
        assertEquals(true, destinationConfig?.isUseHTTPS)
        assertEquals(true, destinationConfig?.isForegroundOnly)
    }

    @Test
    fun testDestinationConfig_whenForegroundIsFalse() {
        val configJson = getJsonFromPath("destinationConfig_whenForegroundIsFalse.json")
        val config = gson.fromJson(configJson, Map::class.java)
        val destinationConfig: ComscoreDestinationConfig? = Utils.createConfig(config)

        assertEquals("1234566778", destinationConfig?.publisherID)
        assertEquals("Sample Android app", destinationConfig?.appName)
        assertEquals(false, destinationConfig?.isForegroundAndBackground)
        assertEquals(67, destinationConfig?.autoUpdateInterval)
        assertEquals(true, destinationConfig?.isUseHTTPS)
        assertEquals(false, destinationConfig?.isForegroundOnly)
    }

    @Test
    fun testDestinationConfig_whenForegroundAndBackgroundIsTrue() {
        val configJson = getJsonFromPath("destinationConfig_whenForegroundAndBackgroundIsTrue.json")
        val config = gson.fromJson(configJson, Map::class.java)
        val destinationConfig: ComscoreDestinationConfig? = Utils.createConfig(config)

        assertEquals("1234566778", destinationConfig?.publisherID)
        assertEquals("Sample Android app", destinationConfig?.appName)
        assertEquals(true, destinationConfig?.isForegroundAndBackground)
        assertEquals(67, destinationConfig?.autoUpdateInterval)
        assertEquals(true, destinationConfig?.isUseHTTPS)
        assertEquals(false, destinationConfig?.isForegroundOnly)
    }

    @Test
    fun identify() {
        for ((inputJsonPath, outputJsonPath) in TEST_IDENTIFY_JSON_PATH_MAP_1) {
            val identifyIdCaptor = ArgumentCaptor.forClass(Map::class.java) as ArgumentCaptor<Map<String, String>>

            doNothing().`when`(this.configuration)?.addPersistentLabels(identifyIdCaptor.capture())

            testMyIO.test(
                inputJsonPath, outputJsonPath,
                RudderMessage::class.java,
                TestIdentify::class.java
            ) { input ->
                comscoreIntegrationFactory?.dump(input)
                TestIdentify(
                    identifyIdCaptor.allValues
                )
            }
        }
    }

    @Test
    fun testTrack() {
        for ((inputJsonPath, outputJsonPath) in TEST_TRACK_JSON_PATH_MAP) {
            val trackCaptor = ArgumentCaptor.forClass(Map::class.java) as ArgumentCaptor<Map<String, String>>
            val mockedStatic: MockedStatic<Analytics> = Mockito.mockStatic(Analytics::class.java)
            mockedStatic.`when`<Any> { Analytics.notifyHiddenEvent(trackCaptor.capture()) }.thenCallRealMethod()

            testMyIO.test(
                inputJsonPath, outputJsonPath,
                RudderMessage::class.java,
                TestTrack::class.java
            ) { input ->
                comscoreIntegrationFactory?.dump(input)
                TestTrack(
                    trackCaptor.allValues
                )
            }

            mockedStatic.close()
        }
    }

    @Test
    fun testScreen() {
        for ((inputJsonPath, outputJsonPath) in TEST_SCREEN_JSON_PATH_MAP) {
            val screenCaptor = ArgumentCaptor.forClass(Map::class.java) as ArgumentCaptor<Map<String, String>>
            val mockedStatic: MockedStatic<Analytics> = Mockito.mockStatic(Analytics::class.java)
            mockedStatic.`when`<Any> { Analytics.notifyViewEvent(screenCaptor.capture()) }.thenCallRealMethod()

            testMyIO.test(
                inputJsonPath, outputJsonPath,
                RudderMessage::class.java,
                TestScreen::class.java
            ) { input ->
                comscoreIntegrationFactory?.dump(input)
                TestScreen(
                    screenCaptor.allValues
                )
            }

            mockedStatic.close()
        }
    }

    @Test
    fun testReset() {
        val identifyIdCaptor = ArgumentCaptor.forClass(Map::class.java) as ArgumentCaptor<Map<String, String>>
        doNothing().`when`(this.configuration)?.addPersistentLabels(identifyIdCaptor.capture())
        val message: RudderMessage = getRudderMessage("identify_input.json", RudderMessage::class.java)

        this.comscoreIntegrationFactory?.dump(message)
        var traits: Any? = identifyIdCaptor.allValues

        doAnswer {
            null.also { traits = it }
        }.`when`(this.configuration)?.removeAllPersistentLabels()
        this.comscoreIntegrationFactory?.reset()

        assertNull(traits)
    }

    private fun getRudderMessage(inputJsonPath: String, inputClass: Class<RudderMessage>): RudderMessage {
        val inputJson = getJsonFromPath(inputJsonPath)
        return parseJson(inputJson!!, inputClass)
    }

    private fun <I> parseJson(json: String, classType: Class<I>): I {
        return gson.fromJson(json, classType)
    }
}