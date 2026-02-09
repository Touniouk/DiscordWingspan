package game.ui.discord;

import game.service.DiscordBotService;
import game.ui.discord.processors.ButtonInteractionProcessor;
import game.ui.discord.processors.SlashCommandProcessor;
import game.ui.discord.processors.StringSelectInteractionProcessor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import util.LogLevel;
import util.Logger;

public class DiscordBot extends ListenerAdapter {

    public static final String BOT_TOKEN = Config.get("bot.token");
    public static final String CLIENT_ID = Config.get("bot.client_id");
    public static final String GUILD_ID = Config.get("bot.guild_id");
    public static final String DEFAULT_GAME_CHANNEL = Config.get("bot.default_game_channel_id");

    private final Logger logger = new Logger(DiscordBot.class, LogLevel.ALL);

    public static void main(String[] args) {
        JDA jda = JDABuilder.createDefault(BOT_TOKEN,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new DiscordBot())
                .setActivity(Activity.playing("Playing Wingspan"))
                .build();
        jda.updateCommands().queue();

        DiscordBotService.getInstance().setDiscordBot(jda);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        logger.io("onMessageReceived -> " + event.getMessage().getAuthor().getName() + ": " + event.getMessage().getContentRaw());
        if (event.getAuthor().isBot()) return;
        event.getChannel().sendMessage("onMessageReceived -> " + event.getMessage().getAuthor().getName() + ": " + event.getMessage().getContentRaw()).queue();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        logger.io("onSlashCommandInteraction -> " + event.getName());
        SlashCommandProcessor.handleCommand(event);
    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        logger.io("onCommandAutoCompleteInteraction -> " + event.getName() + ":" + event.getFocusedOption().getName());
        SlashCommandProcessor.handleAutoComplete(event);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        logger.io("onButtonInteraction -> " + event.getInteraction().getButton().getLabel());
        ButtonInteractionProcessor.handleCommand(event);
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        logger.io("onStringSelectInteraction -> " + event.getInteraction());
        StringSelectInteractionProcessor.handleCommand(event);
    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        event.getGuild().updateCommands().addCommands(
                SlashCommandProcessor.getCommandsData()
        ).queue();
    }
}
