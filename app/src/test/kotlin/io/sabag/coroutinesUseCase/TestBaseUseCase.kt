package io.sabag.coroutinesUseCase

import kotlinx.coroutines.delay

enum class TestUseCaseMode { SYNCHRONOUS, ASYNCHRONOUS }
enum class TestUseCaseAsynchronousMode { WAIT_FOR_ALL, WAIT_FOR_FIRST_END, WAIT_FOR_FIRST_TWO_END }

class TestUseCaseRequest {
    lateinit var mode : TestUseCaseMode
    var asynchronousMode = TestUseCaseAsynchronousMode.WAIT_FOR_ALL
    var value = 0
    var errorMsg : String? = null
}

class TestUseCaseResponse {
    var value = 0
}

class TestBaseUseCase : BaseUseCase<TestUseCaseRequest, TestUseCaseResponse>() {
    override fun provideNewRequest() = TestUseCaseRequest()

    override fun handleRequest(request: TestUseCaseRequest) {
        val response = TestUseCaseResponse()
        when (request.mode) {
            TestUseCaseMode.SYNCHRONOUS -> {
                if (request.errorMsg != null) {
                    throw Throwable(request.errorMsg)
                } else {
                    response.value = request.value
                }
            }

            TestUseCaseMode.ASYNCHRONOUS -> {
                var value = 0
                val shortestTask = inParallel {
                    delay(30)
                    value += 2
                }

                val middleTask = inParallel {
                    delay(160)
                    value += 3
                }

                val longestTask = inParallel {
                    delay(290)
                    value += 4
                }

                when (request.asynchronousMode) {
                    TestUseCaseAsynchronousMode.WAIT_FOR_ALL ->
                        waitForAll(listOf(shortestTask, middleTask, longestTask))

                    TestUseCaseAsynchronousMode.WAIT_FOR_FIRST_END ->
                        waitForFirst(listOf(shortestTask, middleTask, longestTask))

                    TestUseCaseAsynchronousMode.WAIT_FOR_FIRST_TWO_END -> {TODO("not implemented yet")}
                }

                response.value = value
            }
        }
        request.responseHandler?.invoke(response)
    }
}