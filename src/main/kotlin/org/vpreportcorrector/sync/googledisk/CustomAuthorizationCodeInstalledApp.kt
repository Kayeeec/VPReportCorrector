package org.vpreportcorrector.sync.googledisk

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver
import tornadofx.find
import java.io.IOException
import java.util.logging.Logger

/**
 * @param flow authorization code flow
 * @param receiver verification code receiver
 */
class CustomAuthorizationCodeInstalledApp(
    flow: AuthorizationCodeFlow,
    receiver: VerificationCodeReceiver
): AuthorizationCodeInstalledApp(flow, receiver) {
    private val log by lazy { Logger.getLogger(this.javaClass.name) }

    @Throws(IOException::class)
    override fun authorize(userId: String?): Credential? {
        return try {
            val credential = flow.loadCredential(userId)
            if (credential != null
                && (credential.refreshToken != null || credential.expiresInSeconds == null || credential.expiresInSeconds > 60)
            ) {
                return credential
            }
            // open in browser
            val redirectUri = receiver.redirectUri
            val authorizationUrl = flow.newAuthorizationUrl().setRedirectUri(redirectUri)
            onAuthorization(authorizationUrl)
            // receive authorization code and exchange it for an access token
            val code = receiver.waitForCode()
            if (code != null) {
                val response = flow.newTokenRequest(code).setRedirectUri(redirectUri).execute()
                // store credential and return it
                flow.createAndStoreCredential(response, userId)
            } else null
        } finally {
            log.info("stopping receiver")
            receiver.stop()
        }
    }

    override fun onAuthorization(authorizationUrl: AuthorizationCodeRequestUrl) {
        val url = authorizationUrl.build()
        println("Trying to open the following address in a web browser: \n${url}")
        try {
            find<GoogleAuthWebView>(params = mapOf(GoogleAuthWebView::url to url)).openModal(block = true)
        } finally {
            receiver.stop()
        }
    }
}
