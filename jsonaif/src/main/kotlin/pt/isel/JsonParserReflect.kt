package pt.isel

import kotlin.reflect.*
import kotlin.reflect.full.*

object JsonParserReflect  : AbstractJsonParser() {

    /**
     * For each domain class we keep a Map<String, Setter> relating properties names with their setters.
     * This is for Part 2 of Jsonaif workout.
     */
    private val setters = mutableMapOf<KClass<*>, Map<String, Setter>>()

    override fun parsePrimitive(tokens: JsonTokens, klass: KClass<*>): Any? { // throw exception verify string "null"
        val prim = tokens.popWordPrimitive()
        val resp = basicParser[klass]
        return resp?.let { it(prim) }
    }

    private fun getPropsMap(klass : KClass<*>) : Map<String,Setter>{
        println(":: processing ${klass.simpleName} ::")
        val propList = klass.memberProperties.filter{
                prop -> prop.visibility == KVisibility.PUBLIC //&& prop
        }
        val map = mutableMapOf<String, Setter>()
        propList.forEach{prop ->
            val convertAnnotation = prop.findAnnotation<JsonConvert>()
            val propertyAnnotation = prop.findAnnotation<JsonProperty>()
            var converterFunction: KFunction<*>? = null
            var converterInstance: Any? = null
            var jsonType: KClass<*>? = null

            if(convertAnnotation != null) {
                converterFunction = convertAnnotation.klass.companionObject?.functions?.single{ it.name == "convert" }
                converterInstance = convertAnnotation.klass.companionObjectInstance
                jsonType = converterFunction?.parameters?.single{it.name == "date"}?.type?.classifier as KClass<*>
            }

            val setter = PropSetter(prop.returnType.classifier as KClass<*>,prop as KMutableProperty1<Any, Any?>, converterFunction, converterInstance, jsonType)
            if(propertyAnnotation != null) map[propertyAnnotation.aka] = setter
            map[prop.name] = setter
        }
        return map
    }

    private fun getParamsMapC(klass : KClass<*>) : Map<String,Setter>{
        println(":: processing ${klass.simpleName} ::")
        val paramList = klass.primaryConstructor?.parameters ?: throw Exception("unsupported type")
        val map = mutableMapOf<String, Setter>()
        paramList.forEach{param ->
            val convertAnnotation = param.findAnnotation<JsonConvert>()
            val propertyAnnotation = param.findAnnotation<JsonProperty>()
            var converterFunction: KFunction<*>? = null
            var converterInstance: Any? = null
            var jsonType: KClass<*>? = null

            if(convertAnnotation != null) {
                converterFunction = convertAnnotation.klass.companionObject?.functions?.single{ it.name == "convert" }
                converterInstance = convertAnnotation.klass.companionObjectInstance
                jsonType = converterFunction?.parameters?.single{it.name == "date"}?.type?.classifier as KClass<*>
            }

            val setter = ConstructorSetter(param.type.classifier as KClass<*>, param, converterFunction, converterInstance, jsonType)
            if(propertyAnnotation != null) map[propertyAnnotation.aka] = setter
            map[param.name ?: throw Exception("parameter name is null")] = setter
        }
        return map
    }

    override fun parseObject(tokens: JsonTokens, klass: KClass<*>) : Any {
        val params = klass.primaryConstructor?.parameters
        return if(params?.filter{ it.isOptional }?.size == params?.size ) parseObjectViaProperties(tokens, klass)
        else parseObjectViaConstructor(tokens, klass)
    }


   private fun parseObjectViaProperties(tokens: JsonTokens, klass: KClass<*>): Any {
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


    private fun parseObjectViaConstructor(tokens: JsonTokens, klass: KClass<*>) : Any {

        val constructor = klass.primaryConstructor ?: throw Exception("unsupported type")
        val map = mutableMapOf<KParameter,Any?>()
        val paramsMap = setters.computeIfAbsent(klass, ::getParamsMapC)
        tokens.pop(OBJECT_OPEN)
        tokens.trim()
        while (tokens.current != OBJECT_END) {
            val paramName = tokens.popWordFinishedWith(COLON).trim()
            val setter = paramsMap[paramName] as ConstructorSetter
            setter.apply(map,tokens)

            if (tokens.current == COMMA)
                tokens.pop(COMMA)
            else break
            tokens.trim()
        }
        tokens.pop(OBJECT_END)

        return constructor.callBy(map)
    }
}
