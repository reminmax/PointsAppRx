package com.reminmax.pointsapp.ui.screens.chart

import androidx.lifecycle.SavedStateHandle
import com.reminmax.pointsapp.R
import com.reminmax.pointsapp.domain.model.LinearChartStyle
import com.reminmax.pointsapp.domain.model.Point
import com.reminmax.pointsapp.domain.model.UserMessage
import com.reminmax.pointsapp.domain.resource_provider.IResourceProvider
import com.reminmax.pointsapp.ui.base.BaseViewModel
import com.reminmax.pointsapp.ui.navigation.NavigationParams
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChartViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val resourceProvider: IResourceProvider,
) : BaseViewModel() {

    private val _uiState = BehaviorSubject.createDefault(ChartUiState())
    val uiState: Observable<ChartUiState> = _uiState.hide()

    private val _userMessages = BehaviorSubject.createDefault<List<UserMessage>>(emptyList())
    val userMessages: Observable<List<UserMessage>> = _userMessages.hide()

    // Примечание:
    // Вопреки настоятельной рекомендации Google не передавать сложные объекты в качестве
    // параметров навигации (вместо этого передавать ключ для извлечения данных),
    // https://developer.android.com/jetpack/compose/navigation#retrieving-complex-data
    // данная реализация состоит в передаче списка точек в формате json в качестве параметра
    // навигации.
    //
    // Данный подход считаю допустимым по двум причинам:
    // 1. Простота реализации (это не коммерческий проект); применение SharedViewModel,
    // создание базы данных, использование DataStore (Shared Preferences) и др. распространенных
    // способов передачи состояния нахожу избыточным.
    // 2. Максимально допустимый размер сохраненного состояния Android (1Mb) гарантированно не
    // будет превышен, так как максимальное количество точек ограничено диапазоном 1..100 путем
    // валидации введенного пользователем значения (класс Validator).
    init {
        savedStateHandle.get<String>(NavigationParams.POINTS)?.let { points ->
            _uiState.value?.copy(
                points = Json.decodeFromString<List<Point>>(points)
            )?.let { _uiState.onNext(it) }
        }
    }

    fun dispatch(action: ChartAction) {
        when (action) {
            is ChartAction.ChartStyleSelected -> {
                onChartStyleSelected(action.style)
            }

            ChartAction.SaveChartToFile -> {
                sendEvent(ChartScreenEvent.SaveChartToFile)
            }

            is ChartAction.ShowUserMessage -> {
                onShowUserMessage(
                    messageToShow = action.messageToShow,
                    actionLabel = action.actionLabel ?: resourceProvider.getString(R.string.ok),
                    onActionPerformed = action.onActionPerformed,
                )
            }

            is ChartAction.UserMessageShown -> {
                onUserMessageShown(action.messageId)
            }
        }
    }

    private fun onChartStyleSelected(style: LinearChartStyle) {
        _uiState.value?.copy(chartStyle = style)?.let { _uiState.onNext(it) }
    }

    private fun onShowUserMessage(
        messageToShow: String,
        actionLabel: String?,
        onActionPerformed: () -> Unit,
    ) {
        val newMessage = UserMessage(
            id = UUID.randomUUID().mostSignificantBits,
            message = messageToShow,
            actionLabel = actionLabel,
            onActionPerformed = onActionPerformed,
        )
        _userMessages.value?.let { messages ->
            _userMessages.onNext(messages + newMessage)
        }
    }

    private fun onUserMessageShown(messageId: Long) {
        _userMessages.value?.let { messages ->
            _userMessages.onNext(messages.filterNot { it.id == messageId })
        }
    }
}