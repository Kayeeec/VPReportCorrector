package org.umlreviewer.utils

import org.apache.commons.lang3.SystemUtils
import java.awt.Desktop
import java.io.File
import java.io.IOException
import java.net.URI
import java.util.ArrayList

/**
 * Custom class for working with the host desktop.
 * With some modifications, based on a class from an answer posted by MightyPork.
 * @see https://stackoverflow.com/a/18004334/7677851
 * @author MightyPork [https://stackoverflow.com/users/2180189/mightypork]
 */
object DesktopApi {
    fun browse(url: String): Boolean {
        val uri = URI(url)
        return browse(uri)
    }

    fun browse(uri: URI): Boolean {
        if (openSystemSpecific(uri.toString())) return true
        return browseDESKTOP(uri)
    }

    fun open(file: File): Boolean {
        if (openSystemSpecific(file.path)) return true
        return openDESKTOP(file)
    }

    fun edit(file: File): Boolean {
        if (openSystemSpecific(file.path)) return true
        return editDESKTOP(file)
    }

    private fun openSystemSpecific(what: String): Boolean {
        return when {
            SystemUtils.IS_OS_LINUX -> {
                when {
                    runCommand("kde-open", "%s", what) -> true
                    runCommand("gnome-open", "%s", what) -> true
                    runCommand("xdg-open", "%s", what) -> true
                    else -> false
                }
            }
            SystemUtils.IS_OS_MAC -> {
                runCommand("open", "%s", what)
            }
            SystemUtils.IS_OS_WINDOWS -> {
                runCommand("explorer", "%s", what)
            }
            else -> false
        }
    }



    private fun browseDESKTOP(uri: URI): Boolean {
        logOut("Trying to use Desktop.getDesktop().browse() with $uri")
        return try {
            if (!Desktop.isDesktopSupported()) {
                logErr("Platform is not supported.")
                return false
            }
            if (!Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                logErr("BROWSE is not supported.")
                return false
            }
            Desktop.getDesktop().browse(uri)
            true
        } catch (t: Throwable) {
            logErr("Error using desktop browse.", t)
            false
        }
    }


    private fun openDESKTOP(file: File): Boolean {
        logOut("Trying to use Desktop.getDesktop().open() with $file")
        return try {
            if (!Desktop.isDesktopSupported()) {
                logErr("Platform is not supported.")
                return false
            }
            if (!Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                logErr("OPEN is not supported.")
                return false
            }
            Desktop.getDesktop().open(file)
            true
        } catch (t: Throwable) {
            logErr("Error using desktop open.", t)
            false
        }
    }


    private fun editDESKTOP(file: File): Boolean {
        logOut("Trying to use Desktop.getDesktop().edit() with $file")
        return try {
            if (!Desktop.isDesktopSupported()) {
                logErr("Platform is not supported.")
                return false
            }
            if (!Desktop.getDesktop().isSupported(Desktop.Action.EDIT)) {
                logErr("EDIT is not supported.")
                return false
            }
            Desktop.getDesktop().edit(file)
            true
        } catch (t: Throwable) {
            logErr("Error using desktop edit.", t)
            false
        }
    }


    private fun runCommand(command: String, args: String, file: String): Boolean {
        logOut("Trying to exec:\n   cmd = $command\n   args = $args\n   %s = $file")
        val parts = prepareCommand(command, args, file)
        return try {
            val p = Runtime.getRuntime().exec(parts) ?: return false
            try {
                val retval = p.exitValue()
                if (retval == 0) {
                    logErr("Process ended immediately.")
                    false
                } else {
                    logErr("Process crashed.")
                    false
                }
            } catch (itse: IllegalThreadStateException) {
                logErr("Process is running.")
                true
            }
        } catch (e: IOException) {
            logErr("Error running command.", e)
            false
        }
    }

    private fun prepareCommand(command: String, args: String?, file: String): Array<String> {
        val parts: MutableList<String> = ArrayList()
        parts.add(command)
        if (args != null) {
            for (s in args.split(" ").toTypedArray()) {
                val str = String.format(s, file)
                parts.add(str.trim { it <= ' ' })
            }
        }
        return parts.toTypedArray()
    }

    private fun logErr(msg: String, t: Throwable) {
        System.err.println(msg)
        t.printStackTrace()
    }

    private fun logErr(msg: String) {
        System.err.println(msg)
    }

    private fun logOut(msg: String) {
        println(msg)
    }
}
