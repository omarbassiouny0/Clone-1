package mega.privacy.android.domain.usecase.transfers.paused

import com.google.common.truth.Truth.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import mega.privacy.android.domain.entity.transfer.Transfer
import mega.privacy.android.domain.entity.transfer.TransferType
import mega.privacy.android.domain.repository.TransferRepository
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MonitorChatUploadTransfersPausedUseCaseTest {
    private lateinit var underTest: MonitorChatUploadTransfersPausedUseCase

    private val transferRepository = mock<TransferRepository>()

    @BeforeAll
    fun setup() {
        underTest = MonitorChatUploadTransfersPausedUseCase(transferRepository)
    }

    @BeforeEach
    fun resetMocks() {
        reset(transferRepository)
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 5, 99])
    fun `test that totalPendingIndividualTransfers returns getNumPendingChatUploads from repository`(
        value: Int,
    ) = runTest {
        whenever(transferRepository.getNumPendingChatUploads()).thenReturn(value)
        assertThat(underTest.totalPendingIndividualTransfers()).isEqualTo(value)
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 5, 99])
    fun `test that totalPausedIndividualTransfers returns getNumPendingPausedChatUploads from repository`(
        value: Int,
    ) = runTest {
        whenever(transferRepository.getNumPendingPausedChatUploads()).thenReturn(value)
        assertThat(underTest.totalPausedIndividualTransfers()).isEqualTo(value)
    }

    @ParameterizedTest
    @MethodSource("getTransfers")
    fun `test that transfers are filtered correctly`(transfer: Transfer, isCorrect: Boolean) {
        assertThat(underTest.isCorrectType(transfer)).isEqualTo(isCorrect)
    }

    private fun getTransfers() = TransferType.values().map {
        Arguments.of(mockTransfer(it), it == TransferType.CHAT_UPLOAD)
    }

    private fun mockTransfer(
        type: TransferType
    ) = mock<Transfer> {
        on { it.transferType }.thenReturn(type)
    }

}