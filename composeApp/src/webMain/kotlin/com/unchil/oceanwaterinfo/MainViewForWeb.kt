package com.unchil.oceanwaterinfo

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
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldDestinationItem
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.rememberPaneExpansionState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.koalaplot.core.style.KoalaPlotTheme
import io.github.vooft.compose.treeview.core.TreeView
import io.github.vooft.compose.treeview.core.node.Node
import io.github.vooft.compose.treeview.core.tree.Tree


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun MainViewForWeb(modifier: Modifier = Modifier) {

    MaterialTheme(
        typography = getTypography(),
        colorScheme = getColorScheme(false)
    ){

        KoalaPlotTheme {
            val samplesTree = envObservationTreeForWeb()
            var selectedItem by remember { mutableStateOf<ChartView<*>?>(EmptyContent) }
            var showOptions by remember { mutableStateOf(false) }

            val directive = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo(true)).copy(
                horizontalPartitionSpacerSize = 0.dp,
            )
            val destination = remember(selectedItem, showOptions, directive.maxHorizontalPartitions) {
                if (selectedItem?.equals(EmptyContent) == true) {
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
                onSelect = {
                    selectedItem = it
                },
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
                IconButton(
                    onClick = {
                        setAllElementVisibilityByHidden()

                        if (isOptions)
                            scaffoldState.onShowOptions(false)
                        else
                            scaffoldState.onSelect(EmptyContent)

                    }
                ) {
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
// 2. 이전에 클릭한 노드를 기억하는 상태 추가
    var previousNode by remember { mutableStateOf<Any?>(null) }


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
                            previousNode?.let {
                                samplesTree.unselectNode(it as Node<ChartViewContent>)
                            }
                            samplesTree.selectNode(node)

                            if (previousNode != node) {
                                previousNode = node
                                setAllElementVisibilityByHidden()
                                val content = node.content
                                if (content is ChartView<*>) {
                                    scaffoldState.onSelect(content)
                                    scaffoldState.onShowOptions(false)
                                } else {
                                    samplesTree.toggleExpansion(node)
                                }
                            }
                        },
                        onDoubleClick = {node -> },
                        onLongClick = {node -> },
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


