package com.example.wanderly.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wanderly.auth.SessionManager
import com.example.wanderly.local.DatabaseProvider
import com.example.wanderly.local.users.UserEntity
import com.example.wanderly.repository.AuthRepository
import com.example.wanderly.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentUser: UserEntity? = null,
    val sessionRestored: Boolean = false,
    val hasSeenOnboarding: Boolean = false,
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: AuthRepository =
        AuthRepository(DatabaseProvider.getDatabase(application).userDao())
    private val session: SessionManager = SessionManager(application)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val savedId = session.currentUserId
            val restoredUser = savedId?.let { repo.findById(it) }
            // If the stored ID points to a missing user (e.g. DB wiped), clear the session.
            if (savedId != null && restoredUser == null) session.currentUserId = null
            _uiState.update {
                it.copy(
                    currentUser = restoredUser,
                    sessionRestored = true,
                    hasSeenOnboarding = session.hasSeenOnboarding,
                )
            }
        }
    }

    fun login(username: String, password: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            handleResult(repo.login(username, password))
        }
    }

    fun signup(username: String, displayName: String, password: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            handleResult(repo.signup(username, displayName, password))
        }
    }

    fun logout() {
        session.currentUserId = null
        _uiState.update { it.copy(currentUser = null, errorMessage = null) }
    }

    fun completeOnboarding() {
        session.hasSeenOnboarding = true
        _uiState.update { it.copy(hasSeenOnboarding = true) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun handleResult(result: AuthResult) {
        when (result) {
            is AuthResult.Success -> {
                session.currentUserId = result.user.id
                _uiState.update { it.copy(isLoading = false, currentUser = result.user) }
            }
            is AuthResult.Error -> {
                _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }
}
