package pt.isel

import kotlin.reflect.*
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.memberProperties

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


    private fun getPropsMap(klass : KClass<*>) : Map<String,Setter>{
        println(":: processing ${klass.simpleName} ::")
        val propList = klass.memberProperties.filter{
                prop -> prop.visibility == KVisibility.PUBLIC
        }
        return propList.associateBy({it.name},{PropSetter(it.returnType.classifier as KClass<*>, it)})
    }



    override fun parseObject(tokens: JsonTokens, klass: KClass<*>): Any {
        val obj = klass.createInstance()
        val propsMap = setters.computeIfAbsent(klass, ::getPropsMap)

        tokens.pop(OBJECT_OPEN)
        tokens.trim()

        while (tokens.current != OBJECT_END) {
            val propName = tokens.popWordFinishedWith(COLON).trim()

            val propSetter = propsMap[propName] as PropSetter
            val propKlass = propSetter.propKlass
            propSetter.apply(obj, tokens)
            if (tokens.current == COMMA)
                tokens.pop(COMMA)
            else break
            tokens.trim()
        }
        tokens.pop(OBJECT_END)
        return obj
    }
}