package com.grup.android.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.navGraphViewModels
import com.grup.android.*
import com.grup.android.R
import com.grup.ui.compose.GroupInvitesView
import com.grup.ui.viewmodel.GroupInvitesViewModel

class GroupInvitesFragment : Fragment() {
    private val groupInvitesViewModel: GroupInvitesViewModel by navGraphViewModels(R.id.main_graph)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                GroupInvitesView(
                    groupInvitesViewModel = groupInvitesViewModel,
                    navController = AndroidNavigationController(findNavController())
                )
            }
        }
    }
}
