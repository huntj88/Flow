package me.jameshunt.generate

import java.io.File

class FlowGenerator(private val file: File) {

    fun generate() {
        val states = PumlParser().parse(file)
        states.forEach(::println)

        println()
//        println(states.generateStates("Summary"))
//        println(states.generateFromInterfaces())
        println(SealedClassGenerator().generate("Summary", states))
        println(MethodsGenerator().generateAbstract("Summary", states))
        println(MethodsGenerator().generateStart("Summary", states))
        println(MethodsGenerator().generateToMethods("Summary", states))
    }


}

data class State(
    val name: String,
    val variables: Set<String>,
    val from: Set<String>
)


