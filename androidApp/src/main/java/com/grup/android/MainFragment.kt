package com.grup.android

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.grup.android.login.LoginActivity
import com.grup.android.transaction.TransactionActivity
import com.grup.android.transaction.TransactionViewModel
import com.grup.android.ui.*
import com.grup.android.ui.apptheme.*
import com.grup.models.*
import kotlinx.coroutines.launch

class MainFragment : Fragment() {
    private val mainViewModel: MainViewModel by navGraphViewModels(R.id.main_graph)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {}
            }
        )
        return ComposeView(requireContext()).apply {
            setContent {
                CompositionLocalProvider(
                    LocalContentColor provides AppTheme.colors.onSecondary
                ) {
                    MainLayout(
                        mainViewModel = mainViewModel,
                        navController = findNavController()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLifecycleComposeApi::class, ExperimentalMaterialApi::class)
@Composable
fun MainLayout(
    mainViewModel: MainViewModel,
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()
    val actionDetailsBottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    val lazyListState = rememberLazyListState()

    val groups: List<Group> by mainViewModel.groups.collectAsStateWithLifecycle()
    val selectedGroup: Group? by mainViewModel.selectedGroup.collectAsStateWithLifecycle()
    val myUserInfo: UserInfo? by mainViewModel.myUserInfo.collectAsStateWithLifecycle()
    val groupActivity: List<TransactionActivity> by
            mainViewModel.groupActivity.collectAsStateWithLifecycle()
    val activeSettleActions: List<SettleAction> by
            mainViewModel.activeSettleActions.collectAsStateWithLifecycle()

    var selectedAction: Action? by remember { mutableStateOf(null) }

    val openDrawer: () -> Unit = { scope.launch { scaffoldState.drawerState.open() } }
    val closeDrawer: () -> Unit = { scope.launch { scaffoldState.drawerState.close() } }
    val selectAction: (Action) -> Unit = { action ->
        selectedAction = action
        scope.launch { actionDetailsBottomSheetState.show() }
    }
    val modalSheets: @Composable (@Composable () -> Unit) -> Unit = { content ->
        selectedAction?.let { selectedAction ->
            BackPressModalBottomSheetLayout(
                sheetState = actionDetailsBottomSheetState,
                sheetContent = {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = {},
                                navigationIcon = {
                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                actionDetailsBottomSheetState.hide()
                                            }
                                        }
                                    ) {
                                        SmallIcon(
                                            imageVector = Icons.Filled.ArrowBack,
                                            contentDescription = "Back"
                                        )
                                    }
                                },
                                backgroundColor = AppTheme.colors.primary
                            )
                        },
                        backgroundColor = AppTheme.colors.primary,
                        modifier = Modifier
                            .fillMaxSize()
                    ) { padding ->
                        when (selectedAction) {
                            is DebtAction -> DebtActionDetails(
                                debtAction = selectedAction,
                                myUserInfo = myUserInfo!!,
                                modifier = Modifier
                                    .padding(padding)
                                    .padding(AppTheme.dimensions.appPadding)
                            )
                            is SettleAction -> SettleActionDetails(
                                settleAction = selectedAction,
                                myUserInfo = myUserInfo!!,
                                navigateSettleActionTransactionOnClick = {
                                    if (myUserInfo!!.userBalance < 0) {
                                        navController.navigate(
                                            R.id.actionAmountFragment,
                                            Bundle().apply {
                                                this.putString(
                                                    "actionType",
                                                    TransactionViewModel.SETTLE_TRANSACTION
                                                )
                                                this.putString("actionId", selectedAction.getId())
                                            }
                                        )
                                    }
                                },
                                acceptSettleActionTransactionOnClick = { transactionRecord ->
                                    mainViewModel.acceptSettleActionTransaction(
                                        selectedAction,
                                        transactionRecord
                                    )
                                    scope.launch { actionDetailsBottomSheetState.hide() }
                                },
                                modifier = Modifier
                                    .padding(padding)
                                    .padding(AppTheme.dimensions.appPadding)
                            )
                        }
                    }
                },
                content = content
            )
        } ?: content()
    }

    modalSheets {
        Scaffold(
            scaffoldState = scaffoldState,
            topBar = {
                TopBar(
                    group = selectedGroup,
                    onNavigationIconClick = openDrawer,
                    navigateGroupMembersOnClick = { navController.navigate(R.id.viewMembers) }
                )
            },
            drawerGesturesEnabled = scaffoldState.drawerState.isOpen,
            drawerBackgroundColor = AppTheme.colors.secondary,
            drawerContent = {
                //delete later
                GroupNavigationMenu(
                    groups = groups,
                    onGroupClick = { index ->
                        mainViewModel.onSelectedGroupChange(groups[index])
                        closeDrawer()
                    },
                    isSelectedGroup = { it.getId() == selectedGroup?.getId() },
                    navigateNotificationsOnClick = {
                        closeDrawer()
                        navController.navigate(R.id.openNotifications)
                    },
                    navigateCreateGroupOnClick = {
                        closeDrawer()
                        navController.navigate(R.id.createGroup)
                    },
                    logOutOnClick = { mainViewModel.logOut() }
                )
            },
            backgroundColor = AppTheme.colors.primary,
            modifier = Modifier
                .fillMaxSize()
        ) { padding ->
            LazyColumn(
                state = lazyListState,
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(
                    top = AppTheme.dimensions.appPadding,
                    start = AppTheme.dimensions.appPadding,
                    end = AppTheme.dimensions.appPadding
                ),
                modifier = Modifier
                    .padding(padding)
            ) {
                if (groups.isNotEmpty()) {
                    myUserInfo?.let { myUserInfo ->
                        item {
                            GroupBalanceCard(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                myUserInfo = myUserInfo,
                                navigateDebtActionAmountOnClick = {
                                    navController.navigate(
                                        R.id.enterActionAmount,
                                        Bundle().apply {
                                            this.putString("actionType", TransactionViewModel.DEBT)
                                        }
                                    )
                                },
                                navigateSettleActionAmountOnClick = {
                                    navController.navigate(
                                        R.id.enterActionAmount,
                                        Bundle().apply {
                                            this.putString(
                                                "actionType",
                                                TransactionViewModel.SETTLE
                                            )
                                        }
                                    )
                                }
                            )
                            Spacer(modifier = Modifier.height(AppTheme.dimensions.spacingLarge))
                        }
                        item {
                            ActiveSettleActions(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                mySettleActions = activeSettleActions.filter { settleAction ->
                                    settleAction.debteeUserInfo!!.userId == myUserInfo.userId!!
                                },
                                activeSettleActions = activeSettleActions.filter { settleAction ->
                                    settleAction.debteeUserInfo!!.userId != myUserInfo.userId!!
                                },
                                settleActionCardOnClick = selectAction
                            )
                            Spacer(modifier = Modifier.height(AppTheme.dimensions.spacingLarge))
                        }
                        recentActivityList(
                            groupActivity = groupActivity,
                            transactionActivityOnClick = { transactionActivity ->
                                selectAction(transactionActivity.action)
                            }
                        )
                    }
                } else {
                    item { NoGroupsDisplay() }
                }
            }
        }
    }
}

