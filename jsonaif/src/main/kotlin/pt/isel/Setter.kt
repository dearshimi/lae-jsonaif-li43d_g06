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
       /* var convertedTokens = tokens
        if (function != null)
            convertedTokens = function.call(instance, tokens) as JsonTokens

        val v = JsonParserReflect.parse(convertedTokens, propKlass)
        prop.set(target,v)*/
        var toObj: Any?
        if(function != null){
            val parsedTokens = JsonParserReflect.parse(tokens, jsonType ?: throw Exception ("Type is null"))  // passa os jsonTokens para string
            toObj = function?.call(instance, parsedTokens)   // passa a string para tipo date
        }
        else
            toObj = JsonParserReflect.parse(tokens, propKlass)

        prop.set(target,toObj)
    }
}

class ConstructorSetter(val paramKlass: KClass<*>, val param: KParameter, val function: KFunction<*>?, val instance: Any?, val jsonType: KClass<*>?) : Setter {
    override fun apply(target: Any, tokens: JsonTokens) {
        /*var convertedTokens = tokens
        if(function != null)
            convertedTokens = function.call(instance, tokens) as JsonTokens

        val v = JsonParserReflect.parse(convertedTokens, paramKlass)
        val map = target as MutableMap<KParameter,Any?>
        map[param] = v*/

        var toObj: Any?
        if(function != null){
            val parsedTokens = JsonParserReflect.parse(tokens, jsonType ?: throw Exception ("Type is null"))  // passa os jsonTokens para string
            toObj = function?.call(instance, parsedTokens)   // passa a string para tipo date
        }
        else
            toObj = JsonParserReflect.parse(tokens, paramKlass)

        val map = target as MutableMap<KParameter,Any?>
        map[param] = toObj

    }
}


class TestSetter(val jsonType: KClass<*>, val prop: KMutableProperty1<Any, Any?>, val function: KFunction<*>?, val instance: Any?) : Setter {
    override fun apply(target: Any, tokens: JsonTokens) {

        val v = JsonParserReflect.parse(tokens, jsonType)  // passa os jsonTokens para string
        val cv = function?.call(instance, v)   // passa a string para tipo date

        prop.set(target,cv)
    }
}