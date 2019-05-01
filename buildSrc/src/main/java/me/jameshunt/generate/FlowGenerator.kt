package me.jameshunt.generate

import java.io.File

class FlowGenerator(private val file: File) {

    fun generate() {
        val (states, inputOutput) = PumlParser().parse(file)
        val (input, output) = inputOutput

        val flowName = file.nameWithoutExtension

        val imports = ImportsGenerator().generate(states)

        val generatedClass =
            "abstract class Generated${flowName}Controller(viewId: ViewId): FragmentFlowController<$input, $output>(viewId) {"
        val sealedClass = SealedClassGenerator().generate("Summary", states)
        val abstractMethods = MethodsGenerator().generateAbstract("Summary", states)
        val startMethod = MethodsGenerator().generateStart("Summary", states, input)

        val toMethods = MethodsGenerator().generateToMethods("Summary", states)

        """
            $imports
            $generatedClass
            $sealedClass
            $abstractMethods
            $startMethod
            $toMethods
            }
        """.let(::println)
    }
}

data class StateSet(
    val states: Set<State>,
    val inputOutput: Pair<String, String>
)

data class State(
    val name: String,
    val variables: Set<String>,
    val imports: Set<String>,
    val from: Set<String>
)


