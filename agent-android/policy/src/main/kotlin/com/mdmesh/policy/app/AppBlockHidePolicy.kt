package com.mdmesh.policy.app

import android.os.Build
import com.mdmesh.policy.PolicyOutcome
import com.mdmesh.policy.wifi.DpmHandle
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * App blocking strategy for API 24+.
 * Uses [android.app.admin.DevicePolicyManager.setApplicationHidden] to hide/block apps.
 *
 * Payload expected: `{ "policy": "app.block", "packageName": "com.example.x", "value": true/false }`
 *
 * `value=true` → hide app (block)
 * `value=false` → unhide app (allow)
 */
internal class AppBlockHidePolicy(
    private val handle: DpmHandle,
) : AppBlockPolicy {

    override val capabilityKey: String = AppBlockPolicy.CAPABILITY_KEY

    override fun isSupported(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
            handle.dpm.isDeviceOwnerApp(handle.admin.packageName)

    override suspend fun apply(payload: JsonObject): PolicyOutcome = runCatching {
        val packageName = payload["packageName"]?.jsonPrimitive?.content
            ?: return PolicyOutcome.Failed("appBlock: missing packageName")
        val block = payload["value"]?.jsonPrimitive?.content?.toBoolean()
            ?: return PolicyOutcome.Failed("appBlock: missing value")

        // value=true => block (hide); value=false => allow (unhide).
        handle.dpm.setApplicationHidden(handle.admin, packageName, block)
        PolicyOutcome.Applied
    }.getOrElse { PolicyOutcome.Failed(it.message ?: "appBlock apply failed") }
}
