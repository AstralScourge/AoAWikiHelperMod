package net.tslat.aoawikihelpermod.loottables;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.util.List;

public class CommandPrintLootTable extends CommandBase {
	@Override
	public String getName() {
		return "printloottable";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/PrintLootTable <Loot Table Path>";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		World world = sender.getEntityWorld();

		if (!world.isRemote) {
			if (args.length == 0) {
				sender.sendMessage(new TextComponentString("Must provide loot table resource path for printout"));
				sender.sendMessage(new TextComponentString("E.G. " + TextFormatting.GOLD + "aoa3:entities/mobs/overworld/charger"));

				return;
			}

			LootTable table = world.getLootTableManager().getLootTableFromLocation(new ResourceLocation(args[0]));

			if (table == LootTable.EMPTY_LOOT_TABLE) {
				sender.sendMessage(new TextComponentString(TextFormatting.RED + "Unable to find loot table: " + args[0]));

				return;
			}

			String[] pathSplit = args[0].substring(Math.max(0, args[0].indexOf(":"))).split("/");
			List<LootPool> pools = ObfuscationReflectionHelper.getPrivateValue(LootTable.class, table, "field_186466_c");
			String poolName = pathSplit[pathSplit.length - 1];

			if (pools.isEmpty()) {
				sender.sendMessage(new TextComponentString("Loot table " + args[0] + " is empty. Nothing to print."));

				return;
			}

			LootTableWriter.writeTable(poolName, pools, sender);
			sender.sendMessage(new TextComponentString("Generated loot table: " + TextFormatting.DARK_BLUE + poolName + TextFormatting.WHITE + " to config folder"));
		}
	}
}