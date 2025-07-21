package game.ui.discord.commands.example;

import game.ui.discord.commands.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.awt.*;

public class ExampleEmbed implements SlashCommand {

    private static final String name = "example_embed";
    private static final String description = "Returns an example embed";

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
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Title", null);
        eb.setColor(Color.red);
        eb.setColor(new Color(0xF40C0C));
        eb.setColor(new Color(255, 0, 54));
        eb.setDescription("Text");
        eb.addField("Title of field", "test of field", false);
        eb.addBlankField(false);
        eb.setAuthor("name", null, "https://github.com/zekroTJA/DiscordBot/blob/master/.websrc/zekroBot_Logo_-_round_small.png");
        eb.setFooter("Text", "https://github.com/zekroTJA/DiscordBot/blob/master/.websrc/zekroBot_Logo_-_round_small.png");
        eb.setImage("https://github.com/zekroTJA/DiscordBot/blob/master/.websrc/logo%20-%20title.png");
        eb.setThumbnail("https://github.com/zekroTJA/DiscordBot/blob/master/.websrc/logo%20-%20title.png");
        event.getChannel().sendMessage(MessageCreateData.fromEmbeds(eb.build())).queue();
    }
}
