package com.autonomousapps.internal.advice.filter

import com.autonomousapps.advice.ComponentWithTransitives
import com.autonomousapps.advice.HasDependency
import com.autonomousapps.advice.TransitiveDependency

internal class LogicalDependencyFilter(
  private val map: Map<String, Set<Regex>>
) : DependencyFilter {

  override val predicate: (HasDependency) -> Boolean = {
    if (map.isEmpty()) {
      // Exit early if we have no logical dependencies
      true
    } else {
      val isLogical = when (it) {
        is TransitiveDependency -> computeTransitive(it)
        is ComponentWithTransitives -> computeDirect(it)
        else -> error("This filter expects a TransitiveDependency or a ComponentWithTransitives")
      }
      // Return true if the dependency doesn't match any of the logical dependencies the user has
      // set
      !isLogical
    }
  }

  /**
   * Return true if [dep] has a parent in the same logical dependency group.
   */
  private fun computeTransitive(dep: TransitiveDependency): Boolean {
    // Find groups that `dep` is a member of
    val regexGroups = map.values
    val groups = regexGroups.filter { regexes ->
      regexes.any { regex ->
        regex.matches(dep.dependency.identifier)
      }
    }
    // Now look for parents in one of these groups, returning true if there is a match
    return groups.any { regexes ->
      regexes.any { regex ->
        dep.parents.any { parent ->
          regex.matches(parent.identifier)
        }
      }
    }
  }

  private fun computeDirect(dep: ComponentWithTransitives): Boolean {
    // Find groups that `dep` is a member of
    val regexGroups = map.values
    val groups = regexGroups.filter { regexes ->
      regexes.any { regex ->
        regex.matches(dep.dependency.identifier)
      }
    }
    // Now look for transitives in one of these groups, returning true if there is a match
    return groups.any { regexes ->
      regexes.any { regex ->
        dep.usedTransitiveDependencies.any { usedTransitive ->
          regex.matches(usedTransitive.identifier)
        }
      }
    }
  }
}