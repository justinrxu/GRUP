package com.grup.android

import com.grup.APIServer
import com.grup.android.transaction.TransactionActivity
import com.grup.models.*
import kotlinx.coroutines.flow.*

class MainViewModel : ViewModel() {
    companion object {
        private val selectedGroupMutable:
                MutableStateFlow<Group?> = MutableStateFlow(null)
        val selectedGroup: Group
            get() = selectedGroupMutable.value!!
    }

    // Selected group in the UI. Other UI flows use this to filter data based on the selected group.
    val selectedGroup: StateFlow<Group?> = selectedGroupMutable
    fun onSelectedGroupChange(group: Group) = group.also {
        selectedGroupMutable.value = group
    }

    // Hot flow containing all Groups the user is in. Updates selectedGroup if it's changed/deleted
    private val _groupsFlow = APIServer.getAllGroupsAsFlow()
    val groups: StateFlow<List<Group>> = _groupsFlow.onEach { newGroups ->
        selectedGroup.value?.let { nonNullGroup ->
            selectedGroupMutable.value = newGroups.find { group ->
                group.getId() == nonNullGroup.getId()
            }
        } ?: run {
            selectedGroupMutable.value = newGroups.getOrNull(0)
        }
    }.asState()

    // Hot flow containing User's UserInfos
    private val _myUserInfosFlow by lazy { APIServer.getMyUserInfosAsFlow() }
    val myUserInfo: StateFlow<UserInfo?> by lazy {
        _myUserInfosFlow.map { userInfos ->
            userInfos.find { it.userId == userObject.getId() }
        }.asState()
    }

    // DebtActions belonging to the selectedGroup, mapped to TransactionActivity
    private val _debtActionsFlow = APIServer.getAllDebtActionsAsFlow()
    private val debtActionsAsTransactionActivity: Flow<List<TransactionActivity>> =
        _debtActionsFlow.combine(selectedGroup) { debtActions, selectedGroup ->
            selectedGroup?.let { group ->
                debtActions.filter { debtAction ->
                    debtAction.groupId == group.getId()
                }.flatMap { debtAction ->
                    listOf(
                        TransactionActivity.CreateDebtAction(debtAction),
                        *debtAction.debtTransactions.filter { transactionRecord ->
                            transactionRecord.dateAccepted != TransactionRecord.PENDING
                        }.map { transactionRecord ->
                            TransactionActivity.AcceptDebtAction(debtAction, transactionRecord)
                        }.toTypedArray()
                    )
                }
            } ?: emptyList()
        }

    // Hot flow combining all TransactionActivity flows to be displayed as recent activity in UI
    val groupActivity: StateFlow<List<TransactionActivity>> =
        combine(
            debtActionsAsTransactionActivity
        ) { allTransactionActivities: Array<List<TransactionActivity>> ->
            allTransactionActivities.flatMap { it }.sortedBy { transactionActivity ->
                transactionActivity.date
            }
        }.asState()


    // Group operations
    fun createGroup(groupName: String) = APIServer.createGroup(groupName)

    fun logOut() = APIServer.logOut()
}