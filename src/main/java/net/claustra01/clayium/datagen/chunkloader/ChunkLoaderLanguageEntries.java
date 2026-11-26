package net.claustra01.clayium.datagen.chunkloader;

import java.util.function.BiConsumer;

public final class ChunkLoaderLanguageEntries {
    public static void add(String locale, BiConsumer<String, String> add) {
        boolean japanese = locale.equals("ja_jp");
        add.accept("block.clayium.clay_3x3_chunk_loader", japanese ? "3×3チャンクローダー" : "3x3 Clay Chunk Loader");
        add.accept("gui.clayium.chunk_loader.status.waiting", japanese ? "状態: チケット待機中" : "Status: Waiting for tickets");
        add.accept("gui.clayium.chunk_loader.status.active", japanese ? "状態: 稼働中" : "Status: Active");
        add.accept("gui.clayium.chunk_loader.status.unavailable", japanese ? "状態: 稼働不可" : "Status: Unavailable");
        add.accept("gui.clayium.chunk_loader.range", japanese ? "範囲: 3×3 (9チャンク)" : "Range: 3x3 (9 chunks)");
        add.accept("gui.clayium.chunk_loader.rejected", japanese
                ? "サーバーがチャンクチケットを拒否しました"
                : "The server rejected the chunk tickets");
        add.accept("tooltip.clayium.clay_3x3_chunk_loader.line1", japanese
                ? "中心チャンクと周囲8チャンクを維持する"
                : "Keeps the center and eight adjacent chunks loaded");
        add.accept("tooltip.clayium.clay_3x3_chunk_loader.line2", japanese
                ? "チケット拒否時は画面に稼働不可を表示する"
                : "Its screen reports when server tickets are unavailable");
    }

    private ChunkLoaderLanguageEntries() {
    }
}
