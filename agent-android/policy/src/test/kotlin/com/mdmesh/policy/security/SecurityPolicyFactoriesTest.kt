package com.mdmesh.policy.security

import android.os.Build
import com.mdmesh.policy.DpmHandle
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

/**
 * Unit tests for security policy factories:
 * - Verify factory probes return null on unsupported APIs
 * - Verify factories return policy instances on supported APIs
 */
@RunWith(MockitoJUnitRunner::class)
class SecurityPolicyFactoriesTest {

    @Mock
    private lateinit var mockHandle: DpmHandle

    @Test
    fun `UsbDebugPolicyFactory returns policy on API 18+`() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            assertNull("Factory should return null on API < 18", UsbDebugPolicyFactory.create(mockHandle))
        } else {
            assertNotNull("Factory should return policy on API >= 18", UsbDebugPolicyFactory.create(mockHandle))
        }
    }

    @Test
    fun `FactoryResetPolicyFactory returns policy on API 18+`() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            assertNull("Factory should return null on API < 18", FactoryResetPolicyFactory.create(mockHandle))
        } else {
            assertNotNull("Factory should return policy on API >= 18", FactoryResetPolicyFactory.create(mockHandle))
        }
    }

    @Test
    fun `UnknownSourcesPolicyFactory returns policy on API 28+ only`() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            assertNull("Factory should return null on API < 28", UnknownSourcesPolicyFactory.create(mockHandle))
        } else {
            assertNotNull("Factory should return policy on API >= 28", UnknownSourcesPolicyFactory.create(mockHandle))
        }
    }

    @Test
    fun `AdminRemovalPolicyFactory returns policy on API 24+`() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            assertNull("Factory should return null on API < 24", AdminRemovalPolicyFactory.create(mockHandle))
        } else {
            assertNotNull("Factory should return policy on API >= 24", AdminRemovalPolicyFactory.create(mockHandle))
        }
    }
}
