package com.xlchat;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Клиент-мод: скрывает сообщения чата по триггерам из config/xlchat.txt
 */
public final class ChatFilterMod {
    private static final String CONFIG_FILE = "xlchat.txt";
    private static final Logger LOGGER = LogManager.getLogger(Core.MODID);
    private static List<String> triggers = Collections.emptyList();

    static {
        loadConfig();
    }

    private static void loadConfig() {
        Path path = FMLPaths.CONFIGDIR.get().resolve(CONFIG_FILE);
        try {
            if (Files.notExists(path)) {
                Files.write(path, List.of(
                        "# One trigger - one line",
                        "# The entry of the line into the message => the message is not shown",
                        "# Lines starting C # are ignored",
                        "#",
                        "bad_word",
                        "spam_link"
                ), StandardCharsets.UTF_8);
            }
            triggers = Files.readAllLines(path, StandardCharsets.UTF_8).stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty() && !s.startsWith("#"))
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.error("[xL ChatFilter] не удалось прочитать {}: {}", path, e.getMessage());
            triggers = Collections.emptyList();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onChat(ClientChatReceivedEvent event) {
        String msg = event.getMessage().getString().toLowerCase(Locale.ROOT);
        for (String t : triggers) {
            if (msg.contains(t)) {
                event.setCanceled(true);
                return;
            }
        }
    }

    @SubscribeEvent
    public static void onClientCommands(RegisterClientCommandsEvent evt) {
        CommandDispatcher<CommandSourceStack> d = evt.getDispatcher();
        d.register(Commands.literal("xlchat")
                .then(Commands.literal("reload")
                        .executes(ctx -> {
                            loadConfig();
                            ctx.getSource().sendSuccess(
                                    () -> Component.literal("[xL ChatFilter] Триггеры перезагружены.")
                                            .withStyle(ChatFormatting.GREEN),
                                    false
                            );
                            return 0;
                        })
                )
        );
    }
}
