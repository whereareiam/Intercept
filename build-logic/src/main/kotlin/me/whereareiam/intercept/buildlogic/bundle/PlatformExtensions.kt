package me.whereareiam.intercept.buildlogic.bundle

import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import javax.inject.Inject

abstract class PlatformCollectionExtension @Inject constructor(
        objects: ObjectFactory
) {
    val memberPaths: SetProperty<String> = objects.setProperty(String::class.java)
        .convention(emptySet())
}

abstract class PlatformExtension @Inject constructor(
        objects: ObjectFactory
) {
    val name: Property<String> = objects.property(String::class.java)
    val bundled: Property<Boolean> = objects.property(Boolean::class.java)
        .convention(true)
    val descriptors: ListProperty<String> = objects.listProperty(String::class.java)
        .convention(emptyList())
}

internal fun Project.platformCollection(): PlatformCollectionExtension {
    rootProject.extensions.findByType(PlatformCollectionExtension::class.java)?.let { collection ->
        return collection
    }

    return rootProject.extensions.create("platformCollection", PlatformCollectionExtension::class.java)
}

internal fun Project.platform(): PlatformExtension {
    extensions.findByType(PlatformExtension::class.java)?.let { extension ->
        return extension
    }

    return extensions.create("platform", PlatformExtension::class.java)
}

internal fun Project.bundlePlatformId(): String =
    platform().name.orNull?.takeIf(String::isNotBlank)
        ?: name.uppercase()

internal fun Project.bundleDescriptors(): List<String> =
    platform().descriptors.orNull.orEmpty()
