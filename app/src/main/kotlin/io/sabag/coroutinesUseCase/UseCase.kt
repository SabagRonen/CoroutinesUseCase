package io.sabag.coroutinesUseCase

import kotlinx.coroutines.*
import kotlinx.coroutines.selects.select
import java.io.Closeable

typealias ErrorHandler = (Throwable) -> Unit
typealias CancelHandler = () -> Unit

interface UseCase<in Request> {
    fun handleRequest(request: Request)
}

abstract class UseCasePauseManager {
    var pauseAble: PauseAble? = null
}

@DslMarker
annotation class UseCaseMarker

@UseCaseMarker
abstract class BaseUseCase<Request, Response> : UseCase<Request>, PauseAble {
    val request : Request by lazy {
        provideNewRequest()
    }
    var taskRunner : TaskRunner = LaunchTaskRunner()
    var useCasePauseManager : UseCasePauseManager? = null
    private var errorHandlerBlock: ErrorHandler? = null
    private var cancelHandler: CancelHandler? = null
    private val closeableList = mutableListOf<Closeable>(taskRunner)


    abstract fun provideNewRequest() : Request
    operator fun Request.invoke(function: Request.() -> Unit) {
        function()
    }

    var responseHandler : ((Response) -> Unit)? = null
    var Request.responseHandler: ((Response) -> Unit)?
        get() = this@BaseUseCase.responseHandler
        set(value) { this@BaseUseCase.responseHandler = value }

    operator fun invoke(function: BaseUseCase<Request, Response>.() -> Unit) {
        function()
        useCasePauseManager?.pauseAble = this
        taskRunner.run {
            try {
                handleRequest(request)
            } catch (throwable: Throwable) {
                errorHandlerBlock?.let { block -> block(throwable) }
            }
        }
    }

    fun onError(function: (Throwable) -> Unit) {
        errorHandlerBlock = function
    }

    fun onCancel(function: () -> Unit) {
        cancelHandler = function
    }

    override fun resume() {
        closeableList.forEach {
            (it as? PauseAble)?.resume()
        }
    }

    override fun pause() {
        closeableList.forEach {
            (it as? PauseAble)?.pause()
        }
    }

    override fun close() {
        cancelHandler?.let{it()}
        closeableList.forEach { it.close() }
    }

    protected fun inParallel(function: suspend () -> Unit) : TaskRunner =
        LaunchTaskRunner().apply {
            closeableList.add(this)
            run {
                function()
            }
        }

    protected fun waitForAll(tasks: List<TaskRunner>) = runBlocking() {
        for (task in tasks) {
            task.waitForThisTask()
        }
    }

    protected fun waitForFirst(tasks: List<TaskRunner>) = runBlocking() {
        select<Int> {
            tasks.forEachIndexed{index, task -> launch { task.waitForThisTask() }.onJoin{
                tasks.forEach { endTask -> endTask.close() }
                index
            }}
        }
    }

    protected fun <R : Closeable> withCloseable(closeable: R) = closeable.apply {
        closeableList.add(closeable)
    }
}