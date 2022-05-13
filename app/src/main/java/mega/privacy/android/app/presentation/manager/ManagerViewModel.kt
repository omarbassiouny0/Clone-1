package mega.privacy.android.app.presentation.manager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import mega.privacy.android.app.data.model.GlobalUpdate
import mega.privacy.android.app.data.repository.DefaultGlobalUpdatesRepository
import mega.privacy.android.app.di.MegaApi
import mega.privacy.android.app.domain.repository.GlobalUpdatesRepository
import mega.privacy.android.app.domain.usecase.MonitorUserUpdates
import nz.mega.sdk.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject

/**
 * ViewModel associated to {@link mega.privacy.android.app.main.ManagerActivity}
 *
 * @param globalUpdatesRepository
 */
@HiltViewModel
class ManagerViewModel @Inject constructor(
    globalUpdatesRepository: GlobalUpdatesRepository
) : ViewModel() {

    val updateUsers: LiveData<List<MegaUser>> =
        globalUpdatesRepository.monitorGlobalUpdates()
            .filterIsInstance<GlobalUpdate.OnUsersUpdate>()
            .also { Timber.d("onUsersUpdate") }
            .map { it.users?.toList() }
            .filterNotNull()
            .filterNot { it.isEmpty() }
            .asLiveData()

    val updateUserAlerts: LiveData<List<MegaUserAlert>> =
        globalUpdatesRepository.monitorGlobalUpdates()
            .filterIsInstance<GlobalUpdate.OnUserAlertsUpdate>()
            .also { Timber.d("onUserAlertsUpdate") }
            .map { it.userAlerts?.toList() }
            .filterNotNull()
            .filterNot { it.isEmpty() }
            .asLiveData()

    val updateNodes: LiveData<List<MegaNode>> =
        globalUpdatesRepository.monitorGlobalUpdates()
            .filterIsInstance<GlobalUpdate.OnNodesUpdate>()
            .also { Timber.d("onNodesUpdate") }
            .map { it.nodeList?.toList() }
            .filterNotNull()
            .filterNot { it.isEmpty() }
            .asLiveData()

    val updateContactsRequests: LiveData<List<MegaContactRequest>> =
        globalUpdatesRepository.monitorGlobalUpdates()
            .filterIsInstance<GlobalUpdate.OnContactRequestsUpdate>()
            .also { Timber.d("onContactRequestsUpdate") }
            .map { it.requests?.toList() }
            .filterNotNull()
            .filterNot { it.isEmpty() }
            .asLiveData()

}