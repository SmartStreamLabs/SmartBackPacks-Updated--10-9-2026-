package net.neoforged.neoforge.energy;

public interface IEnergyStorage {
   int receiveEnergy(int var1, boolean var2);

   int extractEnergy(int var1, boolean var2);

   int getEnergyStored();

   int getMaxEnergyStored();

   boolean canExtract();

   boolean canReceive();
}
