package pt.isel

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KParameter

interface Setter {
    fun apply(target: Any, tokens: JsonTokens)
}

class PropSetter(val propKlass: KClass<*>, val prop: KMutableProperty1<Any, Any?>, val function: KFunction<*>?, val instance: Any?, val jsonType: KClass<*>?) : Setter {
    override fun apply(target: Any, tokens: JsonTokens) {
        val toObj: Any?
        if(function != null && jsonType != null){
            val parsedTokens = JsonParserReflect.parse(tokens, jsonType)
            toObj = function.call(instance, parsedTokens)
        }
        else
            toObj = JsonParserReflect.parse(tokens, propKlass)

        prop.set(target,toObj)
    }
}

class ConstructorSetter(val paramKlass: KClass<*>, val param: KParameter, val function: KFunction<*>?, val instance: Any?, val jsonType: KClass<*>?) : Setter {
    override fun apply(target: Any, tokens: JsonTokens) {
        val toObj: Any?
        if(function != null && jsonType != null){
            val parsedTokens = JsonParserReflect.parse(tokens, jsonType)
            toObj = function.call(instance, parsedTokens)
        }
        else
            toObj = JsonParserReflect.parse(tokens, paramKlass)

        val map = target as MutableMap<KParameter,Any?>
        map[param] = toObj
    }
}
