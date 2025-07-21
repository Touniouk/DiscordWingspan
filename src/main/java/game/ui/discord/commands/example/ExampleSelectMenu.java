package game.ui.discord.commands.example;

import game.ui.discord.commands.SlashCommand;
import game.ui.discord.enumeration.Constants;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

public class ExampleSelectMenu implements SlashCommand {

    private static final String name = "example_select_menu";
    private static final String description = "Returns an example select menu";

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
        StringSelectMenu menu1 = StringSelectMenu.create("bird_menu_1")
                .setPlaceholder("Pick ground birds")
                .setMinValues(1)
                .setMaxValues(5)
                .addOptions(
                        SelectOption.of("Owl", "owl"),
                        SelectOption.of("Eagle", "eagle"),
                        SelectOption.of("Penguin", "penguin"),
                        SelectOption.of("Hawk", "hawk"),
                        SelectOption.of("Parrot", "parrot")
                )
                .build();

        StringSelectMenu menu2 = StringSelectMenu.create("bird_menu_2")
                .setPlaceholder("Pick sky birds")
                .addOptions(
                        SelectOption.of("Falcon", "falcon"),
                        SelectOption.of("Sparrow", "sparrow")
                )
                .build();

        event.reply("Choose your birds:")
                .addActionRow(menu1)
                .addActionRow(menu2)
                .addActionRow(Button.primary("submit_birds", Constants.SUBMIT_SELECTION))
                .setEphemeral(true)
                .queue();
    }
}
