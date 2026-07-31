/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.compat.jei;

/** JEI-only view over special, server-owned process definitions. */
public record SpecialProcessRecipe(Kind kind, int amount) {
    public enum Kind { QUARTZ_CRUCIBLE, CHEMICAL_METAL_SEPARATOR }
}
