package com.grup.ui.compose.views.tabs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import cafe.adriel.voyager.transitions.SlideTransition
import com.grup.ui.compose.views.GroupDetailsView
import com.grup.ui.compose.views.GroupsView
import com.grup.ui.viewmodel.GroupsViewModel

internal class GroupsTab(
    private val groupId: String? = null,
    private val actionId: String? = null
) : Tab {
    override val key: ScreenKey = uniqueScreenKey

    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.Home)
            return remember {
                TabOptions(
                    index = 1u,
                    title = "Groups",
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        val groupsViewModel: GroupsViewModel = getScreenModel()
        Navigator(
            mutableListOf<Screen>(GroupsView()).apply {
                if (groupId != null) {
                    groupsViewModel.selectGroup(groupId) {
                        add(GroupDetailsView(actionId))
                    }
                }
            }
        ) { navigator ->
            SlideTransition(navigator = navigator) { screen ->
                screen.Content()
            }
        }
    }
}