@Composable
fun NoGroupsDisplay() {
    Text(text = "Yaint in any groups bozo", color = AppTheme.colors.onSecondary)
}

@Composable
fun GroupNavigationMenu(
    groups: List<Group>,
    onGroupClick: (Int) -> Unit,
    isSelectedGroup: (Group) -> Boolean,
    navigateNotificationsOnClick: () -> Unit,
    navigateCreateGroupOnClick: () -> Unit,
    logOutOnClick: () -> Unit,
    background: Color = AppTheme.colors.primary
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
    ) {
        DrawerHeader(navigateNotificationsOnClick = navigateNotificationsOnClick)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(AppTheme.dimensions.spacing)) {
            itemsIndexed(groups) { index, group ->
                GroupNavigationRow(
                    group = group,
                    onGroupClick = { onGroupClick(index) },
                    isSelected = isSelectedGroup(group)
                )
            }
        }
        Column(
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier.fillMaxSize()
        ) {
            Divider(color = AppTheme.colors.primary, thickness = 3.dp)
            DrawerSettings(
                items = listOf(
                    MenuItem(
                        id = "new_group",
                        title = "Create New Group",
                        contentDescription = "Create a new group",
                        icon = Icons.Default.AddCircle,
                        onClick = { navigateCreateGroupOnClick() }
                    ),
                    MenuItem(
                        id = "settings",
                        title = "Settings",
                        contentDescription = "Open settings screen",
                        icon = Icons.Default.Settings,
                        onClick = { }
                    ),
                    MenuItem(
                        id = "logout",
                        title = "Sign Out",
                        contentDescription = "Log out",
                        icon = Icons.Default.ExitToApp,
                        onClick = {
                            logOutOnClick()
                            context.startActivity(
                                Intent(context, LoginActivity::class.java)
                                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            )
                        }
                    ),
                )
            )
        }
    }
}

