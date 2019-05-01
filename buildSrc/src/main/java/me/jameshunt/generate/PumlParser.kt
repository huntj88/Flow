package me.jameshunt.generate

import java.io.File


private val transitionRegex = "(\\S+)\\s*[-]+>\\s*(\\S+)".toRegex()
private val dataRegex = "([a-zA-Z]+)\\s*:\\s*((val|var) [a-zA-Z]+: \\S+)".toRegex()
private val packageRegex = ": (\\S+)".toRegex()

fun String.isTransition() = this.contains(transitionRegex)
fun String.isData() = this.contains(dataRegex)
fun String.getPackage(): String = packageRegex.find(this)!!.groups[1]!!.value

class PumlParser {

    sealed class LineType {
        data class Transition(val line: String) : LineType()
        data class Data(val line: String) : LineType()
    }

    fun parse(file: File): StateSet {
        val lines = file.readLines().mapNotNull {
            when {
                it.isTransition() -> LineType.Transition(it)
                it.isData() -> LineType.Data(it)
                else -> null
            }
        }

        val dataLines = lines.mapNotNull { it as? LineType.Data }
        val transitionLines = lines.mapNotNull { it as? LineType.Transition }

        val states = transitionLines
            .identifyStates()
            .addVariables(dataLines)
            .addFrom(transitionLines)

        val inputOutput = states.inputOutput()
        return StateSet(states, inputOutput)
    }

    private fun Set<State>.inputOutput(): Pair<String, String> {
        val input = this.first { it.from.contains("[*]") }.variables.firstOrNull()?.let {
            it.split(" ").last()
        }?: "Unit"
        val output = this.firstOrNull { it.name == "Done" }?.variables?.firstOrNull()?.let {
            it.split(" ").last()
        }?: "Unit"
        return Pair(input,output)
    }

    private fun List<LineType.Transition>.identifyStates(): Set<State> {
        return this
            .map {
                listOf(
                    transitionRegex.find(it.line)!!.groups[1]?.value!!,
                    transitionRegex.find(it.line)!!.groups[2]?.value!!
                )
            }
            .flatten()
            .fold(setOf()) { acc, stateName ->
                acc + State(name = stateName, variables = setOf(), from = setOf(), imports = setOf())
            }
    }

    private fun Set<State>.addVariables(lines: List<LineType.Data>): Set<State> {
        val statesWithVariables = lines
            .map { stateData ->
                val stateName = dataRegex.find(stateData.line)!!.groups[1]?.value!!

                val variableWithPackage = dataRegex.find(stateData.line)!!.groups[2]?.value!!
                val packageName = variableWithPackage.getPackage()
                val variableSimpleType = variableWithPackage.split(".").last()
                val variableName = variableWithPackage.replace(packageName, variableSimpleType)

                this
                    .first { it.name == stateName }
                    .let { it.copy(variables = it.variables + variableName, imports = it.imports + packageName) }
            }
            .map { Pair(it.name, it) }
            .toMap()

        val rawStates = this.map { Pair(it.name, it) }.toMap()

        return (rawStates + statesWithVariables).map { it.value }.toSet()
    }

    private fun Set<State>.addFrom(lines: List<LineType.Transition>): Set<State> {

        val existingStates: MutableMap<String, State> = this
            .map { Pair(it.name, it) }
            .toMap()
            .toMutableMap()

        lines
            .map { stateData ->
                val from = transitionRegex.find(stateData.line)!!.groups[1]?.value!!
                val to = transitionRegex.find(stateData.line)!!.groups[2]?.value!!
                this.first { it.name == to }.let {
                    it.copy(from = it.from + from)
                }
            }
            .forEach {
                val existing  = existingStates[it.name]
                existingStates[it.name] = existing!!.copy(from = existing.from + it.from)
            }

        return existingStates.values.toSet()
    }
}
