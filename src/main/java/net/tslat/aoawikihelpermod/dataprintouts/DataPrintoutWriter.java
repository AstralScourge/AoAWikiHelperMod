package net.tslat.aoawikihelpermod.dataprintouts;

import net.minecraft.command.ICommandSource;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.tslat.aoa3.advent.AdventOfAscension;
import net.tslat.aoawikihelpermod.AoAWikiHelperMod;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DataPrintoutWriter {
    public static File configDir = null;
    private static PrintWriter writer = null;

    /*
    public static void writeItemEntityDropsList(ICommandSource sender, ItemStack targetStack, boolean copyToClipboard) {
        if (writer != null) {
            sender.sendMessage(new StringTextComponent("You're already outputting data! Wait a moment and try again"));

            return;
        }

        String fileName = targetStack.getItem().getDisplayName(targetStack).getString() + " Output Trades.txt";
        ArrayList<String> entities = new ArrayList<String>();
        World world = ServerLifecycleHooks.getCurrentServer().getWorld(DimensionType.OVERWORLD);
        Method lootTableMethod = ObfuscationReflectionHelper.findMethod(LivingEntity.class, "func_184647_J", ResourceLocation.class);

        enableWriter(fileName);

        for (EntityEntry entry : ForgeRegistries.ENTITIES.getValuesCollection()) {
            if (!LivingEntity.class.isAssignableFrom(entry.getEntityClass()))
                continue;

            LivingEntity entity = (LivingEntity)entry.newInstance(world);
            LootTable table;

            try {
                ResourceLocation tableLocation = (ResourceLocation)lootTableMethod.invoke(entity);

                if (tableLocation == null)
                    continue;

                table = world.getLootTableManager().getLootTableFromLocation(tableLocation);
            }
            catch (Exception e) {
                continue;
            }

            if (table == LootTable.EMPTY_LOOT_TABLE)
                continue;

            List<LootPool> pools = ObfuscationReflectionHelper.getPrivateValue(LootTable.class, table, "field_186466_c");
            AccessibleLootTable accessibleTable = new AccessibleLootTable(pools, "");

            for (AccessibleLootTable.AccessibleLootPool pool : accessibleTable.pools) {
                for (AccessibleLootTable.AccessibleLootEntry poolEntry : pool.lootEntries) {
                    if (poolEntry.item == targetStack.getItem())
                        entities.add(entity.getDisplayName().getUnformattedText());
                }
            }
        }

        if (entities.size() >= 15) {
            write("Click 'Expand' to see a list of mobs that drop " + targetStack.getItem().getDisplayName(targetStack).getString());
            write("<div class=\"mw-collapsible mw-collapsed\">");
        }

        entities.sort(Comparator.naturalOrder());
        entities.forEach(e -> write("* [[" + e + "]]"));

        disableWriter();
        sender.sendMessage(AoAWikiHelperMod.generateInteractiveMessagePrintout("Printed out " + entities.size() + " entities that drop ", new File(configDir, fileName), targetStack.getDisplayName(), copyToClipboard && AoAWikiHelperMod.copyFileToClipboard(new File(configDir, fileName)) ? ". Copied to clipboard" : ""));
    }*/

    public static void writeData(String name, List<String> data, ICommandSource sender, boolean copyToClipboard) {
        String fileName = name + " Printout " + AdventOfAscension.VERSION + ".txt";

        enableWriter(fileName);

        data.forEach(DataPrintoutWriter::write);

        disableWriter();
        sender.sendMessage(AoAWikiHelperMod.generateInteractiveMessagePrintout("Generated data file: ", new File(configDir, fileName), name, copyToClipboard && AoAWikiHelperMod.copyFileToClipboard(new File(configDir, fileName)) ? ". Copied to clipboard" : ""));
    }

    private static void enableWriter(final String fileName) {
        configDir = AoAWikiHelperMod.prepConfigDir("Data Printouts");

        File streamFile = new File(configDir, fileName);

        try {
            if (streamFile.exists())
                streamFile.delete();

            streamFile.createNewFile();

            writer = new PrintWriter(streamFile);
        }
        catch (Exception e) {}
    }

    private static void disableWriter() {
        if (writer != null)
            IOUtils.closeQuietly(writer);

        writer = null;
    }

    private static void write(String line) {
        if (writer != null)
            writer.println(line);
    }
}