@Composable
fun GroupNavigationRow(
    group: Group,
    onGroupClick: () -> Unit,
    isSelected: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppTheme.dimensions.appPadding)
            .clip(AppTheme.shapes.medium)
            .clickable { onGroupClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = AppTheme.dimensions.paddingSmall)
        ) {
            Box(
                modifier =
                    if (isSelected)
                        Modifier.border(
                            width = 2.dp,
                            color = Color.White
                        )
                    else
                        Modifier
            ) {
                SmallIcon(
                    imageVector = Icons.Default.Home,
                    contentDescription = group.groupName!!
                )
            }
            Spacer(modifier = Modifier.width(20.dp))
            H1Text(
                text = group.groupName!!,
                color = AppTheme.colors.onSecondary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun TopBar(
    group: Group?,
    onNavigationIconClick: () -> Unit,
    navigateGroupMembersOnClick: () -> Unit
) {
    TopAppBar(
        title = { group?.let { H1Text(text = it.groupName!!) } },
        backgroundColor = AppTheme.colors.primary,
        navigationIcon = {
            BadgedBox(
                badge = {
                    Badge(
                        backgroundColor = AppTheme.colors.error,
                        modifier = Modifier
                            .offset((-8).dp, (10).dp)
                    ) {
                        H1Text(
                            text = "1",
                            fontSize = 14.sp
                        )
                    }
                }
            ) {
                IconButton(onClick = onNavigationIconClick) {
                    SmallIcon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "Menu"
                    )
                }
            }
        },
        actions = {
            if (group != null) {
                MembersButton(navigateGroupMembersOnClick = navigateGroupMembersOnClick)
            }
        }
    )
}

@Composable
fun NotificationsButton(
    navigateNotificationsOnClick: () -> Unit
) {
    IconButton(onClick = navigateNotificationsOnClick) {
        SmallIcon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "Notifications"
        )
    }
}

@Composable
fun MembersButton(
    navigateGroupMembersOnClick: () -> Unit
) {
    IconButton(
        onClick = navigateGroupMembersOnClick
    ) {
        SmallIcon(
            imageVector = Icons.Default.Person,
            contentDescription = "Members"
        )
    }
}

