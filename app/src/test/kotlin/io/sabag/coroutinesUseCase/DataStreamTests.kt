package io.sabag.coroutinesUseCase

import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class DataStreamTests {

    private lateinit var dataStream: DataStream<String>
    private lateinit var channel: BroadcastChannel<String>
    var response: String? = null
    var notifyCounter = 0

    @Before
    fun setupTest() {
        val runner = LaunchTaskRunner()
        channel = BroadcastChannel(Channel.CONFLATED)
        notifyCounter = 0
        dataStream = DataStream(runner, channel)
        dataStream.notifyOnChange {
            response = it
            notifyCounter++
        }
    }

    @Test
    fun whenSendNewValueShouldNotifyThisValue() {
        // act
        sendNewValue("test")

        // verify
        assertEquals("test", response)
    }

    @Test
    fun whenSendSeveralValueShouldNotifyLastValue() {
        // act
        sendNewValue("tests")
        sendNewValue("are")
        sendNewValue("fun")

        // verify
        assertEquals("fun", response)
    }

    @Test
    fun whenPauseShouldNotifyLastValueBeforePause() {
        // prepare
        sendNewValue("tests")
        sendNewValue("are")

        // act
        dataStream.pause()
        sendNewValue("fun")

        // verify
        assertEquals("are", response)
    }

    @Test
    fun whenPauseAndResumeShouldNotifyValueAfterResumeAndNotNotifyValueDuringPause() {
        // act
        sendNewValue("tests")
        dataStream.pause()
        sendNewValue("are")
        sendNewValue("fun")
        dataStream.resume()
        runBlocking { delay(10) }


        // verify
        assertEquals("fun", response)
        assertEquals(2, notifyCounter)
    }

    private fun sendNewValue(value: String) {
        runBlocking {
            channel.send(value)
            delay(10)
        }
    }
}