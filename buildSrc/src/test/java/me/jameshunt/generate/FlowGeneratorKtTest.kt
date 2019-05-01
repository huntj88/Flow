package me.jameshunt.generate

import org.junit.Assert.*
import org.junit.Test

class FlowGeneratorKtTest {

    @Test
    fun isTransitionTest() {
        println("wow")
        assertTrue("[*]->GatherData".isTransition())
        assertFalse("[*] - >GatherData".isTransition())
        assertTrue("[*] -> GatherData".isTransition())
        assertTrue("GatherData -> Render".isTransition())
        assertTrue("GatherData --> Render".isTransition())
        assertTrue("GatherData ---> Render".isTransition())
    }

    @Test
    fun isDataTest() {
        assertFalse("GatherData : vl blah: Book".isData())
        assertFalse("GatherData : val blah1: Book".isData()) // should i let numbers?
        assertTrue("GatherData : val blah: Book".isData())
        assertTrue("GatherData:val blah: Book".isData())
        assertTrue("GatherData:var blah: Book".isData())
        assertTrue("GatherData: var blah: Book".isData())
        assertTrue("Render : val data: me.jameshunt.flow3.summary.SummaryInput".isData())
        assertFalse("GatherData val blah: Book".isData())
    }

    @Test
    fun packageNameTest() {
        "val data: me.jameshunt.flow3.summary.SummaryInput".getPackage().let(::println)
        assertTrue("val data: me.jameshunt.flow3.summary.SummaryInput".getPackage() == "me.jameshunt.flow3.summary.SummaryInput")
    }
}