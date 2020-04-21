package ru.skillbranch.skillarticles.data.delegates

import ru.skillbranch.skillarticles.data.local.PrefManager
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
class PrefDelegate<T>(private val defaultValue: T) : ReadWriteProperty<PrefManager, T?> {
    private var value: T? = null

    override fun getValue(thisRef: PrefManager, property: KProperty<*>): T? {
        if (value == null) {
            val key = property.name
            value = when (defaultValue) {
                is Boolean -> thisRef.preferences.getBoolean(key, defaultValue) as? T
                is String -> thisRef.preferences.getString(key, defaultValue) as? T
                is Float -> thisRef.preferences.getFloat(key, defaultValue) as? T
                is Int -> thisRef.preferences.getInt(key, defaultValue) as? T
                is Long -> thisRef.preferences.getLong(key, defaultValue) as? T
                else -> throw IllegalArgumentException("Value must be primitive type")
            }
        }
        return value
    }

    override fun setValue(thisRef: PrefManager, property: KProperty<*>, value: T?) {
        thisRef.preferences.edit().run {
            val key = property.name
            when (value) {
                is Boolean -> putBoolean(key, value)
                is String -> putString(key, value)
                is Float -> putFloat(key, value)
                is Int -> putInt(key, value)
                is Long -> putLong(key, value)
                else -> throw IllegalArgumentException("Value must be primitive type")
            }
            apply()
        }
        this.value = value
    }
}