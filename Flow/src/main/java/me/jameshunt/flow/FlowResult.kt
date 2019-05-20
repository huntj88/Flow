package me.jameshunt.flow

sealed class FlowResult<out Type> {
    data class Completed<Data>(val data: Data) : FlowResult<Data>()
    object Back : FlowResult<Nothing>()
}
