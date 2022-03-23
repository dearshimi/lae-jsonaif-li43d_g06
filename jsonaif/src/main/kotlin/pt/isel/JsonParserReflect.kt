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
        val prim = tokens.popWordPrimitive() // getting the primitive value
        val resp = basicParser[klass] // applying the parser that will give us the function necessary to cast the primitive from a string to what we desire
        return resp?.let { it(prim) } // apply the function existent in basicParser key association and returning its value
    }

    override fun parseObject(tokens: JsonTokens, klass: KClass<*>): Any? {
        val obj = klass.createInstance()
        tokens.pop(OBJECT_OPEN) // remove the opening of the json object
        tokens.trim() // remove the withe spaces
        while (tokens.current != OBJECT_END) {
            val propName = tokens.popWordFinishedWith(COLON).trim() // receive the value of the prop in evaluation and remove the spaces after her
            val prop = klass.memberProperties.first{ it.name == propName } //trying to get the first existent property whose specifications in the predicate are true
            val propKlass = prop.returnType.classifier as KClass<*> //getting the KClass of the property found
            val v = parse(tokens, propKlass) // applying the parse to the part of the token found( most likely a primitive)
            (prop as KMutableProperty1<Any, Any?>).set(obj,v) // update of the obj( which is an instance of our Klass). Addition of the property we have just found and its value
            if (tokens.current == COMMA) // if there are more properties we want to remove the comma
                tokens.pop(COMMA)
            else break
            tokens.trim() // remove the white spaces between properties
        }
        tokens.pop(OBJECT_END)
        return obj
    }
}
