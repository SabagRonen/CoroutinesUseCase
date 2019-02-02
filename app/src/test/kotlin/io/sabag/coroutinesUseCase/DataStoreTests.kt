package io.sabag.coroutinesUseCase

import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class DataStoreTests {

    lateinit var dataStore : DataStore<Int>

    @Before
    fun setupTest() {
        val runner = LaunchTaskRunner()
        dataStore = DataStore(runner)
    }

    @After
    fun clearResources() {
        dataStore.shutdown()
    }

    @Test
    fun notifyAndThanSetValue() {
        val stopper = CountDownLatch(1)
        var receivedValue = 0

        dataStore.getDataStream().notifyOnChange { value ->
            receivedValue = value
            stopper.countDown()
        }

        dataStore.setCurrentValue(5)

        stopper.await()
        assertEquals(5, receivedValue)
    }

    @Test
    fun shouldReturnValueIfHasOne() {
        val stopper = CountDownLatch(1)
        var receivedValue = 0

        dataStore.setCurrentValue(5)
        dataStore.getDataStream().notifyOnChange { value ->
            receivedValue = value
            stopper.countDown()
        }

        stopper.await()
        assertEquals(5, receivedValue)
    }

    @Test
    fun dataStreamShouldSupportSeveralObservers() {
        val stopper = CountDownLatch(2)
        var firstReceivedValue = 0
        var secondReceivedValue = 0

        dataStore.setCurrentValue(2)
        dataStore.getDataStream().notifyOnChange { value ->
            firstReceivedValue = value
            stopper.countDown()
        }

        dataStore.getDataStream().notifyOnChange { value ->
            secondReceivedValue = value
            stopper.countDown()
        }

        stopper.await()
        assertEquals(2, firstReceivedValue)
        assertEquals(2, secondReceivedValue)
    }

    @Test
    fun shouldBeNotifyUponAnyChangeAfterStartingToObserve() = runBlocking {
        val firstStopper = CountDownLatch(1)
        val secondStopper = CountDownLatch(1)
        var receivedValue = 0

        dataStore.setCurrentValue(7)
        dataStore.getDataStream().notifyOnChange { value ->
            receivedValue = value
            if (value == 7) {
                firstStopper.countDown()
            } else {
                secondStopper.countDown()
            }
        }

        firstStopper.await()
        assertEquals(7, receivedValue)

        dataStore.setCurrentValue(4)
        secondStopper.await()
        assertEquals(4, receivedValue)
    }

//    @Test
    fun shouldReturnLastValueIfHasMoreThanOne() {
        val stopper = CountDownLatch(1)
        var receivedValue = 0

        dataStore.setCurrentValue(5)
        Thread.yield()
        dataStore.setCurrentValue(8)
        dataStore.getDataStream().notifyOnChange { value ->
            receivedValue = value
            stopper.countDown()
        }

        stopper.await()
        assertEquals(8, receivedValue)
    }

    @Test
    fun test() {
        var stopper = CountDownLatch(1)
        var receivedValue = 0

        val dataStream = dataStore.getDataStream()
        dataStream.notifyOnChange { value ->
            receivedValue = value
            stopper.countDown()
        }

        dataStore.setCurrentValue(5)
        stopper.await()
        stopper = CountDownLatch(1)
        dataStream.close()
        dataStore.setCurrentValue(8)
        stopper.await(10, TimeUnit.MILLISECONDS)

        assertEquals(5, receivedValue)
    }
}