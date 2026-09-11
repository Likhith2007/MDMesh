package com.mdmesh.policy.security

import android.os.Build
import com.mdmesh.policy.DpmHandle
import com.mdmesh.policy.PolicyOutcome
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify

/**
 * Unit tests for [UsbDebugRestrictionPolicy]:
 * - setEnabled(false) → addUserRestriction(DISALLOW_DEBUGGING_FEATURES)
 * - setEnabled(true) → clearUserRestriction(DISALLOW_DEBUGGING_FEATURES)
 * - API guards: SDK 18+ only
 */
@RunWith(MockitoJUnitRunner::class)
class UsbDebugPolicyTest {

    @Mock
    private lateinit var mockDpm: com.mdmesh.policy.FakeDpmHandle

    @Test
    fun `disables USB debug via DISALLOW_DEBUGGING_FEATURES restriction`() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return // Skip on older APIs
        }

        val policy = UsbDebugRestrictionPolicy(mockDpm)
        val result = policy.setEnabled(false)

        assertEquals(PolicyOutcome.Applied, result)
        verify(mockDpm.dpm).addUserRestriction(
            mockDpm.admin,
            "no_debugging_features"
        )
    }

    @Test
    fun `enables USB debug by clearing DISALLOW_DEBUGGING_FEATURES restriction`() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return // Skip on older APIs
        }

        val policy = UsbDebugRestrictionPolicy(mockDpm)
        val result = policy.setEnabled(true)

        assertEquals(PolicyOutcome.Applied, result)
        verify(mockDpm.dpm).clearUserRestriction(
            mockDpm.admin,
            "no_debugging_features"
        )
    }

    @Test
    fun `reports supported on API 18+`() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return // Skip on older APIs
        }

        val policy = UsbDebugRestrictionPolicy(mockDpm)
        assert(policy.isSupported())
    }

    @Test
    fun `has usbDebug capability key`() {
        val policy = UsbDebugRestrictionPolicy(mockDpm)
        assertEquals("usbDebug", policy.capabilityKey)
    }
}
