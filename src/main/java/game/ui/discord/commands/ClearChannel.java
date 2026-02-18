package game.ui.discord.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.List;

/**
 * Slash command: {@code /clear_channel} - Deletes messages in the channel.
 * Restricted to members with the Manage Messages permission.
 */
public class ClearChannel implements SlashCommand {

    private static final String name = "clear_channel";
    private static final String description = "Delete messages in this channel (mod only)";

    @Override
    public String getName() {
        return name;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(name, description)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                .addOption(OptionType.INTEGER, "count", "Number of messages to scan (default 200, max 1000)", false)
                .addOption(OptionType.BOOLEAN, "all_messages", "Delete all messages, not just bot messages (default false)", false);
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply(true).queue();

        int count = event.getOption("count", 200, OptionMapping::getAsInt);
        count = Math.min(count, 1000);
        count = Math.max(count, 1);
        boolean allMessages = event.getOption("all_messages", false, OptionMapping::getAsBoolean);

        MessageChannel channel = event.getChannel();
        channel.getIterableHistory().takeAsync(count).thenAccept(messages -> {
            List<Message> toDelete;
            if (allMessages) {
                toDelete = messages;
            } else {
                toDelete = messages.stream()
                        .filter(m -> m.getAuthor().equals(event.getJDA().getSelfUser()))
                        .toList();
            }

            if (toDelete.isEmpty()) {
                event.getHook().editOriginal("No messages to delete.").queue();
                return;
            }

            event.getChannel().asTextChannel().purgeMessages(toDelete);
            event.getHook().editOriginal("Deleted " + toDelete.size() + " messages.").queue();
        });
    }
}
