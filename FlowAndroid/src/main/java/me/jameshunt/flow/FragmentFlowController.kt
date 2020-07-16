package me.jameshunt.flow

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

typealias ViewId = Int

interface AndroidFlowFunctions {
    suspend fun <NewInput, NewOutput, Controller> flow(
        controller: Class<Controller>,
        input: NewInput
    ): FlowResult<NewOutput>
            where Controller : FragmentFlowController<NewInput, NewOutput>

    suspend fun <FragInput, FragOutput, FragmentType> flow(
        fragmentProxy: FragmentProxy<FragInput, FragOutput, FragmentType>,
        input: FragInput
    ): FlowResult<FragOutput>
            where FragmentType : FlowUI<FragInput, FragOutput>

    suspend fun <GroupInput, GroupOutput, Controller> flowGroup(
        controller: Class<Controller>,
        input: GroupInput
    ): FlowResult<GroupOutput>
            where Controller : FragmentGroupFlowController<GroupInput, GroupOutput>

    suspend fun <NewInput, NewOutput, Controller> flowBusiness(
        controller: Class<Controller>,
        input: NewInput
    ): FlowResult<NewOutput>
            where Controller : BusinessFlowController<NewInput, NewOutput>
}

abstract class FragmentFlowController<Input, Output> : AndroidFlowController<Input, Output>() {

    internal var viewId: ViewId = 0 // is only set once at the beginning

    private val flowFunctions: AndroidFlowFunctions = AndroidFlowFunctionsImpl()

    private var activeFragment: FragmentProxy<*, *, *>? = null

    private var activeDialogFragment: FragmentProxy<*, *, *>? = null

    internal var uncommittedTransaction: (() -> Unit)? = null
        private set

    protected suspend fun <FragInput, FragOutput, FragmentType> flow(
        fragmentProxy: FragmentProxy<FragInput, FragOutput, FragmentType>,
        input: FragInput
    ): FlowResult<FragOutput>
            where FragmentType : FlowUI<FragInput, FragOutput> {
        return flowFunctions.flow(fragmentProxy = fragmentProxy, input = input)
    }

    protected suspend fun <NewInput, NewOutput, Controller> flow(
        controller: Class<Controller>,
        input: NewInput
    ): FlowResult<NewOutput>
            where Controller : FragmentFlowController<NewInput, NewOutput> {
        return flowFunctions.flow(controller = controller, input = input)
    }

    protected suspend fun <GroupInput, GroupOutput, Controller> flowGroup(
        controller: Class<Controller>,
        input: GroupInput
    ): FlowResult<GroupOutput>
            where Controller : FragmentGroupFlowController<GroupInput, GroupOutput> {
        return flowFunctions.flowGroup(controller = controller, input = input)
    }

    protected suspend fun <NewInput, NewOutput, Controller> flowBusiness(
        controller: Class<Controller>,
        input: NewInput
    ): FlowResult<NewOutput>
            where Controller : BusinessFlowController<NewInput, NewOutput> {
        return flowFunctions.flowBusiness(controller = controller, input = input)
    }

    /**
     * resuming only renders the view again
     * the fragment reattaches itself to the existing promise
     */

