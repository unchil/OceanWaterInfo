package com.unchil.oceanwaterinfo

import androidx.compose.runtime.Composable


sealed interface ChartViewContent

data class ChartViewCategory(
    val name: String,
) : ChartViewContent {
    override fun toString(): String = name
}


interface ChartView<S> : ChartViewContent {
    val name: String
    val hasOptions: Boolean get() = false

    @Composable
    fun rememberState(): S

    @Composable
    fun Content(state: S)

    @Composable
    fun Options(state: S) {
    }
}


interface SimpleChartView : ChartView<Unit> {
    @Composable
    override fun rememberState() = Unit

    @Composable
    override fun Content(state: Unit) = Content()

    @Composable
    fun Content()
}
