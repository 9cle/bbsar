package mchorse.bbs_mod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mchorse.bbs_mod.data.DataToString;
import mchorse.bbs_mod.forms.FormUtils;
import mchorse.bbs_mod.forms.forms.Form;
import mchorse.bbs_mod.morphing.Morph;
import mchorse.bbs_mod.network.ServerNetwork;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;

public class BBSCommands
{
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment)
    {
        LiteralArgumentBuilder<ServerCommandSource> bbs = CommandManager.literal("bbs").requires((source) -> source.hasPermissionLevel(2));

        registerMorphCommand(bbs, environment);
        registerMorphEntityCommand(bbs, environment);
        registerFilmsCommand(bbs, environment);
        registerDCCommand(bbs, environment);
        registerOnHead(bbs, environment);

        dispatcher.register(bbs);
    }

    private static void registerMorphCommand(LiteralArgumentBuilder<ServerCommandSource> bbs, CommandManager.RegistrationEnvironment environment)
    {
        LiteralArgumentBuilder<ServerCommandSource> morph = CommandManager.literal("morph");
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> target = CommandManager.argument("target", EntityArgumentType.player());
        RequiredArgumentBuilder<ServerCommandSource, String> form = CommandManager.argument("form", StringArgumentType.greedyString());

        morph.then(target
            .executes(BBSCommands::morphCommandDemorph)
            .then(form.executes(BBSCommands::morphCommandMorph)));

        bbs.then(morph);
    }

    private static void registerMorphEntityCommand(LiteralArgumentBuilder<ServerCommandSource> bbs, CommandManager.RegistrationEnvironment environment)
    {
        LiteralArgumentBuilder<ServerCommandSource> morph = CommandManager.literal("morph_entity");

        morph.executes((source) ->
        {
            Entity entity = source.getSource().getEntity();

            if (entity instanceof ServerPlayerEntity player)
            {
                Form form = Morph.getMobForm(player);

                if (form != null)
                {
                    ServerNetwork.sendMorphToTracked(player, form);
                    Morph.getMorph(entity).setForm(FormUtils.copy(form));
                }
            }

            return 1;
        });

        bbs.then(morph);
    }

    private static void registerFilmsCommand(LiteralArgumentBuilder<ServerCommandSource> bbs, CommandManager.RegistrationEnvironment environment)
    {
        LiteralArgumentBuilder<ServerCommandSource> scene = CommandManager.literal("films");
        LiteralArgumentBuilder<ServerCommandSource> play = CommandManager.literal("play");
        LiteralArgumentBuilder<ServerCommandSource> stop = CommandManager.literal("stop");
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> target = CommandManager.argument("target", EntityArgumentType.players());
        RequiredArgumentBuilder<ServerCommandSource, String> playFilm = CommandManager.argument("film", StringArgumentType.string());
        RequiredArgumentBuilder<ServerCommandSource, String> stopFilm = CommandManager.argument("film", StringArgumentType.string());
        RequiredArgumentBuilder<ServerCommandSource, Boolean> camera = CommandManager.argument("camera", BoolArgumentType.bool());

        playFilm.suggests((ctx, builder) ->
        {
            for (String key : BBSMod.getFilms().getKeys())
            {
                builder.suggest(key);
            }

            return builder.buildFuture();
        });

        stopFilm.suggests((ctx, builder) ->
        {
            for (String key : BBSMod.getFilms().getKeys())
            {
                builder.suggest(key);
            }

            return builder.buildFuture();
        });

        scene.then(
            target.then(
                play.then(
                    playFilm.executes((source) -> sceneCommandPlay(source, true))
                        .then(
                            camera.executes((source) -> sceneCommandPlay(source, BoolArgumentType.getBool(source, "camera")))
                        )
                )
            )
            .then(
                stop.then(
                    stopFilm.executes(BBSCommands::sceneCommandStop)
                )
            )
        );

        bbs.then(scene);
    }

    private static void registerDCCommand(LiteralArgumentBuilder<ServerCommandSource> bbs, CommandManager.RegistrationEnvironment environment)
    {
        LiteralArgumentBuilder<ServerCommandSource> dc = CommandManager.literal("dc");
        LiteralArgumentBuilder<ServerCommandSource> shutdown = CommandManager.literal("shutdown");
        LiteralArgumentBuilder<ServerCommandSource> start = CommandManager.literal("start");
        LiteralArgumentBuilder<ServerCommandSource> stop = CommandManager.literal("stop");

        bbs.then(
            dc.then(start.executes(BBSCommands::DCCommandStart))
                .then(stop.executes(BBSCommands::DCCommandStop))
                .then(shutdown.executes(BBSCommands::DCCommandShutdown))
        );
    }

    private static void registerOnHead(LiteralArgumentBuilder<ServerCommandSource> bbs, CommandManager.RegistrationEnvironment environment)
    {
        LiteralArgumentBuilder<ServerCommandSource> onHead = CommandManager.literal("on_head");

        bbs.then(onHead.executes(BBSCommands::onHead));
    }

    /**
     * /bbs morph McHorseYT - demorph (remove morph) player McHorseYT
     */
    private static int morphCommandDemorph(CommandContext<ServerCommandSource> source) throws CommandSyntaxException
    {
        ServerPlayerEntity entity = EntityArgumentType.getPlayer(source, "target");

        ServerNetwork.sendMorphToTracked(entity, null);
        Morph.getMorph(entity).setForm(null);

        return 1;
    }

    /**
     * /bbs morph McHorse {id:"bbs:model",model:"butterfly",texture:"assets:models/butterfly/yellow.png"}
     *
     * Morphs player McHorseYT into a butterfly model with yellow skin
     */
    private static int morphCommandMorph(CommandContext<ServerCommandSource> source) throws CommandSyntaxException
    {
        ServerPlayerEntity entity = EntityArgumentType.getPlayer(source, "target");
        String formData = StringArgumentType.getString(source, "form");

        try
        {
            Form form = FormUtils.fromData(DataToString.mapFromString(formData));

            ServerNetwork.sendMorphToTracked(entity, form);
            Morph.getMorph(entity).setForm(FormUtils.copy(form));

            return 1;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * /bbs film McHorseYT play test - Plays a film (with camera) to McHorseYT
     * /bbs film @a play test false - Plays a film (without camera) to all players
     */
    private static int sceneCommandPlay(CommandContext<ServerCommandSource> source, boolean withCamera) throws CommandSyntaxException
    {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(source, "target");
        String filmId = StringArgumentType.getString(source, "film");

        for (ServerPlayerEntity player : players)
        {
            ServerNetwork.sendPlayFilm(player, filmId, withCamera);
        }

        return 1;
    }

    /**
     * /bbs film McHorseYT stop test - Stops film playback
     */
    private static int sceneCommandStop(CommandContext<ServerCommandSource> source) throws CommandSyntaxException
    {
        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(source, "target");
        String filmId = StringArgumentType.getString(source, "film");

        for (ServerPlayerEntity player : players)
        {
            ServerNetwork.sendStopFilm(player, filmId);
        }

        return 1;
    }

    private static int DCCommandShutdown(CommandContext<ServerCommandSource> source)
    {
        BBSMod.getActions().resetDamage(source.getSource().getWorld());

        return 1;
    }

    private static int DCCommandStart(CommandContext<ServerCommandSource> source)
    {
        BBSMod.getActions().trackDamage(source.getSource().getWorld());

        return 1;
    }

    private static int DCCommandStop(CommandContext<ServerCommandSource> source)
    {
        BBSMod.getActions().stopDamage(source.getSource().getWorld());

        return 1;
    }

    private static int onHead(CommandContext<ServerCommandSource> source)
    {
        if (source.getSource().getEntity() instanceof LivingEntity livingEntity)
        {
            ItemStack stack = livingEntity.getEquippedStack(EquipmentSlot.MAINHAND);

            if (!stack.isEmpty())
            {
                livingEntity.equipStack(EquipmentSlot.HEAD, stack.copy());
            }
        }

        return 1;
    }
}
