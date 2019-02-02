package io.sabag.coroutinesUseCase

import org.junit.Assert.*
import org.junit.Test

class UseCaseTests {
    @Test
    fun verifyOnResponseCalledWithTheCorrectResponse() {
        val useCase = TestBaseUseCase()
        useCase {
            taskRunner = RunBlockingTaskRunner()
            request {
                mode = TestUseCaseMode.SYNCHRONOUS
                value = 10
                responseHandler = { response ->
                    assertEquals(10 , response.value)
                }
            }

            onError {
                fail()
            }
        }
    }

    @Test
    fun verifyOnErrorCalledWithTheCorrectMessage() {
        val useCase = TestBaseUseCase()
        useCase {
            taskRunner = RunBlockingTaskRunner()
            request {
                mode = TestUseCaseMode.SYNCHRONOUS
                errorMsg = "this is error message"
                responseHandler = { _ ->
                    fail()
                }
            }

            onError { throwable ->
                assertEquals("this is error message", throwable.message)
            }
        }
    }

    @Test
    fun verifyThatWaitForAllIsWorkingCorrectly() {
        val useCase = TestBaseUseCase()
        useCase {
            taskRunner = RunBlockingTaskRunner()
            request {
                mode = TestUseCaseMode.ASYNCHRONOUS
                asynchronousMode = TestUseCaseAsynchronousMode.WAIT_FOR_ALL
                value = 0
                responseHandler = { response ->
                    assertEquals(9, response.value)
                }
            }

            onError {
                fail()
            }
        }
    }

    @Test
    fun verifyThatWaitForFirstIsWorkingCorrectly() {
        val useCase = TestBaseUseCase()
        useCase {
            taskRunner = RunBlockingTaskRunner()
            request {
                mode = TestUseCaseMode.ASYNCHRONOUS
                asynchronousMode = TestUseCaseAsynchronousMode.WAIT_FOR_FIRST_END
                value = 0
                responseHandler = { response ->
                    assertEquals(2, response.value)
                }
            }

            onError {
                fail()
            }
        }
    }
}