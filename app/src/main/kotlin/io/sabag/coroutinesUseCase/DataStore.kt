package io.sabag.coroutinesUseCase

import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel

interface IDataStore<T> {
    fun setCurrentValue(value: T)
    fun shutdown()
    fun getDataStream() : IDataStream<T>
}

open class DataStore<T : Any> @JvmOverloads constructor (
        private val taskRunner: TaskRunner = LaunchTaskRunner()
) : IDataStore<T> {

    private val output = BroadcastChannel<T>(Channel.CONFLATED)


    override fun setCurrentValue(value: T) {
        taskRunner.run {
            output.send(value)
        }
    }

    override fun shutdown() {
        taskRunner.run {
            output.close()
        }
    }

    override fun getDataStream() : IDataStream<T> = DataStream(taskRunner, output)
}