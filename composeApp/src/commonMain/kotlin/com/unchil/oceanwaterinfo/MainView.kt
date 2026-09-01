package com.unchil.oceanwaterinfo

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldDestinationItem
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldValue
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.rememberPaneExpansionState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.koalaplot.core.style.KoalaPlotTheme
import io.github.vooft.compose.treeview.core.TreeView
import io.github.vooft.compose.treeview.core.node.Branch
import io.github.vooft.compose.treeview.core.node.Leaf
import io.github.vooft.compose.treeview.core.tree.Tree


@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun envObservationTree(): Tree<ChartViewContent> = Tree {

    Branch(ChartViewCategory("Ocean Water Quality")) {
        Leaf<ChartViewContent>(WaterDegTimeSeries_KHOA)
        Leaf<ChartViewContent>(OceanWaterInfoTimeSeries)
        Leaf<ChartViewContent>(OceanWaterInfoBoxPlotChart)
        Leaf<ChartViewContent>(OceanWaterInfoBarChart)
        Leaf<ChartViewContent>(OceanWaterInfoGeoChart)
    }

    Branch(ChartViewCategory("Hydro Nuclear Power")) {
        Leaf<ChartViewContent>(WindPolarChart_KHNP)
        Leaf<ChartViewContent>(NuclearPlantStatePieChart_KHNP)
        Leaf<ChartViewContent>(RadioActiveWastePlantStatStackedBarChart_KHNP)
        Leaf<ChartViewContent>(KHNPRadioActiveWasteStackBarChart)
        Leaf<ChartViewContent>(WaterTempTimeSeries_KHOA)
        Leaf<ChartViewContent>(RadioRateBarChart)
        Leaf<ChartViewContent>(WasteWaterTimeSeries_KHNP)
        Leaf<ChartViewContent>(ThermalWasteWaterTimeSeries_KHNP)
    }

    if(getPlatform().alias.equals(PlatformAlias.ANDROID)){

        Leaf<ChartViewContent>(TestWebViewScreen)
    }else{
        Branch(ChartViewCategory("Prediction Information Map")){
            Leaf<ChartViewContent>(AirQuality)
            Leaf<ChartViewContent>(CoastalFloodingMap)
            Leaf<ChartViewContent>(TidalForecastMap)
            Leaf<ChartViewContent>(OceanCurrentSpeedMap)
        }
    }




    Leaf<ChartViewContent>(OceanWaterInfoDataGrid)

}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun MainView(modifier: Modifier = Modifier) {

    MaterialTheme(
        typography = getTypography(),
        colorScheme = getColorScheme(false)
    ){

        KoalaPlotTheme {
            val samplesTree = envObservationTree()
            var selectedItem by remember { mutableStateOf<ChartView<*>?>(null) }
            var showOptions by remember { mutableStateOf(false) }

            val directive = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo(true)).copy(
                horizontalPartitionSpacerSize = 0.dp,
            )
            val destination = remember(selectedItem, showOptions, directive.maxHorizontalPartitions) {
                if (selectedItem == null) {
                    ThreePaneScaffoldDestinationItem(ListDetailPaneScaffoldRole.List, contentKey = null)
                } else if (showOptions && directive.maxHorizontalPartitions < 3) {
                    ThreePaneScaffoldDestinationItem(ListDetailPaneScaffoldRole.Extra, contentKey = selectedItem)
                } else {
                    ThreePaneScaffoldDestinationItem(ListDetailPaneScaffoldRole.Detail, contentKey = selectedItem)
                }
            }

            val scaffoldState = ChartScaffoldState(
                scaffoldValue = computeThreePaneScaffoldValue(
                    directive,
                    destination,
                    selectedItem,
                    showOptions
                ),
                directive = directive,
                onSelect = { selectedItem = it },
                onShowOptions = { showOptions = it },
            )

            EnvObservationScaffold(
                samplesTree,
                selectedItem,
                scaffoldState,
                modifier
            )

        }
    }
}




