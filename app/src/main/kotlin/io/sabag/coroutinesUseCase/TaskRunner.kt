package io.sabag.coroutinesUseCase

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.Closeable

interface TaskRunner : Closeable {
    fun run(block: suspend () -> Unit)
    suspend fun waitForThisTask()
}

abstract class JobTaskRunner : TaskRunner {
    var job: Job? = null
    override fun close() {
        if (job?.isActive == true) {
            job?.cancel()
        }
    }
}

class LaunchTaskRunner : JobTaskRunner() {
    override fun run(block: suspend () -> Unit) {
        job = GlobalScope.launch {
            block();
        }
    }

    override suspend fun waitForThisTask() {
        job?.join()
    }
}

class RunBlockingTaskRunner : JobTaskRunner() {
    override fun run(block: suspend () -> Unit) {
        runBlocking {
            block();
        }
    }

    override suspend fun waitForThisTask() {}
}