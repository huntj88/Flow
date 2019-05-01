package me.jameshunt.generate

class MethodsGenerator {

    fun generateAbstract(flowName: String, states: Set<State>): String {
        return states
            .filter { it.name != "[*]" }
            .filter { it.name != "Done" }
            .filter { it.name != "Back" }
            .joinToString("\n") {
                "protected abstract fun on${it.name}(state: ${flowName}FlowState.${it.name}): Promise<${flowName}FlowState.From${it.name}>"
            }
    }

    fun generateStart(
        flowName: String,
        states: Set<State>,
        input: String
    ): String {
        val from = states.firstOrNull { it.from.contains("[*]") }?.name!!

        val impl = when(input == "Unit") {
            true -> "to$from(${flowName}FlowState.$from)"
            false -> "to$from(${flowName}FlowState.$from(state.input))"
        }

        return """
            final override fun onStart(state: InitialState<$input>) {
                $impl
            }
        """
    }

    fun generateToMethods(flowName: String, states: Set<State>): String {
        return states
            .filter { it.name != "[*]" }
            .filter { it.name != "Done" }
            .filter { it.name != "Back" }
            .joinToString("") {
                it.generateToMethod(flowName, states.fromWhen(flowName, it))
            }
    }

    private fun State.generateToMethod(flowName: String, fromWhen: String): String {
        return """
            private fun to${this.name}(state: ${flowName}FlowState.${this.name}) {
                currentState = state
                on${this.name}(state).then {
                    when(it) {
                        $fromWhen
                        else -> throw IllegalStateException("Illegal transition from: ${"$"}state, to: ${"$"}it")
                    }
                }
            }
        """
    }

    private fun Set<State>.fromWhen(flowName: String, state: State): String {

        fun String.handleBackAndDone(): String {
            return when(this) {
                "Back" -> "it.onBack()"
                "Done" -> "it.onDone()"
                else -> "to$this(it)"
            }
        }

        return this.filter { it.from.contains(state.name) }.joinToString("\n") {
            "is ${flowName}FlowState.${it.name} -> ${it.name.handleBackAndDone()}"
        }
    }
}