@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun <S> EnvObservationScaffold(
    samplesTree: Tree<ChartViewContent>,
    item: ChartView<S>?,
    scaffoldState: ChartScaffoldState,
    modifier: Modifier,
) {
    val listState: LazyListState = rememberLazyListState()
    val state = key(item) { item?.rememberState() }

    // 1. 드래그 상태 기억
    val expansionState = rememberPaneExpansionState(scaffoldState.scaffoldValue)


    ListDetailPaneScaffold(
        directive = scaffoldState.directive,
        value = scaffoldState.scaffoldValue,
        // 2. 상태 전달
        paneExpansionState = expansionState,
        listPane = {
            AnimatedPane {
                Row(Modifier.fillMaxSize()) {
                    TreeView(
                        samplesTree,
                        modifier = Modifier.weight(1f),
                        onClick = { node ->
                            val content = node.content
                            if (content is ChartView<*>) {
                                scaffoldState.onSelect(content)
                                scaffoldState.onShowOptions(false)
                            } else {
                                samplesTree.toggleExpansion(node)
                            }
                        },
                        listState = listState,
                    )
                    if (scaffoldState.scaffoldValue.secondary == PaneAdaptedValue.Expanded &&
                        scaffoldState.scaffoldValue.primary == PaneAdaptedValue.Expanded
                    ) {
                        VerticalDivider()
                    }
                }
            }
        },
        detailPane = {
            AnimatedPane {
                Row(Modifier.fillMaxSize()) {
                    ChartPaneView(item, state, false, scaffoldState, Modifier.weight(1f))
                    if (scaffoldState.scaffoldValue.primary == PaneAdaptedValue.Expanded &&
                        scaffoldState.scaffoldValue.tertiary == PaneAdaptedValue.Expanded
                    ) {
                        VerticalDivider()
                    }
                }
            }
        },
        modifier = modifier,
        extraPane = {
            AnimatedPane {
                ChartPaneView(item, state, true, scaffoldState)
            }
        },
        // 1. 람다 매개변수로 state(PaneExpansionState)를 받습니다.
        paneExpansionDragHandle = { state ->
            // 2. 수동으로 드래그 양을 전달하는 로직은 paneExpansionDraggable 모디파이어가 처리합니다.
            VerticalDivider(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    // ThreePaneScaffoldScope 내에서 제공되는 확장 모디파이어 사용
                    .paneExpansionDraggable(
                        state = state,
                        interactionSource=  MutableInteractionSource(),
                        // 터치 영역을 확장하고 싶다면 아래 값 조절 (기본값 사용 가능)
                         minTouchTargetSize = 24.dp
                    ),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        },
    )
}




@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun <S> ChartPaneView(
    item: ChartView<S>?,
    state: S?,
    isOptions: Boolean,
    scaffoldState: ChartScaffoldState,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(item?.name ?: "") },
            navigationIcon = {
                IconButton(onClick = {
                    if (isOptions) scaffoldState.onShowOptions(false) else scaffoldState.onSelect(null)
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
            },
            actions = {
                if (!isOptions && item?.hasOptions == true) {
                    IconButton(onClick = { scaffoldState.onShowOptions(true) }) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                }
            },
        )
        HorizontalDivider()
        if (item != null && state != null) {
            if (isOptions) item.Options(state) else item.Content(state)
        }
    }
}


@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Immutable
private data class ChartScaffoldState(
    val scaffoldValue: ThreePaneScaffoldValue,
    val directive: PaneScaffoldDirective,
    val onSelect: (ChartView<*>?) -> Unit,
    val onShowOptions: (Boolean) -> Unit,
)


@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private fun computeThreePaneScaffoldValue(
    directive: PaneScaffoldDirective,
    destination: ThreePaneScaffoldDestinationItem<*>,
    item: ChartView<*>?,
    showOptions: Boolean,
): ThreePaneScaffoldValue {
    val isList = destination.pane == ListDetailPaneScaffoldRole.List
    val isExtra = showOptions && item?.hasOptions == true

    return when (directive.maxHorizontalPartitions) {
        1 -> when {
            isList -> ThreePaneScaffoldValue(PaneAdaptedValue.Hidden, PaneAdaptedValue.Expanded, PaneAdaptedValue.Hidden)
            isExtra -> ThreePaneScaffoldValue(PaneAdaptedValue.Hidden, PaneAdaptedValue.Hidden, PaneAdaptedValue.Expanded)
            else -> ThreePaneScaffoldValue(PaneAdaptedValue.Expanded, PaneAdaptedValue.Hidden, PaneAdaptedValue.Hidden)
        }

        2 -> ThreePaneScaffoldValue(
            secondary = if (isList) PaneAdaptedValue.Expanded else PaneAdaptedValue.Hidden,
            primary = if (!isList && !isExtra) PaneAdaptedValue.Expanded else PaneAdaptedValue.Hidden,
            tertiary = if (isExtra) PaneAdaptedValue.Expanded else PaneAdaptedValue.Hidden,
        )

        else -> ThreePaneScaffoldValue(
            secondary = PaneAdaptedValue.Expanded,
            primary = PaneAdaptedValue.Expanded,
            tertiary = if (isExtra) PaneAdaptedValue.Expanded else PaneAdaptedValue.Hidden,
        )
    }
}