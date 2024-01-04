package com.github.abaker.googlevoice.list

import Voice
import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.abaker.googlevoice.GoogleVoiceClient
import com.github.abaker.googlevoice.model.Thread
import com.github.abaker.googlevoice.proto.parseData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class MessageListViewModel(
    private val client: GoogleVoiceClient = GoogleVoiceClient(),
) : ViewModel() {

    data class State(
        val inbox: List<Thread> = emptyList()
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    private suspend fun fetchInbox(): List<Thread> {
        return try {
            val response = client.getThreads() ?: return emptyList()
            println("post: $response")
            Timber.d("post: $response")
            parseData(Voice.ListResponse::class.java, response)
                .threadList
                .map {
                    Thread(
                        id = it.id,
                        title = it.headingContactsPhoneNumberKeyList.joinToString(", "),
                        preview = it.messagesList.first().messageText,
                        isGroup = it.headingContactsPhoneNumberKeyCount > 1,
                    )
                }
        } catch (e: Exception) {
            Timber.e(e)
            emptyList()
        }
    }

    init {
        viewModelScope.launch {
            val inbox = fetchInbox()
            println(inbox)
            Timber.d("inbox: $inbox")
            _state.update {
                it.copy(
                    inbox = inbox
                )
            }
        }
    }
}