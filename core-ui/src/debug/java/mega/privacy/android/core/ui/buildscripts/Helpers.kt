package mega.privacy.android.core.ui.buildscripts

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import kotlin.reflect.KClass


internal fun createDataClass(
    name: String,
    properties: List<Triple<String, KClass<*>, String>>,
): TypeSpec {
    val dataClassBuilder = TypeSpec.classBuilder(name)
        .addModifiers(KModifier.DATA)
    val constructor = FunSpec.constructorBuilder()
    properties.forEach { (propName, propType, defaultValue) ->
        constructor.addParameter(
            ParameterSpec.builder(propName, propType)
                .defaultValue(defaultValue)
                .build()
        )
        dataClassBuilder.addProperty(
            PropertySpec.builder(propName, propType)
                .initializer(propName)
                .build()
        )
    }
    dataClassBuilder.addModifiers(KModifier.INTERNAL)
    dataClassBuilder.primaryConstructor(constructor.build())

    return dataClassBuilder.build()
}

/**
 * Returns a String with Kotlin idiomatic format:
 *  - remove spaces
 *  - remove "--" prefix
 *  - snake_case to camelCase
 *  - add an "n" prefix in case it does not starts with a letter
 */
internal fun String?.jsonNameToKotlinName(): String {
    if (this == null) return "Unknown"
    val pattern = "([_\\-])[a-z,0-9]".toRegex()
    val camelCase = this
        .removePrefix("--") //remove "--" prefixes
        .replace(" ", "_") //convert spaces to underscore
        .replace(pattern) { it.value.last().uppercase() } //snake_case to camelCase
        .trim()
    return if (camelCase.matches("^[^a-zA-Z].*".toRegex())) {
        "n$camelCase" //if is not starting with a letter (usually a number) add "n" prefix
    } else {
        camelCase
    }
}

internal fun String.lowercaseFirstChar(): String =
    this.replaceFirstChar { it.lowercase() }

internal fun String?.getPropertyName(typePrefix: String, groupParentName: String?): String =
    (this.jsonNameToKotlinName()
        .removePrefix(typePrefix)
        .removePrefix(typePrefix.lowercaseFirstChar())
        .removePrefix(groupParentName ?: "")
        .takeIf { it.isNotBlank() } ?: this.jsonNameToKotlinName()
            ).lowercaseFirstChar()
