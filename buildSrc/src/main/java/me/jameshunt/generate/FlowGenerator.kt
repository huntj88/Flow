package me.jameshunt.generate

import java.io.File

class FlowGenerator(private val file: File) {

    fun generate() {
        val states = PumlParser().parse(file)
        states.forEach(::println)

        println()
//        println(states.generateStates("Summary"))
//        println(states.generateFromInterfaces())
        println(states.generateSealedClass("Summary"))
    }

    private fun Set<State>.generateSealedClass(flowName: String): String {
        return """
            protected sealed class ${flowName}FlowState : State {
                ${this.generateFromInterfaces()}

                ${this.generateStates(flowName)}
            }
        """
    }

    private fun Set<State>.generateFromInterfaces(): String {
        return this
            .filter { it.name != "[*]" }
            .joinToString("\n") { "interface From${it.name}" }
    }

    private fun Set<State>.generateStates(flowName: String): String {

        fun State.postFix(): String {
            val from = this.from.filter { it != "[*]" }.joinToString(", ") { "From$it" }.let {
                if(it.isNotBlank()) { ", $it" } else ""
            }
            return ": ${flowName}FlowState()$from"
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

data class State(
    val name: String,
    val variables: Set<String>,
    val from: Set<String>
)