@Composable
fun GroupBalanceCard(
    modifier: Modifier = Modifier,
    myUserInfo: UserInfo,
    navigateDebtActionAmountOnClick: () -> Unit,
    navigateSettleActionAmountOnClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(AppTheme.shapes.extraLarge)
            .background(AppTheme.colors.secondary)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimensions.spacingSmall),
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.dimensions.cardPadding)
        ) {
            H1Text(text = "Your Balance")
            MoneyAmount(moneyAmount = myUserInfo.userBalance)
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = AppTheme.dimensions.spacing)
            ) {
                TextButton(
                    colors = ButtonDefaults.buttonColors(backgroundColor = AppTheme.colors.confirm),
                    modifier = Modifier
                        .width(140.dp)
                        .height(45.dp),
                    shape = AppTheme.shapes.CircleShape,
                    onClick = navigateDebtActionAmountOnClick
                ) {
                    H1Text(
                        text = "Request",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = AppTheme.colors.onSecondary,
                    )
                }
                TextButton(
                    colors = ButtonDefaults.buttonColors(backgroundColor = AppTheme.colors.confirm),
                    modifier = Modifier
                        .width(140.dp)
                        .height(45.dp),
                    shape = AppTheme.shapes.CircleShape,
                    onClick = navigateSettleActionAmountOnClick
                ) {
                    H1Text(
                        text = "Settle",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = AppTheme.colors.onSecondary,
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveSettleActions(
    modifier: Modifier = Modifier,
    mySettleActions: List<SettleAction>,
    activeSettleActions: List<SettleAction>,
    settleActionCardOnClick: (SettleAction) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimensions.spacing),
        modifier = modifier
    ) {
        H1Text(text = "Active Settle", fontWeight = FontWeight.Medium)
        if (mySettleActions.isEmpty() && activeSettleActions.isEmpty()) {
            // TODO: No requests display
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppTheme.dimensions.appPadding)
            ) {
                if (mySettleActions.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(AppTheme.dimensions.spacing),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Caption(text = "My Settle")
                        Row(
                            horizontalArrangement = Arrangement
                                .spacedBy(AppTheme.dimensions.appPadding),
                            modifier = Modifier
                                .width(IntrinsicSize.Min)
                        ) {
                            mySettleActions.forEach { settleAction ->
                                SettleActionCard(
                                    settleAction = settleAction,
                                    settleActionCardOnClick = {
                                        settleActionCardOnClick(settleAction)
                                    },
                                    showPendingNotification = true
                                )
                            }
                        }
                    }
                }
                if (activeSettleActions.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(AppTheme.dimensions.spacing),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Caption(text = "Other Settle")
                        Row(
                            horizontalArrangement = Arrangement
                                .spacedBy(AppTheme.dimensions.appPadding),
                            modifier = Modifier
                                .width(IntrinsicSize.Min)
                        ) {
                            activeSettleActions.forEach { settleAction ->
                                SettleActionCard(
                                    settleAction = settleAction,
                                    settleActionCardOnClick = {
                                        settleActionCardOnClick(settleAction)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettleActionCard(
    settleAction: SettleAction,
    settleActionCardOnClick: () -> Unit,
    showPendingNotification: Boolean = false
) {
    BadgedBox(
        badge = {
            settleAction.transactionRecords.count {
                it.dateAccepted == TransactionRecord.PENDING
            }.let { notificationCount ->
                if (showPendingNotification && notificationCount > 0) {
                    Badge(
                        backgroundColor = AppTheme.colors.error,
                        modifier = Modifier
                            .offset((-18).dp, (18.dp))
                    ) {
                        H1Text(
                            text = notificationCount.toString(),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    ) {
        Box(
            modifier = Modifier
                .clip(AppTheme.shapes.large)
                .width(140.dp)
                .height(140.dp)
                .background(AppTheme.colors.secondary)
                .clickable(onClick = settleActionCardOnClick)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppTheme.dimensions.spacingLarge),
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppTheme.dimensions.cardPadding)
            ) {
                ProfileIcon(
                    imageVector = Icons.Default.Face,
                    iconSize = 50.dp
                )
                MoneyAmount(
                    moneyAmount = settleAction.remainingAmount,
                    fontSize = 24.sp
                )
            }
        }
    }
}

@Composable
fun DebtActionDetails(
    modifier: Modifier = Modifier,
    debtAction: DebtAction,
    myUserInfo: UserInfo
) {
    val isMyAction: Boolean = myUserInfo.userId!! == debtAction.debteeUserInfo!!.userId!!

    Column(
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimensions.spacing),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        UserInfoRowCard(
            userInfo = debtAction.debteeUserInfo!!,
            mainContent = {
                Column(horizontalAlignment = Alignment.Start) {
                    Caption(text = debtAction.debteeUserInfo!!.nickname!!)
                    MoneyAmount(
                        moneyAmount =
                            if (isMyAction) debtAction.totalAmount
                            else debtAction.acceptedAmount,
                        fontSize = 60.sp
                    )
                }
            },
            sideContent = {
                Column(horizontalAlignment = Alignment.End) {
                    Caption(text = "Request")
                    Caption(text = isoDate(debtAction.date))
                }
            },
            iconSize = 90.dp
        )

        if (debtAction.message.isNotBlank()) {
            H1Text(
                text = "\"" + debtAction.message + "\"",
                modifier = Modifier.padding(vertical = AppTheme.dimensions.spacingMedium)
            )
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AppTheme.dimensions.paddingMedium)
        ) {
            H1Text(text = "Requests")
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(AppTheme.shapes.extraLarge)
                .background(AppTheme.colors.secondary)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppTheme.dimensions.spacing),
                modifier = Modifier.padding(AppTheme.dimensions.cardPadding)
            ) {
                debtAction.transactionRecords.filter { transactionRecord ->
                    transactionRecord.dateAccepted != TransactionRecord.PENDING
                }.forEach { acceptedTransaction ->
                    TransactionRowCard(transactionRecord = acceptedTransaction)
                }
            }
        }

        if (isMyAction) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = AppTheme.dimensions.paddingMedium)
            ) {
                H1Text(text = "Pending")
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(AppTheme.shapes.extraLarge)
                    .background(AppTheme.colors.secondary)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(AppTheme.dimensions.spacing),
                    modifier = Modifier.padding(AppTheme.dimensions.cardPadding)
                ) {
                    debtAction.transactionRecords.filter { transactionRecord ->
                        transactionRecord.dateAccepted == TransactionRecord.PENDING
                    }.forEach { pendingTransaction ->
                        TransactionRowCard(transactionRecord = pendingTransaction)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SettleActionDetails(
    modifier: Modifier = Modifier,
    settleAction: SettleAction,
    myUserInfo: UserInfo,
    navigateSettleActionTransactionOnClick: () -> Unit,
    acceptSettleActionTransactionOnClick: (TransactionRecord) -> Unit
) {
    val scope = rememberCoroutineScope()
    val acceptPendingTransactionBottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    
    val tabTitles: MutableList<String> = mutableListOf("Accepted")
    var selectedTabIndex: Int by remember { mutableStateOf(0) }
    var selectedTransaction: TransactionRecord? by remember { mutableStateOf(null) }

    val isMyAction: Boolean = myUserInfo.userId!! == settleAction.debteeUserInfo!!.userId!!
    if (
        isMyAction &&
        settleAction.remainingAmount > 0 &&
        settleAction.transactionRecords.any {
            it.dateAccepted == TransactionRecord.PENDING
        }
    ) {
        tabTitles.add("Pending")
    }
    selectedTabIndex = 0

    val modalSheets: @Composable (@Composable () -> Unit) -> Unit = { content ->
        selectedTransaction?.let { selectedTransaction ->
            BackPressModalBottomSheetLayout(
                sheetState = acceptPendingTransactionBottomSheetState,
                sheetContent = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement
                            .spacedBy(AppTheme.dimensions.spacingMedium),
                        modifier = modifier.fillMaxWidth()
                    ) {
                        H1Text(
                            text = "${selectedTransaction.debtorUserInfo!!.nickname!!} is " +
                                    "paying ${selectedTransaction.balanceChange!!.asMoneyAmount()}"
                        )
                        Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                            H1ConfirmTextButton(
                                text = "Accept",
                                scale = 0.8f,
                                onClick = {
                                    acceptSettleActionTransactionOnClick(selectedTransaction)
                                    scope.launch { acceptPendingTransactionBottomSheetState.hide() }
                                }
                            )
                        }
                    }
                },
                content = content
            )
        } ?: content()
    }
    
    modalSheets {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimensions.spacing),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.fillMaxSize()
        ) {
            UserInfoRowCard(
                userInfo = settleAction.debteeUserInfo!!,
                mainContent = {
                    Column(horizontalAlignment = Alignment.Start) {
                        Caption(text = settleAction.debteeUserInfo!!.nickname!!)
                        MoneyAmount(
                            moneyAmount = settleAction.remainingAmount,
                            fontSize = 60.sp
                        )
                    }
                },
                sideContent = {
                    Column(horizontalAlignment = Alignment.End) {
                        Caption(text = "Settle")
                        Caption(text = isoDate(settleAction.date))
                    }
                },
                iconSize = 90.dp
            )

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = AppTheme.dimensions.paddingMedium)
            ) {
                tabTitles.forEachIndexed { index, tabTitle ->
                    if (tabTitle == tabTitles[selectedTabIndex]) {
                        H1Text(
                            text = tabTitle,
                            modifier = Modifier.clickable { selectedTabIndex = index }
                        )
                    } else {
                        Caption(
                            text = tabTitle,
                            modifier = Modifier.clickable { selectedTabIndex = index }
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(AppTheme.shapes.extraLarge)
                    .background(AppTheme.colors.secondary)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(AppTheme.dimensions.spacing),
                    modifier = Modifier.padding(AppTheme.dimensions.cardPadding)
                ) {
                    when (selectedTabIndex) {
                        0 -> {
                            settleAction.transactionRecords.filter { transactionRecord ->
                                transactionRecord.dateAccepted != TransactionRecord.PENDING
                            }.let { acceptedTransactions ->
                                if (acceptedTransactions.isNotEmpty()) {
                                    acceptedTransactions.forEach { acceptedTransaction ->
                                        TransactionRowCard(transactionRecord = acceptedTransaction)
                                    }
                                }
                            }
                        }
                        1 -> {
                            settleAction.transactionRecords.filter { transactionRecord ->
                                transactionRecord.dateAccepted == TransactionRecord.PENDING
                            }.let { pendingTransactions ->
                                if (pendingTransactions.isNotEmpty()) {
                                    pendingTransactions.forEach { pendingTransaction ->
                                        TransactionRowCard(
                                            transactionRecord = pendingTransaction,
                                            modifier = Modifier.clickable {
                                                selectedTransaction = pendingTransaction
                                                scope.launch {
                                                    acceptPendingTransactionBottomSheetState.show()
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            if (
                !isMyAction &&
                myUserInfo.userBalance < 0 &&
                settleAction.remainingAmount > 0
            ) {
                H1ConfirmTextButton(
                    text = "Settle",
                    onClick = navigateSettleActionTransactionOnClick,
                    modifier = Modifier
                        .padding(top = AppTheme.dimensions.appPadding)
                )
            }
        }
    }
}

@Composable
fun TransactionRowCard(
    modifier: Modifier = Modifier,
    transactionRecord: TransactionRecord
) {
    UserInfoRowCard(
        userInfo = transactionRecord.debtorUserInfo!!,
        iconSize = 62.dp,
        mainContent = {
            Column(
                verticalArrangement = Arrangement
                    .spacedBy(AppTheme.dimensions.spacingSmall)
            ) {
                H1Text(
                    text = transactionRecord
                        .debtorUserInfo!!.nickname!!
                )
                Caption(
                    text = isoDate(
                        if (transactionRecord.dateAccepted != TransactionRecord.PENDING)
                            transactionRecord.dateAccepted
                        else
                            transactionRecord.dateCreated
                    )
                )
            }
        },
        sideContent = {
            MoneyAmount(
                moneyAmount = transactionRecord
                    .balanceChange!!,
                fontSize = 24.sp
            )
        },
        modifier = modifier
    )
}
