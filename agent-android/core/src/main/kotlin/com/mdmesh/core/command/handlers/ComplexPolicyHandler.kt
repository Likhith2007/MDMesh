package com.mdmesh.core.command.handlers

import com.mdmesh.core.command.CommandHandler
import com.mdmesh.core.command.CommandResults
import com.mdmesh.policy.ComplexPolicy
import com.mdmesh.policy.PolicyOutcome
import com.mdmesh.proto.CommandEnvelope
import com.mdmesh.proto.CommandResult
import kotlinx.serialization.json.JsonObject

/**
 * `policy.apply` for complex policies — applies a policy with a context-aware payload.
 * Payload examples (per `proto/registry.md`):
 * - `{ "policy": "app.block", "packageName": "com.example.x", "value": true }`
 * - `{ "policy": "app.hide", "packageName": "com.example.y", "value": false }`
 * - `{ "policy": "internet.schedule", "schedules": [...] }`
 *
 * Routing is fully data-driven: the policy key is looked up in [complexPolicies] (built from the
 * device's supported strategies). Unknown / unsupported keys degrade to `unsupported`, mirroring
 * the open-registry contract.
 *
 * This handler is separate from [PolicyApplyHandler] to cleanly separate toggle (simple value)
 * from complex (full payload) policies. Both command handlers respond to `type: "policy.apply"`;
 * routing is determined by whether the policy key appears in the toggle or complex registry.
 */
class ComplexPolicyHandler(
    private val complexPolicies: Map<String, ComplexPolicy>,
) : CommandHandler {

    override val type: String = "policy.apply"

    override suspend fun handle(command: CommandEnvelope): CommandResult {
        val payload = command.payload as? JsonObject
            ?: return CommandResults.failed(command, "policy.apply requires a payload")

        val policyKey = payload["policy"]?.let {
            it.toString().trim('"')  // Handle JSON string serialization
        } ?: return CommandResults.failed(command, "policy.apply: missing policy key")

        val complexPolicy = complexPolicies[policyKey]
            ?: return CommandResults.unsupported(command, "policy not supported: $policyKey")

        return when (val outcome = complexPolicy.apply(payload)) {
            PolicyOutcome.Applied -> CommandResults.done(command)
            PolicyOutcome.Unsupported -> CommandResults.unsupported(command)
            is PolicyOutcome.Failed -> CommandResults.failed(command, outcome.reason)
        }
    }
}
