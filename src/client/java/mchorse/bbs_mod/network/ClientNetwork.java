package mchorse.bbs_mod.network;

import mchorse.bbs_mod.BBSModClient;
import mchorse.bbs_mod.actions.ActionState;
import mchorse.bbs_mod.blocks.entities.ModelBlockEntity;
import mchorse.bbs_mod.data.types.BaseType;
import mchorse.bbs_mod.data.types.MapType;
import mchorse.bbs_mod.film.Film;
import mchorse.bbs_mod.film.Films;
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
import mchorse.bbs_mod.ui.dashboard.UIDashboard;
import mchorse.bbs_mod.ui.film.UIFilmPanel;
import mchorse.bbs_mod.ui.framework.UIBaseMenu;
import mchorse.bbs_mod.ui.framework.UIScreen;
import mchorse.bbs_mod.ui.model_blocks.UIModelBlockPanel;
import mchorse.bbs_mod.utils.clips.Clips;
import mchorse.bbs_mod.utils.repos.RepositoryOperation;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ClientNetwork
{
    private static int ids = 0;
    private static Map<Integer, Consumer<BaseType>> callbacks = new HashMap<>();
    private static boolean isBBSModOnServer;

    public static void resetHandshake()
    {
        isBBSModOnServer = false;
    }

    public static boolean isIsBBSModOnServer()
    {
        return isBBSModOnServer;
    }

    /* Network */

    public static void setup()
    {
        ClientPlayNetworking.registerGlobalReceiver(ClientClickedModelPayload.ID, (payload, context) -> handleClientModelBlockPacket(context.client(), payload));
        ClientPlayNetworking.registerGlobalReceiver(ClientPlayerFormPayload.ID, (payload, context) -> handlePlayerFormPacket(context.client(), payload));
        ClientPlayNetworking.registerGlobalReceiver(ClientPlayFilmPayload.ID, (payload, context) -> handlePlayFilmPacket(context.client(), payload));
        ClientPlayNetworking.registerGlobalReceiver(ClientManagerDataPayload.ID, (payload, context) -> handleManagerDataPacket(context.client(), payload));
        ClientPlayNetworking.registerGlobalReceiver(ClientStopFilmPayload.ID, (payload, context) -> handleStopFilmPacket(context.client(), payload));
        ClientPlayNetworking.registerGlobalReceiver(ClientHandshakePayload.ID, (payload, context) -> isBBSModOnServer = true);
        ClientPlayNetworking.registerGlobalReceiver(ClientRecordedActionsPayload.ID, (payload, context) -> handleRecordedActionsPacket(context.client(), payload));
    }

    /* Handlers */

    private static void handleClientModelBlockPacket(MinecraftClient client, ClientClickedModelPayload buf)
    {
        BlockPos pos = buf.pos();

        client.execute(() ->
        {
            BlockEntity entity = client.world.getBlockEntity(pos);

            if (!(entity instanceof ModelBlockEntity))
            {
                return;
            }

            UIBaseMenu menu = UIScreen.getCurrentMenu();
            UIDashboard dashboard = BBSModClient.getDashboard();

            if (menu != dashboard)
            {
                UIScreen.open(dashboard);
            }

            UIModelBlockPanel panel = dashboard.getPanels().getPanel(UIModelBlockPanel.class);

            dashboard.setPanel(panel);
            panel.fill((ModelBlockEntity) entity, true);
        });
    }

    private static void handlePlayerFormPacket(MinecraftClient client, ClientPlayerFormPayload buf)
    {
        int id = buf.id();
        Form form = null;

        if (buf.data() instanceof MapType)
        {
            form = FormUtils.fromData((MapType) buf.data());
        }

        final Form finalForm = form;

        client.execute(() ->
        {
            Entity entity = client.world.getEntityById(id);
            Morph morph = Morph.getMorph(entity);

            if (morph != null)
            {
                morph.setForm(finalForm);
            }
        });
    }

    private static void handlePlayFilmPacket(MinecraftClient client, ClientPlayFilmPayload buf)
    {
        String filmId = buf.id();
        boolean withCamera = buf.camera();
        Film film = new Film();

        film.setId(filmId);
        film.fromData(buf.data());

        client.execute(() ->
        {
            Films.playFilm(film, withCamera);
        });
    }

    private static void handleManagerDataPacket(MinecraftClient client, ClientManagerDataPayload buf)
    {
        int callbackId = buf.id();
        RepositoryOperation op = buf.getOp();
        BaseType data = buf.data();

        client.execute(() ->
        {
            Consumer<BaseType> callback = callbacks.remove(callbackId);

            if (callback != null)
            {
                callback.accept(data);
            }
        });
    }

    private static void handleStopFilmPacket(MinecraftClient client, ClientStopFilmPayload buf)
    {
        String filmId = buf.id();

        client.execute(() -> Films.stopFilm(filmId));
    }

    private static void handleRecordedActionsPacket(MinecraftClient client, ClientRecordedActionsPayload buf)
    {
        String filmId = buf.filmId();
        int replayId = buf.replayId();
        BaseType data = buf.data();

        client.execute(() ->
        {
            BBSModClient.getDashboard().getPanels().getPanel(UIFilmPanel.class).receiveActions(filmId, replayId, data);
        });
    }

    /* API */
    
    public static void sendModelBlockForm(BlockPos pos, ModelBlockEntity modelBlock)
    {
        ClientPlayNetworking.send(new ServerModelBlockFormPayload(pos, modelBlock.getProperties().toData()));
    }

    public static void sendPlayerForm(Form form)
    {
        ClientPlayNetworking.send(new ServerPlayerFormPayload(FormUtils.toData(form)));
    }

    public static void sendModelBlockTransforms(MapType data)
    {
        ClientPlayNetworking.send(new ServerModelBlockTransformsPayload(data));
    }

    public static void sendManagerDataLoad(String id, Consumer<BaseType> consumer)
    {
        MapType mapType = new MapType();

        mapType.putString("id", id);
        ClientNetwork.sendManagerData(RepositoryOperation.LOAD, mapType, consumer);
    }

    public static void sendManagerData(RepositoryOperation op, BaseType data, Consumer<BaseType> consumer)
    {
        int id = ids;

        callbacks.put(id, consumer);
        sendManagerData(id, op, data);

        ids += 1;
    }

    public static void sendManagerData(int callbackId, RepositoryOperation op, BaseType data)
    {
        ClientPlayNetworking.send(new ServerManagerDataPayload(callbackId, op.ordinal(), data));
    }

    public static void sendActionRecording(String filmId, int replayId, int tick, boolean state)
    {
        PacketByteBuf buf = PacketByteBufs.create();

        buf.writeString(filmId);
        buf.writeInt(replayId);
        buf.writeInt(tick);
        buf.writeBoolean(state);

        ClientPlayNetworking.send(new ServerActionRecordingPayload(filmId, replayId, tick, state));
    }

    public static void sendToggleFilm(String filmId, boolean withCamera)
    {
        ClientPlayNetworking.send(new ServerToggleFilmPayload(filmId, withCamera));
    }

    public static void sendActionState(String filmId, ActionState state, int tick)
    {
        ClientPlayNetworking.send(new ServerActionControlPayload(filmId, state.ordinal(), tick));
    }

    public static void sendActions(String filmId, int replayId, Clips actions)
    {
        ClientPlayNetworking.send(new ServerActionsUploadPayload(filmId, replayId, actions.toData()));
    }

    public static void sendTeleport(double x, double y, double z)
    {
        ClientPlayNetworking.send(new ServerPlayerTPPayload(x, y, z));
    }
}