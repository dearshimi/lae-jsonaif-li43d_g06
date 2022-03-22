package pt.isel

import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
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

    override fun parseObject(tokens: JsonTokens, klass: KClass<*>): Any? {
        val obj = klass.createInstance()
        tokens.pop(OBJECT_OPEN)
        tokens.trim()
        while (tokens.current != OBJECT_END) {
            val propName = tokens.popWordFinishedWith(COLON).trim()
            val prop = klass.memberProperties.first{ it.name == propName }
            val propKlass = prop.returnType.classifier as KClass<*>
            val v = parse(tokens, propKlass)
            (prop as KMutableProperty1<Any, Any?>).set(obj,v)
            if (tokens.current == COMMA)
                tokens.pop(COMMA)
            else break
            tokens.trim()
        }
        tokens.pop(OBJECT_END)
        return obj
    }
}
