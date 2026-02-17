package game.ui.discord.commands;

import game.components.subcomponents.BirdCard;
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
import java.util.stream.Collectors;

/**
 * Slash command: {@code /bird_info} - Look up detailed information about a bird card.
 */
public class BirdInfo implements SlashCommand {

    private static final String name = "bird_info";
    private static final String description = "Look up a bird card by name";

    @Override
    public String getName() {
        return name;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(name, description)
                .addOption(OptionType.STRING, "name", "Bird name", true, true);
    }

    @Override
    public void handleAutoComplete(CommandAutoCompleteInteractionEvent event) {
        String typed = event.getFocusedOption().getValue();
        List<Command.Choice> choices = CardRegistry.getInstance()
                .searchBirdNames(typed, 25)
                .stream()
                .map(n -> new Command.Choice(n, n))
                .toList();
        event.replyChoices(choices).queue();
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        String birdName = event.getOption("name").getAsString();
        Optional<BirdCard> result = CardRegistry.getInstance().findBirdByName(birdName);

        if (result.isEmpty()) {
            event.reply("Bird not found: **" + birdName + "**").setEphemeral(true).queue();
            return;
        }

        BirdCard bird = result.get();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(0x1abc9c);

        // Title: bird name
        embed.setTitle(bird.getName());

        // Description: scientific name + expansion
        StringBuilder desc = new StringBuilder();
        if (bird.getScientificName() != null && !bird.getScientificName().isEmpty()) {
            desc.append("*").append(bird.getScientificName()).append("*\n");
        }
        desc.append(bird.getExpansion().getLabel());
        embed.setDescription(desc.toString());

        // Victory points
        embed.addField("Victory Points",
                bird.getFeatherPoints() + " " + EmojiEnum.FEATHER_POINTS.getEmoteId(), true);

        // Habitats
        String habitats = bird.getHabitats().stream()
                .map(h -> h.getEmoji().getEmoteId() + " " + h.getJsonValue())
                .collect(Collectors.joining("\n"));
        embed.addField("Habitat", habitats, true);

        // Food cost
        String foodCost;
        if (bird.getFoodCost().isEmpty() || bird.getFoodCost().stream().allMatch(List::isEmpty)) {
            foodCost = EmojiEnum.NO_FOOD.getEmoteId() + " None";
        } else {
            foodCost = bird.getFoodCost().stream()
                    .map(alt -> alt.stream()
                            .map(f -> f.getEmoji().getEmoteId())
                            .collect(Collectors.joining(" ")))
                    .collect(Collectors.joining(" / "));
        }
        embed.addField("Food Cost", foodCost, true);

        // Nest
        embed.addField("Nest",
                bird.getNest().getType().getEmoji().getEmoteId() + " " + bird.getNest().getType().getLabel() +
                " — " + EmojiEnum.EGG.getEmoteId() + " x" + bird.getNest().getCapacity(), true);

        // Wingspan
        embed.addField("Wingspan", bird.getWingspan() + " cm", true);

        // Categories (predator/flocking/bonus)
        StringBuilder categories = new StringBuilder();
        if (bird.isPredator()) categories.append(EmojiEnum.PREDATOR.getEmoteId()).append(" Predator\n");
        if (bird.isFlocking()) categories.append(EmojiEnum.FLOCKING.getEmoteId()).append(" Flocking\n");
        if (bird.isBonus()) categories.append(EmojiEnum.BONUS.getEmoteId()).append(" Bonus\n");
        if (!categories.isEmpty()) {
            embed.addField("Categories", categories.toString().trim(), true);
        }

        // Power text (full width field at the bottom)
        if (bird.getPower() != null && bird.getPower().getPowerText() != null && !bird.getPower().getPowerText().isEmpty()) {
            embed.addField("Power", StringUtil.replacePlaceholders(bird.getPower().getPowerText()), false);
        }

        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }
}
