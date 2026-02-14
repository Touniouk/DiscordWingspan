package game.ui.discord.commands;

import game.Game;
import game.components.enums.FoodType;
import game.components.enums.HabitatEnum;
import game.components.enums.NestType;
import game.components.subcomponents.BirdCard;
import game.service.DiscordBotService;
import game.ui.discord.enumeration.Constants;
import game.ui.discord.enumeration.EmojiEnum;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SeeTray implements SlashCommand {

    private static final String name = "see_tray";
    private static final String description = "See the bird tray";

    private static final String PARAM_GAME_ID = Constants.GAME_ID;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(name, description)
                .addOption(OptionType.STRING, PARAM_GAME_ID, "The game id (defaults to your active game)", false, true);
    }

    @Override
    public void handleAutoComplete(CommandAutoCompleteInteractionEvent event) {
        SlashCommand.autoCompleteGameId(event);
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        Optional<DiscordBotService.GameContext> gameContextOptional = DiscordBotService.resolveGameContext(event);
        if (gameContextOptional.isEmpty()) return;
        DiscordBotService.GameContext gameContext = gameContextOptional.get();

        seeTray(event, gameContext.game());
    }

    public static void seeTray(IReplyCallback event, Game currentGame) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Birds in tray:");
        embed.setColor(0x1abc9c);

        for (BirdCard bird : currentGame.getBirdDeck().getTray()) {
            embed.addField(bird.getName(), buildBirdDetails(bird), true);
        }

        event.replyEmbeds(embed.build())
                .setEphemeral(true)
                .queue();
    }

    private static String buildBirdDetails(BirdCard bird) {
        StringBuilder sb = new StringBuilder();

        // Habitats
        String habitats = bird.getHabitats().stream()
                .map(h -> h.getEmoji().getEmoteId())
                .collect(Collectors.joining(" "));
        sb.append(habitats).append("\n");

        // Food cost
        sb.append("Food: ");
        if (bird.getFoodCost().isEmpty() || bird.getFoodCost().stream().allMatch(List::isEmpty)) {
            sb.append(EmojiEnum.NO_FOOD.getEmoteId());
        } else {
            String foodCost = bird.getFoodCost().stream()
                    .map(alt -> alt.stream()
                            .map(f -> f.getEmoji().getEmoteId())
                            .collect(Collectors.joining("")))
                    .collect(Collectors.joining(" / "));
            sb.append(foodCost);
        }
        sb.append("\n");

        // Nest + egg capacity
        sb.append(bird.getNest().getType().getEmoji().getEmoteId())
                .append(" ").append(EmojiEnum.EGG.getEmoteId())
                .append(" x").append(bird.getNest().getCapacity()).append("\n");

        // Victory points + wingspan
        sb.append(EmojiEnum.FEATHER_POINTS.getEmoteId()).append(" ").append(bird.getFeatherPoints())
                .append(" | ").append(bird.getWingspan()).append("cm\n");

        // Power text
        if (bird.getPower() != null && bird.getPower().getPowerText() != null && !bird.getPower().getPowerText().isEmpty()) {
            sb.append("\n*").append(bird.getPower().getPowerText()).append("*");
        }

        return sb.toString();
    }
}
