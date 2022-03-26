package pt.isel

import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

interface Setter {
    fun apply(target: Any, tokens: JsonTokens)
}

class PropSetter(val propKlass : KClass<*>, val prop : KProperty1<out Any, *>) : Setter {
    override fun apply(target: Any, tokens: JsonTokens) {
        val v = JsonParserReflect.parse(tokens, propKlass)
        (prop as KMutableProperty1<Any, Any?>).set(target,v)
    }
}