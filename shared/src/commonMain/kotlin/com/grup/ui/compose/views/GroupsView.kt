package com.grup.ui.compose.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.grup.models.Group
import com.grup.models.UserInfo
import com.grup.ui.apptheme.AppTheme
import com.grup.ui.compose.Caption
import com.grup.ui.compose.GroupRowCard
import com.grup.ui.compose.H1Text
import com.grup.ui.compose.MoneyAmount
import com.grup.ui.compose.SmallIcon
import com.grup.ui.compose.collectAsStateWithLifecycle
import com.grup.ui.viewmodel.GroupsViewModel

internal class GroupsView: Screen {
    override val key: ScreenKey = uniqueScreenKey

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val groupsViewModel = getScreenModel<GroupsViewModel>()

        CompositionLocalProvider(
            LocalContentColor provides AppTheme.colors.onSecondary
        ) {
            GroupsLayout(groupsViewModel = groupsViewModel, navigator = navigator)
        }
    }
}

@Composable
private fun GroupsLayout(
    groupsViewModel: GroupsViewModel,
    navigator: Navigator
) {
    val groups: List<Group> by groupsViewModel.groups.collectAsStateWithLifecycle()
    val myUserInfos: List<UserInfo> by groupsViewModel.myUserInfosFlow.collectAsStateWithLifecycle()

    val userInfos: List<UserInfo> by groupsViewModel.userInfosFlow.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                actions = {
                    IconButton(
                        onClick = {
                            navigator.push(CreateGroupView())
                        }
                    ) {
                        SmallIcon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Create Group"
                        )
                    }
                },
                backgroundColor = AppTheme.colors.primary
            )
        },
        backgroundColor = AppTheme.colors.primary,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimensions.spacingLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(AppTheme.dimensions.appPadding),
            modifier = Modifier
                .fillMaxSize()
        ) {
            item {
                Caption(
                    text = "Overall Balance",
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                myUserInfos.sumOf { it.userBalance }.let { overallBalance ->
                    MoneyAmount(
                        moneyAmount = overallBalance,
                        color = if (overallBalance >= 0) AppTheme.colors.confirm
                                else AppTheme.colors.deny,
                        fontSize = 50.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth(0.9f)
                    )
                }
            }
            item {
                Divider(
                    color = AppTheme.colors.secondary,
                    thickness = Dp.Hairline
                )
            }
            item {
                H1Text(
                    text = "Groups",
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
            items(groups) { group ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(AppTheme.dimensions.bigItemRowCardHeight)
                        .fillMaxWidth()
                        .clip(AppTheme.shapes.large)
                        .background(AppTheme.colors.secondary)
                        .clickable {
                            groupsViewModel.selectGroup(group)
                            navigator.push(GroupDetailsView())
                        }
                        .padding(AppTheme.dimensions.rowCardPadding)
                ) {
                    GroupRowCard(
                        group = group,
                        userBalance = myUserInfos.find { it.group.id == group.id }!!.userBalance,
                        membersCount = userInfos.count { it.group.id == group.id }
                    )
                }
            }
        }
    }
}