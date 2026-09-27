package net.neoforged.neoforge.fluids;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public final class FluidStack {
   public static final FluidStack EMPTY = new FluidStack(Fluids.EMPTY, 0);
   public static final Codec<FluidStack> OPTIONAL_CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            Identifier.CODEC
         .optionalFieldOf("fluid", BuiltInRegistries.FLUID.getKey(Fluids.EMPTY))
         .forGetter(stack -> BuiltInRegistries.FLUID.getKey(stack.fluid)),
            Codec.INT.optionalFieldOf("amount", 0).forGetter(FluidStack::getAmount)
         )
         .apply(instance, (id, amount) -> new FluidStack(BuiltInRegistries.FLUID.getValue(id), amount))
   );
   private final Fluid fluid;
   private int amount;

   public FluidStack(Fluid fluid, int amount) {
      this.fluid = fluid;
      this.amount = Math.max(0, amount);
   }

   public Fluid getFluid() {
      return this.fluid;
   }

   public int getAmount() {
      return this.amount;
   }

   public void setAmount(int amount) {
      this.amount = Math.max(0, amount);
   }

   public void limitSize(int maxAmount) {
      this.amount = Math.min(this.amount, Math.max(0, maxAmount));
   }

   public boolean isEmpty() {
      return this.fluid == Fluids.EMPTY || this.amount <= 0;
   }

   public FluidStack copy() {
      return new FluidStack(this.fluid, this.amount);
   }

   public FluidStack copyWithAmount(int newAmount) {
      return new FluidStack(this.fluid, newAmount);
   }

   public void grow(int amount) {
      this.amount = this.amount + Math.max(0, amount);
   }

   public void shrink(int amount) {
      this.amount = Math.max(0, this.amount - Math.max(0, amount));
   }

   public boolean isFluidEqual(FluidStack other) {
      return other != null && this.fluid == other.fluid;
   }

   public boolean is(Fluid fluid) {
      return this.fluid == fluid;
   }

   public boolean is(TagKey<Fluid> tag) {
      return !this.isEmpty() && this.fluid.builtInRegistryHolder().is(tag);
   }

   public Component getHoverName() {
      return Component.literal(BuiltInRegistries.FLUID.getKey(this.fluid).toString());
   }

   public static boolean isSameFluidSameComponents(FluidStack left, FluidStack right) {
      return left != null && right != null && left.fluid == right.fluid;
   }
}
