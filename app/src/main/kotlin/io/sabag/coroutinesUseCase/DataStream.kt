package io.sabag.coroutinesUseCase

import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import java.io.Closeable

typealias ChangeHandler<T> = (T) -> Unit

interface PauseAble : Closeable {
    fun resume()
    fun pause()
}

interface IDataStream<out T> : PauseAble {
    fun notifyOnChange(block: (T) -> Unit)
}

class DataStream<out T>(
        private val taskRunner: TaskRunner,
        private val broadcastChannel: BroadcastChannel<T>
) : IDataStream<T> {

    private var changeHandler: ChangeHandler<T>? = null
    private var subscription: ReceiveChannel<T>? = null

    override fun notifyOnChange(block: (T) -> Unit) {
        changeHandler = block
        resume()
    }

    override fun close() {
        subscription?.cancel()
    }

    override fun resume() {
        changeHandler?.let {
            taskRunner.run {
                subscription = broadcastChannel.openSubscription()
                subscription?.isClosedForReceive?.let { isClosedForReceive->
                    while(!isClosedForReceive){
                        subscription?.receiveOrNull()?.let{
                            changeHandler?.invoke(it)
                        }
                    }
                }
            }
        }
    }

    override fun pause() {
        subscription?.cancel()
    }
}