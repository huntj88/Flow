package me.jameshunt.generate

import java.io.File

class FlowGenerator(private val file: File) {

    fun generate() {
        val states = PumlParser().parse(file)

        val flowName = file.nameWithoutExtension

        val imports = """
            import me.jameshunt.flow.FragmentFlowController
            import me.jameshunt.flow.ViewId
            import me.jameshunt.flow.promise.Promise
            import me.jameshunt.flow.promise.then
        """

        val generatedClass =
            "abstract class Generated${flowName}Controller(viewId: ViewId): FragmentFlowController<Unit, Unit>(viewId) {"
        val sealedClass = SealedClassGenerator().generate("Summary", states)
        val abstractMethods = MethodsGenerator().generateAbstract("Summary", states)
        val startMethod = MethodsGenerator().generateStart("Summary", states)

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

data class State(
    val name: String,
    val variables: Set<String>,
    val from: Set<String>
)


