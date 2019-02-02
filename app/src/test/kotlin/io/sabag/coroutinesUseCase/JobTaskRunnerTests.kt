package io.sabag.coroutinesUseCase

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class JobTaskRunnerTests {
    @Test
    fun launchRunnerIsWaiting() = runBlocking {
        val runner = LaunchTaskRunner()
        var value = 5
        runner.run {
            delay(100)
            value = 6
        }

        runner.waitForThisTask()

        assertEquals(6, value)
    }

    @Test
    fun launchRunnerCanBeCanceled() = runBlocking {
        val runner = LaunchTaskRunner()
        var value = 5

        runner.run {
            delay(100)
            value = 6
        }

        runner.close()

        assertEquals(5, value)
    }

    @Test
    fun runBlockingRunnerIsBlocking() {
        val runner = RunBlockingTaskRunner()
        var value = 5

        runner.run {
            delay(100)
            value = 6
        }

        assertEquals(6, value)
    }

    @Test
    fun runBlockingRunnerCanNotBeCanceled() {
        val runner = RunBlockingTaskRunner()
        var value = 5

        runner.run {
            delay(100)
            value = 6
        }

        runner.close()

        assertEquals(6, value)
    }
}