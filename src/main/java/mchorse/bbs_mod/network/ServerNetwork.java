package mchorse.bbs_mod.network;

import io.netty.buffer.ByteBuf;
import mchorse.bbs_mod.BBSMod;
import mchorse.bbs_mod.actions.ActionManager;
import mchorse.bbs_mod.actions.ActionPlayer;
import mchorse.bbs_mod.actions.ActionState;
import mchorse.bbs_mod.blocks.entities.ModelBlockEntity;
import mchorse.bbs_mod.data.DataStorageUtils;
import mchorse.bbs_mod.data.storage.DataStorage;
import mchorse.bbs_mod.data.types.BaseType;
import mchorse.bbs_mod.data.types.ByteType;
import mchorse.bbs_mod.data.types.ListType;
import mchorse.bbs_mod.data.types.MapType;
import mchorse.bbs_mod.film.Film;
import mchorse.bbs_mod.film.FilmManager;
import mchorse.bbs_mod.forms.FormUtils;
import mchorse.bbs_mod.forms.forms.Form;
import mchorse.bbs_mod.morphing.Morph;
import mchorse.bbs_mod.network.payloads.ClientClickedModelPayload;
import mchorse.bbs_mod.network.payloads.ClientHandshakePayload;
import mchorse.bbs_mod.network.payloads.ClientManagerDataPayload;
import mchorse.bbs_mod.network.payloads.ClientPlayFilmPayload;
import mchorse.bbs_mod.network.payloads.ClientPlayerFormPayload;
import mchorse.bbs_mod.network.payloads.ClientRecordedActionsPayload;
import mchorse.bbs_mod.network.payloads.ClientStopFilmPayload;
import mchorse.bbs_mod.network.payloads.ServerActionControlPayload;
import mchorse.bbs_mod.network.payloads.ServerActionRecordingPayload;
import mchorse.bbs_mod.network.payloads.ServerActionsUploadPayload;
import mchorse.bbs_mod.network.payloads.ServerManagerDataPayload;
import mchorse.bbs_mod.network.payloads.ServerModelBlockFormPayload;
import mchorse.bbs_mod.network.payloads.ServerModelBlockTransformsPayload;
import mchorse.bbs_mod.network.payloads.ServerPlayerFormPayload;
import mchorse.bbs_mod.network.payloads.ServerPlayerTPPayload;
import mchorse.bbs_mod.network.payloads.ServerToggleFilmPayload;
import mchorse.bbs_mod.utils.CollectionUtils;
import mchorse.bbs_mod.utils.clips.Clips;
import mchorse.bbs_mod.utils.repos.RepositoryOperation;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ServerNetwork
{
    public static final PacketCodec<ByteBuf, BaseType> BASE_TYPE = new PacketCodec<ByteBuf, BaseType>()
    {
        public BaseType decode(ByteBuf byteBuf)
        {
            if (!byteBuf.readBoolean())
            {
                return null;
            }

            byte[] bytes = new byte[byteBuf.readInt()];

            byteBuf.readBytes(bytes);
            ByteArrayInputStream stream = new ByteArrayInputStream(bytes);

            try
            {
                return DataStorage.readFromStream(stream);
            }
            catch (IOException e)
            {}

            return null;
        }

        public void encode(ByteBuf byteBuf, BaseType type)
        {
            try
            {
                byteBuf.writeBoolean(type != null);

                if (type != null)
                {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();

                    DataStorage.writeToStream(stream, type);

                    byteBuf.writeInt(stream.size());
                    byteBuf.writeBytes(stream.toByteArray());
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    };

    public static void setup()
    {
        PayloadTypeRegistry.playS2C().register(ClientClickedModelPayload.ID, ClientClickedModelPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ClientPlayerFormPayload.ID, ClientPlayerFormPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ClientPlayFilmPayload.ID, ClientPlayFilmPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ClientManagerDataPayload.ID, ClientManagerDataPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ClientStopFilmPayload.ID, ClientStopFilmPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ClientHandshakePayload.ID, ClientHandshakePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ClientRecordedActionsPayload.ID, ClientRecordedActionsPayload.CODEC);

        PayloadTypeRegistry.playC2S().register(ServerModelBlockFormPayload.ID, ServerModelBlockFormPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ServerModelBlockTransformsPayload.ID, ServerModelBlockTransformsPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ServerPlayerFormPayload.ID, ServerPlayerFormPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ServerManagerDataPayload.ID, ServerManagerDataPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ServerActionRecordingPayload.ID, ServerActionRecordingPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ServerToggleFilmPayload.ID, ServerToggleFilmPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ServerActionControlPayload.ID, ServerActionControlPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ServerActionsUploadPayload.ID, ServerActionsUploadPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ServerPlayerTPPayload.ID, ServerPlayerTPPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ServerModelBlockFormPayload.ID, (payload, context) -> handleModelBlockFormPacket(context.server(), context.player(), payload));
        ServerPlayNetworking.registerGlobalReceiver(ServerModelBlockTransformsPayload.ID, (payload, context) -> handleModelBlockTransformsPacket(context.server(), context.player(), payload));
        ServerPlayNetworking.registerGlobalReceiver(ServerPlayerFormPayload.ID, (payload, context) -> handlePlayerFormPacket(context.server(), context.player(), payload));
        ServerPlayNetworking.registerGlobalReceiver(ServerManagerDataPayload.ID, (payload, context) -> handleManagerDataPacket(context.server(), context.player(), payload));
        ServerPlayNetworking.registerGlobalReceiver(ServerActionRecordingPayload.ID, (payload, context) -> handleActionRecording(context.server(), context.player(), payload));
        ServerPlayNetworking.registerGlobalReceiver(ServerToggleFilmPayload.ID, (payload, context) -> handleToggleFilm(context.server(), context.player(), payload));
        ServerPlayNetworking.registerGlobalReceiver(ServerActionControlPayload.ID, (payload, context) -> handleActionControl(context.server(), context.player(), payload));
        ServerPlayNetworking.registerGlobalReceiver(ServerActionsUploadPayload.ID, (payload, context) -> handleActionsUpload(context.server(), context.player(), payload));
        ServerPlayNetworking.registerGlobalReceiver(ServerPlayerTPPayload.ID, (payload, context) -> handleTeleportPlayer(context.server(), context.player(), payload));
    }

    /* Handlers */

    private static void handleModelBlockFormPacket(MinecraftServer server, ServerPlayerEntity player, ServerModelBlockFormPayload buf)
    {
        BlockPos pos = buf.pos();

        try
        {
            MapType data = (MapType) buf.data();

            server.execute(() ->
            {
                World world = player.getWorld();
                BlockEntity be = world.getBlockEntity(pos);

                if (be instanceof ModelBlockEntity modelBlock)
                {
                    modelBlock.updateForm(data, world);
                }
            });
        }
        catch (Exception e)
        {}
    }

    private static void handleModelBlockTransformsPacket(MinecraftServer server, ServerPlayerEntity player, ServerModelBlockTransformsPayload buf)
    {
        try
        {
            MapType data = (MapType) buf.data();

            server.execute(() ->
            {
                ItemStack stack = player.getEquippedStack(EquipmentSlot.MAINHAND).copy();

                stack.set(DataComponentTypes.BLOCK_ENTITY_DATA, stack.get(DataComponentTypes.BLOCK_ENTITY_DATA).apply((d) ->
                {
                    d.put("Properties", DataStorageUtils.toNbt(data));
                }));

                player.equipStack(EquipmentSlot.MAINHAND, stack);
            });
        }
        catch (Exception e)
        {}
    }

    private static void handlePlayerFormPacket(MinecraftServer server, ServerPlayerEntity player, ServerPlayerFormPayload buf)
    {
        MapType data = (MapType) buf.data();
        Form form = null;

        try
        {
            form = BBSMod.getForms().fromData(data);
        }
        catch (Exception e)
        {}

        final Form finalForm = form;

        server.execute(() ->
        {
            Morph.getMorph(player).setForm(FormUtils.copy(finalForm));

            sendMorphToTracked(player, finalForm);
        });
    }

    private static void handleManagerDataPacket(MinecraftServer server, ServerPlayerEntity player, ServerManagerDataPayload buf)
    {
        int callbackId = buf.id();
        RepositoryOperation op = buf.getOp();
        MapType data = (MapType) buf.data();
        FilmManager films = BBSMod.getFilms();

        if (op == RepositoryOperation.LOAD)
        {
            String id = data.getString("id");
            Film film = films.load(id);

            sendManagerData(player, callbackId, op, film.toData());
        }
        else if (op == RepositoryOperation.SAVE)
        {
            films.save(data.getString("id"), data.getMap("data"));
        }
        else if (op == RepositoryOperation.RENAME)
        {
            films.rename(data.getString("from"), data.getString("to"));
        }
        else if (op == RepositoryOperation.DELETE)
        {
            films.delete(data.getString("id"));
        }
        else if (op == RepositoryOperation.KEYS)
        {
            ListType list = DataStorageUtils.stringListToData(films.getKeys());

            sendManagerData(player, callbackId, op, list);
        }
        else if (op == RepositoryOperation.ADD_FOLDER)
        {
            sendManagerData(player, callbackId, op, new ByteType(films.addFolder(data.getString("folder"))));
        }
        else if (op == RepositoryOperation.RENAME_FOLDER)
        {
            sendManagerData(player, callbackId, op, new ByteType(films.renameFolder(data.getString("from"), data.getString("to"))));
        }
        else if (op == RepositoryOperation.DELETE_FOLDER)
        {
            sendManagerData(player, callbackId, op, new ByteType(films.deleteFolder(data.getString("folder"))));
        }
    }

    private static void handleActionRecording(MinecraftServer server, ServerPlayerEntity player, ServerActionRecordingPayload buf)
    {
        String filmId = buf.filmId();
        int replayId = buf.replayId();
        int tick = buf.tick();
        boolean recording = buf.recording();

        server.execute(() ->
        {
            if (recording)
            {
                Film film = BBSMod.getFilms().load(filmId);

                if (film != null)
                {
                    BBSMod.getActions().startRecording(film, player, tick);
                    BBSMod.getActions().play(player.getServerWorld(), film, tick, replayId);
                }
            }
            else
            {
                Clips clips = BBSMod.getActions().stopRecording(player);

                /* Save clips to the film */
                Film film = BBSMod.getFilms().load(filmId);

                if (clips != null && film != null && CollectionUtils.inRange(film.replays.getList(), replayId))
                {
                    film.replays.getList().get(replayId).actions.fromData(clips.toData());
                    BBSMod.getFilms().save(filmId, film.toData().asMap());
                }

                /* Send recorded clips to the client */
                sendRecordedActions(player, filmId, replayId, clips);
            }
        });
    }

    private static void handleToggleFilm(MinecraftServer server, ServerPlayerEntity player, ServerToggleFilmPayload buf)
    {
        String filmId = buf.id();
        boolean withCamera = buf.camera();

        server.execute(() ->
        {
            ActionPlayer actionPlayer = BBSMod.getActions().getPlayer(filmId);

            if (actionPlayer != null)
            {
                BBSMod.getActions().stop(filmId);

                for (ServerPlayerEntity otherPlayer : server.getPlayerManager().getPlayerList())
                {
                    sendStopFilm(otherPlayer, filmId);
                }
            }
            else
            {
                sendPlayFilm(player.getServerWorld(), filmId, withCamera);
            }
        });
    }

    private static void handleActionControl(MinecraftServer server, ServerPlayerEntity player, ServerActionControlPayload buf)
    {
        ActionManager actions = BBSMod.getActions();
        String filmId = buf.filmId();
        ActionState state = buf.getState();
        int tick = buf.tick();

        server.execute(() ->
        {
            if (state == ActionState.SEEK)
            {
                ActionPlayer actionPlayer = actions.getPlayer(filmId);

                if (actionPlayer != null)
                {
                    actionPlayer.goTo(tick);
                }
            }
            else if (state == ActionState.PLAY)
            {
                ActionPlayer actionPlayer = actions.getPlayer(filmId);

                if (actionPlayer != null)
                {
                    actionPlayer.goTo(tick);
                    actionPlayer.playing = true;
                }
            }
            else if (state == ActionState.PAUSE)
            {
                ActionPlayer actionPlayer = actions.getPlayer(filmId);

                if (actionPlayer != null)
                {
                    actionPlayer.goTo(tick);
                    actionPlayer.playing = false;
                }
            }
            else if (state == ActionState.RESTART)
            {
                ActionPlayer actionPlayer = actions.getPlayer(filmId);

                if (actionPlayer == null)
                {
                    Film film = BBSMod.getFilms().load(filmId);

                    if (film != null)
                    {
                        actionPlayer = actions.play(player.getServerWorld(), film, tick);
                    }
                }
                else
                {
                    actions.stop(filmId);

                    actionPlayer = actions.play(player.getServerWorld(), actionPlayer.film, 0);
                }

                if (actionPlayer != null)
                {
                    actionPlayer.syncing = true;
                    actionPlayer.playing = false;

                    if (tick != 0)
                    {
                        actionPlayer.goTo(tick);
                    }
                }

                sendStopFilm(player, filmId);
            }
            else if (state == ActionState.STOP)
            {
                actions.stop(filmId);
            }
        });
    }

    private static void handleActionsUpload(MinecraftServer server, ServerPlayerEntity player, ServerActionsUploadPayload buf)
    {
        String filmId = buf.filmId();
        int replayId = buf.replayId();
        BaseType data = buf.data();

        server.execute(() ->
        {
            BBSMod.getActions().updatePlayers(filmId, replayId, data);
        });
    }

    private static void handleTeleportPlayer(MinecraftServer server, ServerPlayerEntity player, ServerPlayerTPPayload buf)
    {
        server.execute(() -> player.teleport(buf.x(), buf.y(), buf.z(), false));
    }

    /* API */

    public static void sendMorph(ServerPlayerEntity player, int playerId, Form form)
    {
        ServerPlayNetworking.send(player, new ClientPlayerFormPayload(playerId, FormUtils.toData(form)));
    }

    public static void sendMorphToTracked(ServerPlayerEntity player, Form form)
    {
        sendMorph(player, player.getId(), form);

        for (ServerPlayerEntity otherPlayer : PlayerLookup.tracking(player))
        {
            sendMorph(otherPlayer, player.getId(), form);
        }
    }

    public static void sendClickedModelBlock(ServerPlayerEntity player, BlockPos pos)
    {
        ServerPlayNetworking.send(player, new ClientClickedModelPayload(pos));
    }

    public static void sendPlayFilm(ServerWorld world, String filmId, boolean withCamera)
    {
        try
        {
            Film film = BBSMod.getFilms().load(filmId);

            if (film != null)
            {
                BBSMod.getActions().play(world, film, 0);

                ClientPlayFilmPayload payload = new ClientPlayFilmPayload(filmId, withCamera, film.toData());

                for (ServerPlayerEntity otherPlayer : world.getPlayers())
                {
                    ServerPlayNetworking.send(otherPlayer, payload);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void sendPlayFilm(ServerPlayerEntity player, String filmId, boolean withCamera)
    {
        try
        {
            Film film = BBSMod.getFilms().load(filmId);

            if (film != null)
            {
                BBSMod.getActions().play(player.getServerWorld(), film, 0);

                ServerPlayNetworking.send(player, new ClientPlayFilmPayload(filmId, withCamera, film.toData()));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void sendStopFilm(ServerPlayerEntity player, String filmId)
    {
        ServerPlayNetworking.send(player, new ClientStopFilmPayload(filmId));
    }

    public static void sendManagerData(ServerPlayerEntity player, int callbackId, RepositoryOperation op, BaseType data)
    {
        ServerPlayNetworking.send(player, new ClientManagerDataPayload(callbackId, op.ordinal(), data));
    }

    public static void sendRecordedActions(ServerPlayerEntity player, String filmId, int replayId, Clips clips)
    {
        ServerPlayNetworking.send(player, new ClientRecordedActionsPayload(filmId, replayId, clips.toData()));
    }
}