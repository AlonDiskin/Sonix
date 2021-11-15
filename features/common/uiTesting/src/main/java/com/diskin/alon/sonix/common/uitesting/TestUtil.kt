package com.diskin.alon.sonix.common.uitesting

import java.io.File
import java.lang.reflect.Field
import java.lang.reflect.Modifier

fun getJsonFromResource(resource: String): String {
    val topLevelClass = object : Any() {}.javaClass.enclosingClass!!
    val jsonResource = topLevelClass.classLoader!! // javaClass.classLoader
        .getResource(resource)

    return File(jsonResource.toURI()).readText()
}

fun setFinalStatic(field: Field, newValue: Any?) {
    field.isAccessible = true
    val modifiersField: Field = try {
        Field::class.java.getDeclaredField("accessFlags")
    } catch (e: NoSuchFieldException) {
        //This is an emulator JVM  ¯\_(ツ)_/¯
        Field::class.java.getDeclaredField("modifiers")
    }
    modifiersField.isAccessible = true
    modifiersField.setInt(field, field.modifiers and Modifier.FINAL.inv())
    field.set(null, newValue)
}