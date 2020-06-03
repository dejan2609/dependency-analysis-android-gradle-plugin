package com.autonomousapps.extension

import com.google.common.truth.Truth.assertThat
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class GroupHandlerTest {

  private val project = ProjectBuilder.builder().build()
  private val objects = project.objects
  private val groupHandler = GroupHandler("test", objects)

  @Test fun includeGroup() {
    // I like this better, but the IDE does not comprehend it and I can't stand the red squigglies.
//    val handler = LogicalDependenciesHandler(objects)
//    handler.group("test") {
//      includeGroup("org.jetbrains.kotlin")
//    }
//    val groupHandler = handler.groups.getAt("test")
    groupHandler.includeGroup("org.jetbrains.kotlin")

    assertThat(groupHandler.anyMatch("org.jetbrains.kotlin:kotlin-stdlib")).isTrue()
    assertThat(groupHandler.anyMatch("org.jetbrains.kotlin:kotlin-stdlib-jdk8")).isTrue()
    assertThat(groupHandler.anyMatch("org.jetbrains.kotlin:kotlin-stdlib-jdk7")).isTrue()
    assertThat(groupHandler.anyMatch("org.jetbrains:kotlin-stdlib")).isFalse()
  }

  @Test fun includeDependency() {
    groupHandler.includeDependency("org.jetbrains.kotlin:kotlin-stdlib")
    groupHandler.includeDependency("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    assertThat(groupHandler.anyMatch("org.jetbrains.kotlin:kotlin-stdlib")).isTrue()
    assertThat(groupHandler.anyMatch("org.jetbrains.kotlin:kotlin-stdlib-jdk8")).isTrue()
    assertThat(groupHandler.anyMatch("org.jetbrains.kotlin:kotlin-stdlib-jdk7")).isFalse()
    assertThat(groupHandler.anyMatch("org.jetbrains.kotlinx:kotlinx-coroutines-core")).isFalse()
  }

  @Test fun `include by string`() {
    groupHandler.include("org.jetbrains.*")

    assertThat(groupHandler.anyMatch("org.jetbrains.kotlin:kotlin-stdlib")).isTrue()
    assertThat(groupHandler.anyMatch("org.jetbrains.kotlin:kotlin-stdlib-jdk8")).isTrue()
    assertThat(groupHandler.anyMatch("org.jetbrains.kotlin:kotlin-stdlib-jdk7")).isTrue()
    assertThat(groupHandler.anyMatch("org.jetbrains.kotlinx:kotlinx-coroutines-core")).isTrue()
    assertThat(groupHandler.anyMatch("com.some:thing")).isFalse()
  }

  @Test fun `include by regex`() {
    groupHandler.include("org.jetbrains.*".toRegex())

    assertThat(groupHandler.anyMatch("org.jetbrains.kotlin:kotlin-stdlib")).isTrue()
    assertThat(groupHandler.anyMatch("org.jetbrains.kotlin:kotlin-stdlib-jdk8")).isTrue()
    assertThat(groupHandler.anyMatch("org.jetbrains.kotlin:kotlin-stdlib-jdk7")).isTrue()
    assertThat(groupHandler.anyMatch("org.jetbrains.kotlinx:kotlinx-coroutines-core")).isTrue()
    assertThat(groupHandler.anyMatch("com.some:thing")).isFalse()
  }
}
