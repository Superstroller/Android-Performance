package com.supertramp.performance

import java.lang.IllegalArgumentException
import java.lang.reflect.Field
import java.lang.reflect.Method

object ReflectUtil {

    @Throws(NoSuchFieldException::class, ClassNotFoundException::class)
    fun getDeclaredFieldRecursive(clazz: Any, fieldName: String?): Field {
        var realClazz: Class<*>? = null
        realClazz = when (clazz) {
            is String -> {
                Class.forName(clazz)
            }
            is Class<*> -> {
                clazz
            }
            else -> {
                throw IllegalArgumentException("Illegal clazz type: " + clazz.javaClass)
            }
        }
        var currClazz = realClazz
        while (true) {
            try {
                val field = currClazz!!.getDeclaredField(fieldName)
                field.isAccessible = true
                return field
            } catch (e: NoSuchFieldException) {
                if (currClazz == Any::class.java) {
                    throw e
                }
                currClazz = currClazz!!.superclass
            }
        }
    }

    @Throws(NoSuchMethodException::class, ClassNotFoundException::class)
    fun getDeclaredMethodRecursive(
        clazz: Any,
        methodName: String?,
        vararg argTypes: Class<*>?
    ): Method {
        var realClazz: Class<*>? = null
        realClazz = when (clazz) {
            is String -> {
                Class.forName(clazz)
            }
            is Class<*> -> {
                clazz
            }
            else -> {
                throw IllegalArgumentException("Illegal clazz type: " + clazz.javaClass)
            }
        }
        var currClazz = realClazz
        while (true) {
            try {
                val method = currClazz!!.getDeclaredMethod(methodName)
                method.isAccessible = true
                return method
            } catch (e: NoSuchMethodException) {
                if (currClazz == Any::class.java) {
                    throw e
                }
                currClazz = currClazz!!.superclass
            }
        }
    }

}