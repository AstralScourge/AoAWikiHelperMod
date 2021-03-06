package net.tslat.aoawikihelpermod.weaponcategories;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoa3.item.tool.axe.BaseAxe;
import net.tslat.aoa3.item.weapon.sword.BaseSword;
import net.tslat.aoa3.util.NumberUtil;
import net.tslat.aoa3.util.StringUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PrintAxesOverviewCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("printaxesoverview")
                        .then(Commands.argument("copyToClipboard", BoolArgumentType.bool())
                                .executes(commandContext -> {
                                    print(commandContext, BoolArgumentType.getBool(commandContext, "copyToClipboard"));
                                    return 0;
                                }))
                        .executes(commandContext -> {
                            print(commandContext, false);
                            return 0;
                        }));
    }

    public static void print(CommandContext<CommandSource> context, boolean attemptToCopy) {
        Entity sender = context.getSource().getEntity();

        if(!(sender instanceof PlayerEntity)) {
            sender.sendMessage(new StringTextComponent("This command can only be done ingame for accuracy."));
            return;
        }

        PlayerEntity player = (PlayerEntity)sender;

        World world = context.getSource().getWorld();

        if(!world.isRemote) {
            boolean copyToClipboard = attemptToCopy;
            if (copyToClipboard && context.getSource().getServer().isDedicatedServer()) {
                sender.sendMessage(new StringTextComponent("Can't copy contents of file to clipboard on dedicated servers, skipping."));
                copyToClipboard = false;
            }

            List<String> data = new ArrayList<String>();
            List<BaseAxe> axes = new ArrayList<BaseAxe>();

            for (Item item: ForgeRegistries.ITEMS.getValues()) {
                if(item instanceof BaseAxe) {
                    axes.add((BaseAxe)item);
                }
            }

            axes = axes.stream().sorted(Comparator.comparing(axe -> axe.getDisplayName(new ItemStack(axe)).getString())).collect(Collectors.toList());

            data.add("{| cellpadding=\"5\" class=\"sortable\" width=\"100%\" cellspacing=\"0\" border=\"1\" style=\"text-align:center\"");
            data.add("|- style=\"background-color:#eee\"");
            data.add("! Name !! data-sort-type=number | Damage !! Efficiency !! Durability !! Effects");
            data.add("|-");

            for (BaseAxe axe : axes) {
                ItemStack axeStack = new ItemStack(axe);
                String name = axe.getDisplayName(axeStack).getString();
                float efficiency = axe.getTier().getEfficiency();
                float damage = axe.getTier().getAttackDamage() + 1.5F + 1;

                data.add("| [[File:" + name + ".png|32px|link=]] '''[[" + name + "]]''' || {{hp|" + NumberUtil.roundToNthDecimalPlace(damage, 1) + "}} || " + NumberUtil.roundToNthDecimalPlace(efficiency, 1) + " || " + axe.getMaxDamage(axeStack) + " || ");
                data.add("|-");
            }

            data.add("|}");

            CategoryTableWriter.writeData("Axes", data, player, copyToClipboard);
        }
    }
}

