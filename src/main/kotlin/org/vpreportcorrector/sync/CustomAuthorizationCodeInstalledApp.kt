package org.vpreportcorrector.sync

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver
import org.vpreportcorrector.utils.DesktopApi
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

    companion object {
        /**
         * Opens a browser at the given URL string or alternatively throws an error containing
         * the URL so the user can use it manually.
         * Used custom implementation because java.awt.Desktop way is not working on some linux systems.
         */
        fun browse(url: String) {
            println("Trying to open the following address in a web browser: \n{url}")
            try {
                DesktopApi.browse(url)
            } catch (e: Throwable) {
                throw OpenBrowserException("Failed to open url in default browser - url:\n${url}", url, e)
            }
        }
    }

    override fun onAuthorization(authorizationUrl: AuthorizationCodeRequestUrl) {
        browse(authorizationUrl.build())
    }
}

class OpenBrowserException(message: String, url: String, throwable: Throwable): Exception(message, throwable)
