package test.mega.privacy.android.app.smsVerification

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import mega.privacy.android.app.presentation.verification.SMSVerificationTextViewModel
import mega.privacy.android.app.presentation.verification.model.SmsVerificationTextState
import mega.privacy.android.app.presentation.verification.model.mapper.SmsVerificationTextErrorMapper
import mega.privacy.android.domain.usecase.verification.VerifyPhoneNumber
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub

@OptIn(ExperimentalCoroutinesApi::class)
internal class SMSVerificationTextViewModelTest {
    private lateinit var underTest: SMSVerificationTextViewModel

    private val verifyPhoneNumber = mock<VerifyPhoneNumber>()
    private val verificationTextErrorMapper = mock<SmsVerificationTextErrorMapper>()

    @Before
    fun setUp() {
        underTest = SMSVerificationTextViewModel(
            verifyPhoneNumber = verifyPhoneNumber,
            verificationTextErrorMapper = verificationTextErrorMapper,
        )
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test that initial state is Empty`() = runTest {
        underTest.state.test {
            assertThat(awaitItem()).isEqualTo(SmsVerificationTextState.Empty)
        }
    }

    @Test
    fun `test that loading state is returned while awaiting response`() = runTest {
        underTest.state
            .filterNot { it is SmsVerificationTextState.Empty }
            .test {
                underTest.submitPin("234")
                assertThat(awaitItem()).isEqualTo(SmsVerificationTextState.Loading)
                cancelAndIgnoreRemainingEvents()
            }
    }

    @Test
    fun `test that successfully verifying sets the state to VerifiedSuccessfully`() = runTest {
        underTest.state
            .filterNot { it is SmsVerificationTextState.Empty }
            .filterNot { it is SmsVerificationTextState.Loading }
            .test {
                underTest.submitPin("234")
                assertThat(awaitItem()).isEqualTo(SmsVerificationTextState.VerifiedSuccessfully)
            }
    }

    @Test
    fun `test that failure during verification returns error state`() = runTest {
        val error = "Expected error string"
        verificationTextErrorMapper.stub {
            on { invoke(any()) }.thenReturn(error)
        }

        verifyPhoneNumber.stub {
            onBlocking { invoke(any()) }.thenAnswer { throw Throwable() }
        }

        underTest.state
            .filterNot { it is SmsVerificationTextState.Empty }
            .filterNot { it is SmsVerificationTextState.Loading }
            .test {
                underTest.submitPin("234")
                assertThat(awaitItem()).isEqualTo(SmsVerificationTextState.Failed(error))
            }
    }
}