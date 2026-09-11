package com.mdmesh.policy.app

import android.os.Build
import com.mdmesh.policy.DpmHandle
import com.mdmesh.policy.PolicyOutcome
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import kotlinx.coroutines.test.runTest

/**
 * Unit tests for [AppBlockHidePolicy]:
 * - Parses packageName and value from JSON payload
 * - Calls setApplicationHidden with correct parameters
 * - Returns Failed if payload is malformed
 * - API guards: SDK 24+ only
 */
@RunWith(MockitoJUnitRunner::class)
class AppBlockPolicyTest {

    @Mock
    private lateinit var mockDpm: DpmHandle

    @Test
    fun `blocks app by setting hidden=true in payload`() = runTest {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return@runTest // Skip on older APIs
        }

        val policy = AppBlockHidePolicy(mockDpm)
        val payload = buildJsonObject {
            put("packageName", "com.example.app")
            put("value", true)
        }

        val result = policy.apply(payload)

        assertEquals(PolicyOutcome.Applied, result)
        verify(mockDpm.dpm).setApplicationHidden(mockDpm.admin, "com.example.app", true)
    }

    @Test
    fun `unblocks app by setting hidden=false in payload`() = runTest {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return@runTest
        }

        val policy = AppBlockHidePolicy(mockDpm)
        val payload = buildJsonObject {
            put("packageName", "com.example.app")
            put("value", false)
        }

        val result = policy.apply(payload)

        assertEquals(PolicyOutcome.Applied, result)
        verify(mockDpm.dpm).setApplicationHidden(mockDpm.admin, "com.example.app", false)
    }

    @Test
    fun `returns Failed if packageName is missing`() = runTest {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return@runTest
        }

        val policy = AppBlockHidePolicy(mockDpm)
        val payload = buildJsonObject {
            put("value", true)
        }

        val result = policy.apply(payload)

        assert(result is PolicyOutcome.Failed)
    }

    @Test
    fun `returns Failed if value is missing`() = runTest {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return@runTest
        }

        val policy = AppBlockHidePolicy(mockDpm)
        val payload = buildJsonObject {
            put("packageName", "com.example.app")
        }

        val result = policy.apply(payload)

        assert(result is PolicyOutcome.Failed)
    }

    @Test
    fun `has appBlock capability key`() {
        val policy = AppBlockHidePolicy(mockDpm)
        assertEquals("appBlock", policy.capabilityKey)
    }

    @Test
    fun `reports supported on API 24+`() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            assert(!AppBlockHidePolicy(mockDpm).isSupported())
        } else {
            assert(AppBlockHidePolicy(mockDpm).isSupported())
        }
    }
}
