/*
 * This file is part of TechReborn, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2024 TechReborn
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

package techreborn.datagen.recipes.machine.fusion_reactor

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.minecraft.registry.RegistryWrapper
import techreborn.datagen.recipes.TechRebornRecipesProvider
import techreborn.init.ModFluids
import techreborn.init.TRContent

import java.util.concurrent.CompletableFuture

class FusionReactorRecipesProvider extends TechRebornRecipesProvider {
	FusionReactorRecipesProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
		super(output, registriesFuture)
	}

	@Override
	void generateRecipes() {
		offerFusionReactorRecipe {
			power 16384
			time 2048
			startEnergy 40000000
			minimumSize 1
			ingredients cellStack(ModFluids.TRITIUM), cellStack(ModFluids.DEUTERIUM)
			outputs cellStack(ModFluids.HELIUM3)
		}
		offerFusionReactorRecipe {
			power 16384
			time 2048
			startEnergy 40000000
			minimumSize 1
			ingredients cellStack(ModFluids.HELIUM3), cellStack(ModFluids.DEUTERIUM)
			outputs cellStack(ModFluids.HELIUMPLASMA)
		}
		offerFusionReactorRecipe {
			power (-2048)
			time 1024
			startEnergy 90000000
			minimumSize 1
			ingredients cellStack(ModFluids.WOLFRAMIUM), cellStack(ModFluids.LITHIUM)
			outputs TRContent.Ores.IRIDIUM
		}
		offerFusionReactorRecipe {
			power (-2048)
			time 1024
			startEnergy 80000000
			minimumSize 1
			ingredients cellStack(ModFluids.WOLFRAMIUM), cellStack(ModFluids.BERYLLIUM)
			outputs TRContent.Dusts.PLATINUM
		}
	}
}
