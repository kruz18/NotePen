package ru.kyamshanov.notepen

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import ru.kyamshanov.notepen.ListComponent.Model

class DefaultListComponent(
    componentContext: ComponentContext,
    private val onItemClickedListener: (item: String) -> Unit,
) : ListComponent, ComponentContext by componentContext {

    override val model: Value<Model> =
        MutableValue(Model(items = List(100) {
            if(it == 0) """C:\Users\kruz18\Documents\English\Voc In Use Upper-Interm_202509160923_58831.pdf"""
            else "Item $it" }))

    override fun onItemClicked(item: String) {
        onItemClickedListener(item)
    }
}