    final override suspend fun resume() {
        (activeFragment as? FragmentProxy<Any?, Any?, FlowFragment<Any?, Any?>>)?.let {
            fun showFragment() = FlowManager.fragmentDisplayManager.show(
                fragmentProxy = it,
                viewId = this.viewId
            )

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
                    uncommittedTransaction = null
                }
            }
        }
    }

    final override fun handleBack() {
        this.childFlows.firstOrNull()
            .let {
                when (it) {
                    is AndroidFlowController<*, *> -> {
                        activeFragment = null
                        it.handleBack()
                    }
                    is BusinessFlowController<*, *> -> businessDeferred.complete(FlowResult.Back)
                    else -> this.activeFragment?.back()
                }
            }
    }

    // promise wrapper around business flow to handle android back, even though BusinessFlowControllers
    // don't have a concept of going back
    private var businessDeferred: CompletableDeferred<FlowResult<Any?>> = CompletableDeferred()

    private inner class AndroidFlowFunctionsImpl : AndroidFlowFunctions {

        override suspend fun <NewInput, NewOutput, Controller> flow(
            controller: Class<Controller>,
            input: NewInput
        ): FlowResult<NewOutput>
                where Controller : FragmentFlowController<NewInput, NewOutput> {

            val flowController = controller.newInstance().apply {
                // apply same viewId to child
                this@apply.viewId = this@FragmentFlowController.viewId
            }

            childFlows.add(flowController)

            return try {
                flowController.launchFlow(input).await()
            } finally {
                childFlows.remove(flowController)
            }
        }

        override suspend fun <FragInput, FragOutput, FragmentType> flow(
            fragmentProxy: FragmentProxy<FragInput, FragOutput, FragmentType>,
            input: FragInput
        ): FlowResult<FragOutput>
                where FragmentType : FlowUI<FragInput, FragOutput> {

            val isDialog = FlowDialogFragment::class.java.isAssignableFrom(fragmentProxy.clazz)

            when (isDialog) {
                true -> {
                    activeDialogFragment = fragmentProxy.also { it.input = input }
                    activeFragment =
                        FlowManager.fragmentDisplayManager.getVisibleFragmentProxy(viewId)
                }
                false -> activeFragment = fragmentProxy.also { it.input = input }
            }

            suspend fun showFragmentForResult(): FlowResult<FragOutput> = try {
                FlowManager
                    .fragmentDisplayManager
                    .show(
                        fragmentProxy = fragmentProxy,
                        viewId = this@FragmentFlowController.viewId
                    )
                    .flowForResult()
            } finally {
                activeDialogFragment = null
            }

            return try {
                when (FlowManager.rootViewManager.isViewVisible(viewId)) {
                    true -> showFragmentForResult()
                    false -> {
                        val fragmentName = fragmentProxy.clazz.simpleName
                        val flowName = this::class.java.simpleName
                        val message =
                            "View does not exist for fragment: $fragmentName, in flow: $flowName"
                        throw IllegalStateException(message)
                    }
                }
            } catch (e: IllegalStateException) {
                e.printStackTrace()
                // from committing transaction after onSavedInstanceState,
                // or view does not exist
                uncommittedTransaction = {
                    FlowManager.launch(Dispatchers.Main) {
                        try {
                            showFragmentForResult()
                        } finally {
                            uncommittedTransaction = null
                        }
                    }
                }

                fragmentProxy.deferredPromise.await()
            }
        }

        override suspend fun <GroupInput, GroupOutput, Controller> flowGroup(
            controller: Class<Controller>,
            input: GroupInput
        ): FlowResult<GroupOutput>
                where Controller : FragmentGroupFlowController<GroupInput, GroupOutput> {

            // remove all the fragments from this flowController before starting the next FlowController
            // (state will still be saved when they get back)
            // The fragments parent views could potentially no longer exist
            FlowManager.fragmentDisplayManager.removeAll()

            val flowController = controller.newInstance()

            childFlows.add(flowController)

            return try {
                flowController.launchFlow(input).await()
            } finally {
                childFlows.remove(flowController)
                FlowManager.resumeActiveFlowControllers()
            }
        }

        override suspend fun <NewInput, NewOutput, Controller> flowBusiness(
            controller: Class<Controller>,
            input: NewInput
        ): FlowResult<NewOutput>
                where Controller : BusinessFlowController<NewInput, NewOutput> {

            val flowController = controller.newInstance()
            childFlows.add(flowController)

            // businessDeferred is resolvable outside of newly launched flow to handle android back
            try {
                flowController
                    .launchFlow(input)
                    .await()
                    .also { businessDeferred.complete(FlowResult.Completed(it)) }
            } catch (e: Exception) {
                businessDeferred.completeExceptionally(e)
            }


            return try {
                businessDeferred.await()
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            } finally {
                childFlows.remove(flowController)
                businessDeferred = CompletableDeferred()
            } as FlowResult<NewOutput>
        }
    }
}