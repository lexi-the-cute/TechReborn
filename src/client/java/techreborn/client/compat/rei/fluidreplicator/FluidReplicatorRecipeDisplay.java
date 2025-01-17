/*
 * This file is part of TechReborn, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2020 TechReborn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package techreborn.client.compat.rei.fluidreplicator;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import reborncore.common.fluid.container.FluidInstance;
import techreborn.recipe.recipes.FluidReplicatorRecipe;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author drcrazy
 */
public class FluidReplicatorRecipeDisplay implements Display {

	private final FluidReplicatorRecipe recipe;
	private final Identifier recipeId;
	private final List<EntryIngredient> inputs;
	private final List<EntryIngredient> output;
	private final FluidInstance fluidInstance;
	private final int energy;
	private final int time;

	public FluidReplicatorRecipeDisplay(RecipeEntry<FluidReplicatorRecipe> recipe) {
		this.recipe = recipe.value();
		this.recipeId = recipe.id();
		this.inputs = CollectionUtils.map(this.recipe.ingredients(), ing -> EntryIngredients.ofItemStacks(ing.getPreviewStacks()));
		this.fluidInstance = this.recipe.fluid();
		this.output = fluidInstance == null ? Collections.emptyList() : Collections.singletonList(EntryIngredients.of(fluidInstance.fluid(), fluidInstance.getAmount().getRawValue()));
		this.energy = this.recipe.power();
		this.time = this.recipe.time();
	}

	public FluidInstance getFluidInstance() {
		return fluidInstance;
	}

	public int getEnergy() {
		return energy;
	}

	public int getTime() {
		return time;
	}

	@Override
	public List<EntryIngredient> getInputEntries() {
		return inputs;
	}

	@Override
	public List<EntryIngredient> getOutputEntries() {
		return output;
	}

	@Override
	public CategoryIdentifier<?> getCategoryIdentifier() {
		return CategoryIdentifier.of(Objects.requireNonNull(Registries.RECIPE_TYPE.getId(recipe.getType())));
	}

	@Override
	public Optional<Identifier> getDisplayLocation() {
		return Optional.of(recipeId);
	}
}
