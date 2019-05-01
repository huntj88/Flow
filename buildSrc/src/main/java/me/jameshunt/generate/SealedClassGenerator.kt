package me.jameshunt.generate

class SealedClassGenerator {

    fun generate(flowName: String, states: Set<State>, output: String): String {
        return """
            protected sealed class ${flowName}FlowState : State {
                ${states.generateFromInterfaces()}

                ${states.generateStates(flowName, output)}
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

    private fun Set<State>.generateStates(flowName: String, output: String): String {

        fun State.postFix(): String {
            val from = this.from.filter { it != "[*]" }.joinToString(", ") { "From$it" }.let {
                if (it.isNotBlank()) {
                    ", $it"
                } else ""
            }
            return ": ${flowName}FlowState()$from".let {
                when (this.name) {
                    "Back" -> "$it, BackState"
                    "Done" -> "$it, DoneState<$output>"
                    else -> it
                }
            }
        }

        return this.filter { it.name != "[*]" }.joinToString("\n") {
            when (it.name == "Done") {
                true -> "data class ${it.name}(override val output: $output)${it.postFix()}"
                false -> {
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
    }
}