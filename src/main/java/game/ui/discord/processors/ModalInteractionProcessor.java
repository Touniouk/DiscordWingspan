package game.ui.discord.processors;

import game.GameLobby;
import game.service.DiscordBotService;
import game.ui.discord.commands.CreateGame;
import game.ui.discord.enumeration.DiscordObject;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import util.LogLevel;
import util.Logger;

import java.util.Objects;
import java.util.Optional;

/**
 * Routes incoming modal interactions to the appropriate handler based on the modal ID.
 */
public class ModalInteractionProcessor {

    private static final Logger logger = new Logger(ModalInteractionProcessor.class, LogLevel.ALL);

    public static void handleCommand(ModalInteractionEvent event) {
        String rawModalId = event.getModalId().split(":")[0];

        if (rawModalId.equals(DiscordObject.CREATE_GAME_SET_SEED_BUTTON.name())) {
            handleSetSeed(event);
        } else {
            logger.warn("Unmatched modal ID: " + rawModalId);
        }
    }

    private static void handleSetSeed(ModalInteractionEvent event) {
        Optional<DiscordBotService.LobbyContext> lobbyContextOptional = DiscordBotService.resolveLobbyContext(event);
        if (lobbyContextOptional.isEmpty()) return;
        GameLobby lobby = lobbyContextOptional.get().lobby();

        String seedValue = Objects.requireNonNull(event.getValue("seed_value")).getAsString().trim();
        if (seedValue.isEmpty()) {
            lobby.setSeed(0);
        } else {
            try {
                lobby.setSeed(Long.parseLong(seedValue));
            } catch (NumberFormatException e) {
                event.reply("Invalid seed value. Please enter a number.").setEphemeral(true).queue();
                return;
            }
        }

        event.editMessageEmbeds(CreateGame.buildLobbyEmbed(lobby))
                .setComponents(CreateGame.buildLobbyComponents(lobby))
                .queue();
    }
}
