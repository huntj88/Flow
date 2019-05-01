package me.jameshunt.generate

class ImportsGenerator {

    fun generate(states: Set<State>): String {
       val variables =  states
            .flatMap { it.imports }.toSet()
            .joinToString("\n") { "import $it" }

        return """
            import me.jameshunt.flow.FragmentFlowController
            import me.jameshunt.flow.ViewId
            import me.jameshunt.flow.promise.Promise
            import me.jameshunt.flow.promise.then
            $variables
        """
    }
}