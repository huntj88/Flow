package me.jameshunt.flow

import com.inmotionsoftware.promisekt.Promise
import com.inmotionsoftware.promisekt.ensure

typealias ViewId = Int

interface AndroidFlowFunctions {
    fun <NewInput, NewOutput, Controller> flow(
        controller: Class<Controller>,
        input: NewInput
    ): Promise<FlowResult<NewOutput>>
            where Controller : FragmentFlowController<NewInput, NewOutput>

    fun <FragInput, FragOutput, FragmentType> flow(
        fragmentProxy: FragmentProxy<FragInput, FragOutput, FragmentType>,
        input: FragInput
    ): Promise<FlowResult<FragOutput>>
            where FragmentType : FlowUI<FragInput, FragOutput>

    fun <GroupInput, GroupOutput, Controller> flowGroup(
        controller: Class<Controller>,
        input: GroupInput
    ): Promise<FlowResult<GroupOutput>>
            where Controller : FragmentGroupFlowController<GroupInput, GroupOutput>

    fun <NewInput, NewOutput, Controller> flowNoUI(
        controller: Class<Controller>,
        input: NewInput
    ): Promise<NewOutput>
            where Controller : FlowController<NewInput, NewOutput>
}

abstract class FragmentFlowController<Input, Output> : AndroidFlowController<Input, Output>() {

    internal var viewId: ViewId = 0 // is only set once at the beginning

    private var flowFunctions: AndroidFlowFunctions = AndroidFlowFunctionsImpl()

    private var activeFragment: FragmentProxy<*, *, *>? = null

    private var activeDialogFragment: FragmentProxy<*, *, *>? = null

    internal var uncommittedTransaction: (() -> Unit)? = null
        private set

    fun <FragInput, FragOutput, FragmentType> flow(
        fragmentProxy: FragmentProxy<FragInput, FragOutput, FragmentType>,
        input: FragInput
    ): Promise<FlowResult<FragOutput>>
            where FragmentType : FlowUI<FragInput, FragOutput> {
        return flowFunctions.flow(fragmentProxy = fragmentProxy, input = input)
    }

    fun <NewInput, NewOutput, Controller> flow(
        controller: Class<Controller>,
        input: NewInput
    ): Promise<FlowResult<NewOutput>>
            where Controller : FragmentFlowController<NewInput, NewOutput> {
        return flowFunctions.flow(controller = controller, input = input)
    }

    fun <NewInput, NewOutput, Controller> flowNoUI(
        controller: Class<Controller>,
        input: NewInput
    ): Promise<NewOutput>
            where Controller : FlowController<NewInput, NewOutput> {
        return flowFunctions.flowNoUI(controller = controller, input = input)
    }

    fun <GroupInput, GroupOutput, Controller> flowGroup(
        controller: Class<Controller>,
        input: GroupInput
    ): Promise<FlowResult<GroupOutput>>
            where Controller : FragmentGroupFlowController<GroupInput, GroupOutput> {
        return flowFunctions.flowGroup(controller = controller, input = input)
    }

    // internal to this instance use
    final override fun launchFlow(input: Input): Promise<FlowResult<Output>> {
        currentState = InitialState(input)
        return super.launchFlow(input)
    }

    /**
     * resuming only renders the view again
     * the fragment reattaches itself to the existing promise
     */

    final override fun resume(currentState: State) {
        (activeFragment as? FragmentProxy<Any?, Any?, FlowFragment<Any?, Any?>>)?.let {
            val showFragment: () -> Unit = {
                FlowManager.fragmentDisplayManager.show(
                    fragmentProxy = it,
                    viewId = this.viewId
                )
            }

            try {
                showFragment()
            } catch (e: IllegalStateException) {
                e.printStackTrace() // from committing transaction after onSavedInstanceState
                uncommittedTransaction = { showFragment(); uncommittedTransaction = null }
            }
        }

        (activeDialogFragment as? FragmentProxy<Any?, Any?, FlowDialogFragment<Any?, Any?>>)?.let {
            // DialogFragments are kept around in FragmentManager memory longer,
            // so null out the instance so it won't try and show the instance from the old activity
            it.fragment = null

            fun showDialogFragment() = FlowManager.fragmentDisplayManager.show(
                fragmentProxy = it,
                viewId = this.viewId
            )

            try {
                showDialogFragment()
            } catch (e: IllegalStateException) {
                e.printStackTrace() // from committing transaction after onSavedInstanceState
                uncommittedTransaction = {
                    uncommittedTransaction?.invoke() // show the fragment behind dialog
                    showDialogFragment()
                }
            }
        }
    }

