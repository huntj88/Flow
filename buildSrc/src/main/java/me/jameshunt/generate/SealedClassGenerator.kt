package me.jameshunt.generate

class SealedClassGenerator {

    fun generate(flowName: String, states: Set<State>): String {
        return """
            protected sealed class ${flowName}FlowState : State {
                ${states.generateFromInterfaces()}

                ${states.generateStates(flowName)}
            }
        """
    }

    private fun Set<State>.generateFromInterfaces(): String {
        return this
            .filter { it.name != "[*]" }
            .filter { it.name != "Done" }
            .filter { it.name != "Back" }
            .joinToString("\n") { "interface From${it.name}" }
    }

    private fun Set<State>.generateStates(flowName: String): String {

        fun State.postFix(): String {
            val from = this.from.filter { it != "[*]" }.joinToString(", ") { "From$it" }.let {
                if(it.isNotBlank()) { ", $it" } else ""
            }
            return ": ${flowName}FlowState()$from".let {
                if(this.name == "Back") "$it, BackState"
                else it
            }
        }

        return this.filter { it.name != "[*]" }.joinToString("\n") {
            when (it.variables.isEmpty()) {
                true -> {
                    "object ${it.name}${it.postFix()}"
                }
                false -> {
                    "data class ${it.name}(${it.variables.joinToString(",")})${it.postFix()}"
                }
            }
        }
    }
}