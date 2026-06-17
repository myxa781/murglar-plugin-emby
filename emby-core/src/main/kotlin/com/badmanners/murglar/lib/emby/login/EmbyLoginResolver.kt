package com.badmanners.murglar.lib.emby.login

import com.badmanners.murglar.lib.core.localization.Messages.Companion.loginWith
import com.badmanners.murglar.lib.core.login.CredentialsLoginVariant
import com.badmanners.murglar.lib.core.login.CredentialsLoginVariant.Credential
import com.badmanners.murglar.lib.core.login.CredentialLoginStep
import com.badmanners.murglar.lib.core.login.LoginResolver
import com.badmanners.murglar.lib.core.login.SuccessfulLogin
import com.badmanners.murglar.lib.core.login.WebLoginVariant
import com.badmanners.murglar.lib.core.network.NetworkMiddleware
import com.badmanners.murglar.lib.core.preference.PreferenceMiddleware
import com.badmanners.murglar.lib.core.webview.WebViewProvider
import com.badmanners.murglar.lib.emby.EmbyMurglar
import com.badmanners.murglar.lib.emby.localization.EmbyMessages

class EmbyLoginResolver(
    private val preferences: PreferenceMiddleware,
    private val network: NetworkMiddleware,
    private val murglar: EmbyMurglar,
    private val messages: EmbyMessages
) : LoginResolver {

    companion object {
        const val PREF_SERVER_URL = "emby_server_url"
        const val PREF_TOKEN      = "emby_token"
        const val PREF_USER_ID    = "emby_user_id"
        const val PREF_USERNAME   = "emby_username"

        private const val VARIANT_APIKEY   = "apikey"
        private const val VARIANT_USERPASS = "userpass"
        private const val CRED_SERVER_URL  = "server_url"
        private const val CRED_TOKEN       = "token"
        private const val CRED_USERNAME    = "username"
        private const val CRED_PASSWORD    = "password"
        private const val NO_VALUE         = ""
    }

    override val isLogged: Boolean
        get() = preferences.getString(PREF_TOKEN, NO_VALUE).isNotEmpty()

    override val loginInfo: String
        get() = when {
            isLogged -> "${messages.youAreLoggedIn}: ${preferences.getString(PREF_USERNAME, NO_VALUE)}"
            else -> messages.youAreNotLoggedIn
        }

    override val webLoginVariants: List<WebLoginVariant>
        get() = emptyList()

    override val credentialsLoginVariants = listOf(
        CredentialsLoginVariant(
            id = VARIANT_APIKEY,
            label = { messages.loginWith(token = true) },
            credentials = listOf(
                Credential(CRED_SERVER_URL, messages::serverUrlTitle),
                Credential(CRED_TOKEN, messages::token)
            )
        ),
        CredentialsLoginVariant(
            id = VARIANT_USERPASS,
            label = { messages.loginWith(username = true) },
            credentials = listOf(
                Credential(CRED_SERVER_URL, messages::serverUrlTitle),
                Credential(CRED_USERNAME, messages::username),
                Credential(CRED_PASSWORD, messages::password)
            )
        )
    )

    override suspend fun credentialsLogin(loginVariantId: String, args: Map<String, String>): CredentialLoginStep {
        logout()
        val url = args[CRED_SERVER_URL]!!.trimEnd('/')
        preferences.setString(PREF_SERVER_URL, url)

        when (loginVariantId) {
            VARIANT_APIKEY -> {
                val apiKey = args[CRED_TOKEN]!!
                val uid = murglar.api.getUserId(apiKey)
                preferences.setString(PREF_TOKEN, apiKey)
                preferences.setString(PREF_USER_ID, uid)
                preferences.setString(PREF_USERNAME, "api_key")
            }
            VARIANT_USERPASS -> {
                val (token, uid, name) = murglar.api.authenticateByName(
                    args[CRED_USERNAME]!!, args[CRED_PASSWORD]!!
                )
                preferences.setString(PREF_TOKEN, token)
                preferences.setString(PREF_USER_ID, uid)
                preferences.setString(PREF_USERNAME, name)
            }
        }
        return SuccessfulLogin
    }

    override fun logout() {
        network.clearAllCookies()
        preferences.remove(PREF_TOKEN)
        preferences.remove(PREF_USER_ID)
        preferences.remove(PREF_USERNAME)
    }

    override suspend fun webLogin(loginVariantId: String, webViewProvider: WebViewProvider): Boolean {
        throw UnsupportedOperationException()
    }
}
