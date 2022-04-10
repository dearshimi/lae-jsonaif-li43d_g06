package pt.isel

import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.*

interface Setter {
    fun apply(target: Any, tokens: JsonTokens)
}

class PropSetter(val propKlass : KClass<*>, val prop : KProperty1<out Any, *>) : Setter {
    override fun apply(target: Any, tokens: JsonTokens) {
        var convertedTokens = tokens
        val annotation = prop.findAnnotation<JsonConvert>()
        if (annotation != null) {
            val function = annotation.klass.companionObject?.functions?.first()
            convertedTokens = function?.call(annotation.klass.companionObjectInstance, tokens) as JsonTokens
        }
        val v = JsonParserReflect.parse(convertedTokens, propKlass)
        (prop as KMutableProperty1<Any, Any?>).set(target,v)
    }
}

class ConstructorSetter(val klass : KClass<*>, val paramKlass : KClass<*>, val param : KParameter) : Setter {
    override fun apply(target: Any, tokens: JsonTokens) {
        var convertedTokens = tokens
        val annotation = klass.memberProperties.find{ it.name == param.name }?.findAnnotation<JsonConvert>()
        if( annotation != null){
            val function = annotation.klass.companionObject?.functions?.first()
            convertedTokens = function?.call(annotation.klass.companionObjectInstance, tokens) as JsonTokens
        }

        val v = JsonParserReflect.parse(convertedTokens, paramKlass)
        val map = target as MutableMap<KParameter,Any?>
        map[param] = v
    }
}