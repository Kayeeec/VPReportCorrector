package org.vpreportcorrector.utils

import java.lang.IllegalArgumentException

/**
 * Returns the enum constant of this type with the specified name. The string must match exactly an identifier
 * used to declare an enum constant in this type. (Extraneous whitespace characters are not permitted.)
 *
 * @return enum constant of this type specified by [name] or null if this enum has no constant with the specified name
 */
inline fun <reified T : Enum<*>> enumValueOrNull(name: String): T? {
    return try {
        enumValueOf<T>(name)
    } catch (e: IllegalArgumentException) {
        return null
    }
}
