@file:Suppress("UnstableApiUsage")

package com.autonomousapps.extension

import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Named
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.setProperty
import org.intellij.lang.annotations.Language
import javax.inject.Inject

/**
 * ```
 * dependencyAnalysis {
 *   logicalDependencies {
 *     group("kotlin-stdlib") {
 *       // 1: include all in group as a single logical dependency
 *       includeGroup("org.jetbrains.kotlin")
 *
 *       // 2: include all supplied dependencies as a single logical dependency
 *       includeDependency("org.jetbrains.kotlin:kotlin-stdlib")
 *       includeDependency("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
 *
 *       // 3: include all dependencies that match the regex as a single logical dependency
 *       include(".*kotlin-stdlib.*")
 *     }
 *   }
 * }
 * ```
 */
open class LogicalDependenciesHandler @Inject constructor(private val objects: ObjectFactory) {

  @get:Nested
  val groups = objects.domainObjectContainer(GroupHandler::class.java)

//  fun groups(): MapProperty<String, Set<Regex>> {
//    val mapProperty = objects.mapProperty<String, Set<Regex>>()
//    groups.all {
//      val t = includes.map {
//        name to it
//      }
//      mapProperty.put(t.map { it.first }, t.map { it.second })
//    }
//  }

//  fun groups(): MapProperty<String, Set<Regex>> {
//    val map = objects.mapP//mutableMapOf<String, Set<Regex>>()
//    groups.all {
//      includes.map {
//        map.put(name, it)
//      }
//    }
//    return map
//  }

  fun group(name: String, action: Action<GroupHandler>) {
    try {
      groups.create(name) {
        action.execute(this)
      }
    } catch (e: GradleException) {
      throw wrapException(e)
    }
  }

  private fun wrapException(e: GradleException) = if (e is InvalidUserDataException)
    GradleException("You must configure this project either at the root or the project level, not both", e)
  else e
}

/**
 * ```
 * group("kotlin-stdlib") {
 *   // 1: include all in group as a single logical dependency
 *   includeGroup("org.jetbrains.kotlin")
 *
 *   // 2: include all supplied dependencies as a single logical dependency
 *   includeDependency("org.jetbrains.kotlin:kotlin-stdlib")
 *   includeDependency("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
 *
 *   // 3: include all dependencies that match the regex as a single logical dependency
 *   include(".*kotlin-stdlib.*")
 * }
 * ```
 */
open class GroupHandler @Inject constructor(
  private val name: String,
  objects: ObjectFactory
) : Named {

  override fun getName(): String = name

  @get:Input
  val includes = objects.setProperty<Regex>().convention(emptySet())

  internal fun anyMatch(input: CharSequence): Boolean {
    return includes.get().any { it.matches(input) }
  }

  fun includeGroup(group: String) {
    include("^$group:.*")
  }

  fun includeDependency(identifier: String) {
    include("^$identifier\$")
  }

  fun include(@Language("RegExp") regex: String) {
    include(regex.toRegex())
  }

  fun include(regex: Regex) {
    includes.add(regex)
  }
}
