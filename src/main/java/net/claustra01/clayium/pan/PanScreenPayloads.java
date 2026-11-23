package net.claustra01.clayium.pan;

import java.util.List;
import net.claustra01.clayium.ClayiumMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class PanScreenPayloads {
    public static final int SORT = 0;
    public static final int REVERSE = 1;
    public static final int PAGE = 2;
    public static final int SELECT = 3;
    public static final int TOGGLE_PROHIBITED = 4;

    public record Candidate(ResourceLocation item, long cost, long consumption, boolean prohibited) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Candidate> STREAM_CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC, Candidate::item,
                ByteBufCodecs.VAR_LONG, Candidate::cost,
                ByteBufCodecs.VAR_LONG, Candidate::consumption,
                ByteBufCodecs.BOOL, Candidate::prohibited,
                Candidate::new);
    }

    public record Control(BlockPos pos, int action, int value) implements CustomPacketPayload {
        public static final Type<Control> TYPE = new Type<>(ClayiumMod.id("pan_screen_control"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Control> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, Control::pos,
                ByteBufCodecs.VAR_INT, Control::action,
                ByteBufCodecs.VAR_INT, Control::value,
                Control::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record Snapshot(int containerId, int page, int pages, int sort, boolean reverse,
                           ResourceLocation selected, List<Candidate> candidates)
            implements CustomPacketPayload {
        private static final ResourceLocation NONE = ClayiumMod.id("none");
        public static final Type<Snapshot> TYPE = new Type<>(ClayiumMod.id("pan_screen_snapshot"));
        private static final StreamCodec<RegistryFriendlyByteBuf, View> VIEW_STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, View::page,
                ByteBufCodecs.VAR_INT, View::pages,
                ByteBufCodecs.VAR_INT, View::sort,
                ByteBufCodecs.BOOL, View::reverse,
                ResourceLocation.STREAM_CODEC, View::selected,
                View::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, Snapshot> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Snapshot::containerId,
                VIEW_STREAM_CODEC, Snapshot::view,
                Candidate.STREAM_CODEC.apply(ByteBufCodecs.list()), Snapshot::candidates,
                Snapshot::fromNetwork);

        public Snapshot {
            candidates = List.copyOf(candidates);
        }

        private View view() {
            return new View(page, pages, sort, reverse, selected);
        }

        private static Snapshot fromNetwork(int containerId, View view, List<Candidate> candidates) {
            return new Snapshot(containerId, view.page(), view.pages(), view.sort(), view.reverse(),
                    view.selected(), candidates);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public long contentHash() {
            long hash = (((31L * page + pages) * 31L + sort) * 31L + (reverse ? 1L : 0L));
            hash = 31L * hash + selected.hashCode();
            for (Candidate candidate : candidates) {
                hash = 31L * hash + candidate.hashCode();
            }
            return hash;
        }
    }

    private record View(int page, int pages, int sort, boolean reverse, ResourceLocation selected) {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(Control.TYPE, Control.STREAM_CODEC, PanScreenPayloads::handleControl);
        event.registrar("1").playToClient(Snapshot.TYPE, Snapshot.STREAM_CODEC, PanScreenPayloads::handleSnapshot);
    }

    private static void handleControl(Control payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            apply(player, payload);
        }
    }

    public static void apply(Player player, Control payload) {
        if (!(player.containerMenu instanceof PanMenu menu)
                || !menu.position().equals(payload.pos())
                || !menu.stillValid(player)
                || menu.pan().kind() != PanBlock.Kind.CORE
                || payload.action() < SORT
                || payload.action() > TOGGLE_PROHIBITED) {
            return;
        }
        if (payload.action() == SORT && (payload.value() < 0 || payload.value() > 2)
                || payload.action() == PAGE && (payload.value() < 0 || payload.value() >= menu.pan().candidatePages())
                || payload.action() == SELECT
                        && (payload.value() < 0 || payload.value() >= PanBlockEntity.CANDIDATES_PER_PAGE)
                || (payload.action() == REVERSE || payload.action() == TOGGLE_PROHIBITED)
                        && payload.value() != 0) {
            return;
        }
        menu.pan().applyScreenControl(payload.action(), payload.value());
    }

    private static void handleSnapshot(Snapshot payload, IPayloadContext context) {
        if (context.player().containerMenu instanceof PanMenu menu) {
            menu.applySnapshot(payload);
        }
    }

    public static Snapshot snapshot(PanMenu menu) {
        PanBlockEntity pan = menu.pan();
        ResourceLocation selected = pan.selectedCandidate() == null ? Snapshot.NONE : pan.selectedCandidate();
        return new Snapshot(menu.containerId, pan.candidatePage(), pan.candidatePages(), pan.candidateSort(),
                pan.candidateReverse(), selected, pan.visibleCandidates());
    }

    private PanScreenPayloads() {
    }
}
