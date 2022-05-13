package pt.isel

import kotlin.reflect.*
import kotlin.reflect.full.*

object JsonParserDynamic  : AbstractJsonParser() {

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

            //vai ser substituido pelo java poet
            val setter = PropSetter(prop.returnType.classifier as KClass<*>,prop as KMutableProperty1<Any, Any?>, converterFunction, converterInstance, jsonType)

            val txt = "Set" + klass.simpleName + "_" + prop.name
            val javaCode = generateCode(txt, klass, prop /* preciso mais argumentos ?? */)
            if(propertyAnnotation != null) map[propertyAnnotation.aka] = setter
            map[prop.name] = setter
        }
        return map
    }

    private fun generateCode(txt: String, klass: KClass<*>, prop: KProperty1<out Any, *>): Any = TODO()


    
    override fun parseObject(tokens: JsonTokens, klass: KClass<*>): Any {
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
}
