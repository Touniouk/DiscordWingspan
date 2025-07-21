package game.ui.discord.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface SlashCommand {

    String getName();

    CommandData getCommandData();

    void handle(SlashCommandInteractionEvent event);
}
