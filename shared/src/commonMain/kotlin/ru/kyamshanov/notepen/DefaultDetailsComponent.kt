package ru.kyamshanov.notepen

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import ru.kyamshanov.notepen.DetailsComponent.Model

class DefaultDetailsComponent(
    componentContext: ComponentContext,
    title: String,
    private val onBackListener: () -> Unit,
) : DetailsComponent, ComponentContext by componentContext {
    override val model: Value<Model> =
        MutableValue(Model(title = title))

    override fun onBack() {
        onBackListener()
    }

}