    fun DoneState<Output>.onDone() {
        FlowManager.fragmentDisplayManager.remove(activeFragment)
        super.onDone(FlowResult.Completed(output))
    }

    protected fun BackState.onBack() {
        super.onDone(FlowResult.Back)
    }

    final override fun handleBack() {
        // does not call FlowController.onBack() ever. that must be done explicitly with a state transition
        this.childFlows.firstOrNull()
            ?.let { it as? AndroidFlowController<*, *> }
            ?.handleBack()
            ?: this.activeFragment?.onBack()
    }

    inner class AndroidFlowFunctionsImpl : AndroidFlowFunctions {

        override fun <NewInput, NewOutput, Controller> flow(
            controller: Class<Controller>,
            input: NewInput
        ): Promise<FlowResult<NewOutput>>
                where Controller : FragmentFlowController<NewInput, NewOutput> {

            val flowController = controller.newInstance().apply {
                // apply same viewId to child
                this@apply.viewId = this@FragmentFlowController.viewId
            } as FlowController<NewInput, FlowResult<NewOutput>>

            childFlows.add(flowController)

            return flowController.launchFlow(input).ensure {
                childFlows.remove(flowController)
            }
        }

        override fun <FragInput, FragOutput, FragmentType> flow(
            fragmentProxy: FragmentProxy<FragInput, FragOutput, FragmentType>,
            input: FragInput
        ): Promise<FlowResult<FragOutput>>
                where FragmentType : FlowUI<FragInput, FragOutput> {

            val isDialog = FlowDialogFragment::class.java.isAssignableFrom(fragmentProxy.clazz)

            when (isDialog) {
                true -> {
                    activeDialogFragment = fragmentProxy.also { it.input = input }
                    activeFragment = FlowManager.fragmentDisplayManager.getVisibleFragmentProxy(viewId)
                }
                false -> activeFragment = fragmentProxy.also { it.input = input }
            }

            fun showFragmentForResult(): Promise<FlowResult<FragOutput>> = FlowManager
                .fragmentDisplayManager
                .show(fragmentProxy = fragmentProxy, viewId = this@FragmentFlowController.viewId)
                .flowForResult()
                .ensure {
                    activeFragment = null

                    if (isDialog) {
                        activeDialogFragment = null
                    }
                }

            return try {
                when (FlowManager.rootViewManager.isViewVisible(viewId)) {
                    true -> showFragmentForResult()
                    false -> {
                        val fragmentName = fragmentProxy.clazz.simpleName
                        val flowName = this::class.java.simpleName
                        val message = "View does not exist for fragment: $fragmentName, in flow: $flowName"
                        throw IllegalStateException(message)
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

        override fun <GroupInput, GroupOutput, Controller> flowGroup(
            controller: Class<Controller>,
            input: GroupInput
        ): Promise<FlowResult<GroupOutput>>
                where Controller : FragmentGroupFlowController<GroupInput, GroupOutput> {

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

        override fun <NewInput, NewOutput, Controller> flowNoUI(
            controller: Class<Controller>,
            input: NewInput
        ): Promise<NewOutput>
                where Controller : FlowController<NewInput, NewOutput> {

            val flowController = controller.newInstance()

            childFlows.add(flowController)

            activeFragment = FlowManager.fragmentDisplayManager.getVisibleFragmentProxy(viewId)

            return flowController.launchFlow(input).ensure {
                childFlows.remove(flowController)
                activeFragment = null
            }
        }
    }
}