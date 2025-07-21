package game.ui.discord.commands.example;

import game.ui.discord.commands.SlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

public class ExampleModal implements SlashCommand {

    private static final String name = "example_modal";
    private static final String description = "Returns an example modal";

    @Override
    public String getName() {
        return name;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(name, description);
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        TextInput subject = TextInput.create("subject", "Subject", TextInputStyle.SHORT)
                .setPlaceholder("Subject of this ticket")
                .setMinLength(10)
                .setMaxLength(100) // or setRequiredRange(10, 100)
                .build();

        TextInput body = TextInput.create("body", "Body", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Your concerns go here")
                .setMinLength(30)
                .setMaxLength(1000)
                .build();

        Modal modal = Modal.create("modmail", "Modmail")
                .addComponents(ActionRow.of(subject), ActionRow.of(body))
                .build();

        event.replyModal(modal).queue();
    }
}
