package org.web25.felix.documents

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.cast

fun <T: Any> singleton(threadLocal: Boolean = false, creator: (() -> T)? = null): ReadOnlyProperty<Any?, T> {
    return SingletonDelegate(threadLocal, creator)
}

class SingletonDelegate<out T: Any>(val threadLocal: Boolean, val creator: (() -> T)?) : ReadOnlyProperty<Any?, T> {


    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if(threadLocal) {
            return SingletonHolder.getThreadLocalInstanceOf(property.returnType, creator)
        } else {
            return SingletonHolder.getInstanceOf(property.returnType, creator)
        }
    }

}

private object SingletonHolder {

    private val map = mutableMapOf<KType, Any>()

    private val threadLocalMap = object : ThreadLocal<MutableMap<KType, Any>>() {
        override fun initialValue(): MutableMap<KType, Any> {
            return mutableMapOf()
        }
    }

    fun <T: Any> getInstanceOf(returnType: KType, creator: (() -> T)?): T {
        if(map.containsKey(returnType)) {
            val value = map[returnType]
            val classifier = returnType.classifier
            if(value != null) {
                if (classifier != null && (classifier as KClass<*>).isInstance(value)) {
                    return classifier.cast(value) as T
                } else {
                    throw RuntimeException("Invalid type of ${value::class.qualifiedName} for $returnType")
                }
            } else {
                throw RuntimeException("Value is null")
            }
        } else {
            try {
                if(creator == null) {
                    throw RuntimeException("No object found and no creator supplied!")
                }
                val created = creator()
                map[returnType] = created
                return created
            } catch (t: Throwable) {
                t.printStackTrace()
                throw t
            }
        }
    }

    fun <T: Any> getThreadLocalInstanceOf(returnType: KType, creator: (() -> T)?): T {
        val map = threadLocalMap.get()
        if(map.containsKey(returnType)) {
            val value = map[returnType]
            val classifier = returnType.classifier
            if(value != null) {
                if (classifier != null && (classifier as KClass<*>).isInstance(value)) {
                    return classifier.cast(value) as T
                } else {
                    throw RuntimeException("Invalid type of ${value::class.qualifiedName} for $returnType")
                }
            } else {
                throw RuntimeException("Value is null")
            }
        } else {
            try {
                if(creator == null) {
                    throw RuntimeException("No object found and no creator supplied!")
                }
                val created = creator()
                map[returnType] = created
                return created
            } catch (t: Throwable) {
                t.printStackTrace()
                throw t
            }
        }
    }

}