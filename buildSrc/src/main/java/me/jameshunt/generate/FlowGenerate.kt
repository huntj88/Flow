package me.jameshunt.generate

import java.io.File
import java.nio.file.Paths

object FlowGenerate {

    @JvmStatic
    fun generate() {
        println("sup")

        val file = File("${Paths.get("").toAbsolutePath()}/app/src/main/java/me/jameshunt/flow3/summary/PillReminder.puml")
//        val file = File("${Paths.get("").toAbsolutePath()}/app/src/main/java/me/jameshunt/flow3/summary/Summary.puml")

        FlowGenerator(file).generate()
    }
}