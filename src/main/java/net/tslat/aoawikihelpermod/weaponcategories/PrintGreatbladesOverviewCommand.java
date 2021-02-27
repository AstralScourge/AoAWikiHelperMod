package net.tslat.aoawikihelpermod.weaponcategories;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import net.tslat.aoa3.item.weapon.greatblade.BaseGreatblade;
import net.tslat.aoa3.util.NumberUtil;
import net.tslat.aoawikihelpermod.AttributeHandler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PrintGreatbladesOverviewCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("printgreatbladesoverview")
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
            List<BaseGreatblade> greatblades = new ArrayList<BaseGreatblade>();

            for (Item item: ForgeRegistries.ITEMS.getValues()) {
                if(item instanceof BaseGreatblade) {
                    greatblades.add((BaseGreatblade)item);
                }
            }

            greatblades = greatblades.stream().sorted(Comparator.comparing(greatblade -> greatblade.getDisplayName(new ItemStack(greatblade)).getString())).collect(Collectors.toList());

            data.add("{| cellpadding=\"5\" class=\"sortable\" width=\"100%\" cellspacing=\"0\" border=\"1\" style=\"text-align:center\"");
            data.add("|- style=\"background-color:#eee\"");
            data.add("! Name !! data-sort-type=number | Damage !! Attack speed !! Durability !! Effects");
            data.add("|-");

            for (BaseGreatblade greatblade : greatblades) {
                ItemStack greatbladeStack = new ItemStack(greatblade);
                String name = greatblade.getDisplayName(greatbladeStack).getString();
                String attackSpeed = NumberUtil.roundToNthDecimalPlace((float)AttributeHandler.getStackAttributeValue(greatbladeStack, SharedMonsterAttributes.ATTACK_SPEED, (PlayerEntity)sender, EquipmentSlotType.MAINHAND, Item.ATTACK_SPEED_MODIFIER), 2);

                data.add("| [[File:" + name + ".png|64px|link=]] '''[[" + name + "]]''' || {{hp|" + NumberUtil.roundToNthDecimalPlace((float)greatblade.getAttackDamage() + 1, 1) + "}} || " + attackSpeed + " || " + greatblade.getMaxDamage(greatbladeStack) + " || ");
                data.add("|-");
            }

            data.add("|}");
            CategoryTableWriter.writeData("Greatblades", data, player, copyToClipboard);
        }
    }
}
