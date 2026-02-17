package game.ui.discord.commands;

import game.components.subcomponents.BonusCard;
import game.service.CardRegistry;
import game.ui.discord.enumeration.EmojiEnum;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import util.StringUtil;

import java.util.List;
import java.util.Optional;

/**
 * Slash command: {@code /bonus_info} - Look up detailed information about a bonus card.
 */
public class BonusInfo implements SlashCommand {

    private static final String name = "bonus_info";
    private static final String description = "Look up a bonus card by name";

    @Override
    public String getName() {
        return name;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(name, description)
                .addOption(OptionType.STRING, "name", "Bonus card name", true, true);
    }

    @Override
    public void handleAutoComplete(CommandAutoCompleteInteractionEvent event) {
        String typed = event.getFocusedOption().getValue();
        List<Command.Choice> choices = CardRegistry.getInstance()
                .searchBonusNames(typed, 25)
                .stream()
                .map(n -> new Command.Choice(n, n))
                .toList();
        event.replyChoices(choices).queue();
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        String bonusName = event.getOption("name").getAsString();
        Optional<BonusCard> result = CardRegistry.getInstance().findBonusByName(bonusName);

        if (result.isEmpty()) {
            event.reply("Bonus card not found: **" + bonusName + "**").setEphemeral(true).queue();
            return;
        }

        BonusCard bonus = result.get();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(0xf1c40f);

        // Title: bonus card name
        embed.setTitle(EmojiEnum.BONUS.getEmoteId() + " " + bonus.getName());

        // Description: expansion
        embed.setDescription(bonus.getExpansion().getLabel());

        // Condition
        embed.addField("Condition", bonus.getCondition(), false);

        // Explanatory text
        if (bonus.getExplanatoryText() != null && !bonus.getExplanatoryText().equals("null")) {
            embed.addField("Details", StringUtil.replacePlaceholders(bonus.getExplanatoryText()), false);
        }

        // VP scoring
        if (bonus.getVp() != null && !bonus.getVp().equals("null")) {
            String vpFormatted = bonus.getVp().replace("[point]", EmojiEnum.FEATHER_POINTS.getEmoteId());
            embed.addField("Victory Points", vpFormatted, false);
        }

        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }
}
