package pt.isel

import kotlin.reflect.*
import kotlin.reflect.full.*

object JsonParserReflect  : AbstractJsonParser() {

    /**
     * For each domain class we keep a Map<String, Setter> relating properties names with their setters.
     * This is for Part 2 of Jsonaif workout.
     */
    private val setters = mutableMapOf<KClass<*>, Map<String, Setter>>()

    override fun parsePrimitive(tokens: JsonTokens, klass: KClass<*>): Any? {
        val prim = tokens.popWordPrimitive()
        val resp = basicParser[klass]
        return resp?.let { it(prim) }
    }

    private fun getPropsMap(klass : KClass<*>) : Map<String,Setter>{ // student
        println(":: processing ${klass.simpleName} ::")
        val propList = klass.memberProperties.filter{
                prop -> prop.visibility == KVisibility.PUBLIC
        }
        val map = mutableMapOf<String, Setter>()
        propList.forEach{prop ->
            val findAn = prop.findAnnotation<JsonProperty>()
            if(findAn != null) map[findAn.aka] = PropSetter(prop.returnType.classifier as KClass<*>,prop as KMutableProperty1<Any, Any?>)
            map[prop.name] = PropSetter(prop.returnType.classifier as KClass<*>, prop as KMutableProperty1<Any, Any?>)
        }
        return map
    }

    private fun getPropsMapC(klass : KClass<*>) : Map<String,Setter>{
        println(":: processing ${klass.simpleName} ::")
        val paramList = klass.primaryConstructor?.parameters ?: throw Exception("unsoported type")
        val map = mutableMapOf<String, Setter>()
        paramList.forEach{param ->
        val findAn = param.findAnnotation<JsonProperty>() //klass.memberProperties.find{it.name==param.name}
        if(findAn != null) map[findAn.aka] = ConstructorSetter(klass,param.type.classifier as KClass<*>,param )
        map[param.name!!] = ConstructorSetter(klass,param.type.classifier as KClass<*>, param)
        }
        return map
    }

    override fun parseObject(tokens: JsonTokens, klass: KClass<*>) : Any {
        val params = klass.primaryConstructor?.parameters
        return if(params?.filter{ it.isOptional }?.size == params?.size ) parseObjectP(tokens, klass)
        else parseObjectC(tokens, klass)
    }


   private fun parseObjectP(tokens: JsonTokens, klass: KClass<*>): Any {
        val obj = klass.createInstance()
        val propsMap = setters.computeIfAbsent(klass, ::getPropsMap)

        tokens.pop(OBJECT_OPEN)
        tokens.trim()

        while (tokens.current != OBJECT_END) {
            val propName = tokens.popWordFinishedWith(COLON).trim()
            val setter = propsMap[propName] as PropSetter
            setter.apply(obj, tokens)
            if (tokens.current == COMMA)
                tokens.pop(COMMA)
            else break
            tokens.trim()
        }
        tokens.pop(OBJECT_END)
        return obj
    }


    private fun parseObjectC(tokens: JsonTokens, klass: KClass<*>) : Any {

        val constructor = klass.primaryConstructor
        val map = mutableMapOf<KParameter,Any?>()
        val propsMap = setters.computeIfAbsent(klass, ::getPropsMapC)
        tokens.pop(OBJECT_OPEN)
        tokens.trim()
        while (tokens.current != OBJECT_END) {
            val paramName = tokens.popWordFinishedWith(COLON).trim()
            val setter = propsMap[paramName] as ConstructorSetter
            setter.apply(map,tokens)

            if (tokens.current == COMMA)
                tokens.pop(COMMA)
            else break
            tokens.trim()
        }
        tokens.pop(OBJECT_END)

        return constructor!!.callBy(map)
    }
}
