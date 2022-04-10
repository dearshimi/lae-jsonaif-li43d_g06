package pt.isel

import kotlin.reflect.KClass

@Target(AnnotationTarget.PROPERTY)
annotation class JsonConvert(val klass : KClass<*>){
}