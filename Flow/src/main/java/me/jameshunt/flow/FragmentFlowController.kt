package me.jameshunt.flow

import com.inmotionsoftware.promisekt.Promise
import com.inmotionsoftware.promisekt.ensure

typealias ViewId = Int

abstract class FragmentFlowController<Input, Output> : FlowController<Input, Output>() {

    interface DoneState<Output> {
        val output: Output
    }

    internal var viewId: ViewId = 0 // is only set once at the beginning

    private var activeFragment: FragmentProxy<*, *, *>? = null

    private var activeDialogFragment: FragmentProxy<*, *, *>? = null

    internal var uncommittedTransaction: (() -> Unit)? = null
        private set

    fun <FragInput, FragOutput, FragmentType : FlowUI<FragInput, FragOutput>> flow(
        fragmentProxy: FragmentProxy<FragInput, FragOutput, FragmentType>,
        input: FragInput
    ): Promise<FlowResult<FragOutput>> {
        val isDialog = FlowDialogFragment::class.java.isAssignableFrom(fragmentProxy.clazz)

        when(isDialog) {
            true -> {
                activeDialogFragment = fragmentProxy.also { it.input = input }
                activeFragment = FlowManager.fragmentDisplayManager.getVisibleFragmentBehindDialog(viewId)
            }
            false -> activeFragment = fragmentProxy.also { it.input = input }
        }

        val showFragmentForResult: () -> Promise<FlowResult<FragOutput>> = {
            FlowManager.fragmentDisplayManager
                .show(fragmentProxy = fragmentProxy, viewId = this.viewId)
                .flowForResult()
                .ensure {
                    activeFragment = null

                    if(isDialog) {
                        activeDialogFragment = null
                    }
                }
        }

        return try {
            when (FlowManager.rootViewManager.isViewVisible(viewId)) {
                true -> showFragmentForResult()
                false -> {
                    val fragmentName = fragmentProxy.clazz.simpleName
                    val flowName = this::class.java.simpleName
                    throw IllegalStateException("View does not exist for fragment: $fragmentName, in flow: $flowName")
                }
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            // from committing transaction after onSavedInstanceState,
            // or view does not exist
            uncommittedTransaction = {
                showFragmentForResult().ensure { uncommittedTransaction = null }
            }

            fragmentProxy.deferredPromise.promise
        }
    }

    fun <GroupInput, GroupOutput, Controller> flowGroup(
        controller: Class<Controller>,
        input: GroupInput
    ): Promise<FlowResult<GroupOutput>>
            where GroupInput : FragmentGroupFlowController.GroupInput,
                  Controller : FragmentGroupFlowController<GroupInput, GroupOutput> {

        // remove all the fragments from this flowController before starting the next FlowController
        // (state will still be saved when they get back)
        // The fragments parent views could potentially no longer exist
        FlowManager.fragmentDisplayManager.removeAll()

        val flowController = controller.newInstance()

        childFlows.add(flowController)

        return flowController.launchFlow(input).ensure {
            childFlows.remove(flowController)
            FlowManager.resumeActiveFlowControllers()
        }
    }

    fun <NewInput, NewOutput, Controller : FragmentFlowController<NewInput, NewOutput>> flow(
        controller: Class<Controller>,
        input: NewInput
    ): Promise<FlowResult<NewOutput>> {
        val flowController = controller.newInstance().apply {
            // apply same viewId to child
            this@apply.viewId = this@FragmentFlowController.viewId
        }

        childFlows.add(flowController)

        return flowController.launchFlow(input).ensure {
            childFlows.remove(flowController)
        }
    }

    final override fun resume(currentState: State) {
        (activeFragment as? FragmentProxy<Any?, Any?, FlowFragment<Any?, Any?>>)?.let {
            FlowManager.fragmentDisplayManager.show(
                fragmentProxy = it,
                viewId = this.viewId
            )
        }

        (activeDialogFragment as? FragmentProxy<Any?, Any?, FlowDialogFragment<Any?, Any?>>)?.let {
            // DialogFragments are kept around in FragmentManager memory longer,
            // so null out the instance so it won't try and show the instance from the old activity
            it.fragment = null

            FlowManager.fragmentDisplayManager.show(
                fragmentProxy = it,
                viewId = this.viewId
            )
        }
    }

    fun DoneState<Output>.onDone() {
        FlowManager.fragmentDisplayManager.remove(activeFragment)
        super.onDone(output)
    }

    final override fun handleBack() {
        // does not call FlowController.onBack() ever. that must be done explicitly with a state transition
        this.childFlows.firstOrNull()?.handleBack() ?: this.activeFragment?.onBack()
    }
}
