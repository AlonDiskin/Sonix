package com.diskin.alon.sonix.util

import java.lang.reflect.Field

object WhiteBox {
    fun getInternalState(target: Any, field: String): Any {
        val c: Class<*> = target.javaClass
        return try {
            val f = getFieldFromHierarchy(c, field)
            f.isAccessible = true
            f[target]
        } catch (e: Exception) {
            throw RuntimeException(
                "Unable to get internal state on a private field. Please report to mockito mailing list.",
                e
            )
        }
    }

    fun setInternalState(target: Any, field: String, value: Any?) {
        val c: Class<*> = target.javaClass
        try {
            val f = getFieldFromHierarchy(c, field)
            f.isAccessible = true
            f[target] = value
        } catch (e: Exception) {
            throw RuntimeException(
                "Unable to set internal state on a private field. Please report to mockito mailing list.",
                e
            )
        }
    }

    private fun getFieldFromHierarchy(clazz: Class<*>, field: String): Field {
        var clazz = clazz
        var f = getField(clazz, field)
        while (f == null && clazz != Any::class.java) {
            clazz = clazz.superclass
            f = getField(clazz, field)
        }
        if (f == null) {
            throw RuntimeException(
                "You want me to get this field: '" + field +
                        "' on this class: '" + clazz.simpleName +
                        "' but this field is not declared within the hierarchy of this class!"
            )
        }
        return f
    }

    private fun getField(clazz: Class<*>, field: String): Field? {
        return try {
            clazz.getDeclaredField(field)
        } catch (e: NoSuchFieldException) {
            null
        }
    }
}