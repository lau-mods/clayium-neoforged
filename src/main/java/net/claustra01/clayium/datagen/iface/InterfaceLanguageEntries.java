package net.claustra01.clayium.datagen.iface;

import java.util.function.BiConsumer;
import net.claustra01.clayium.datagen.logistics.LogisticsLanguageEntries;
import net.claustra01.clayium.machine.iface.InterfaceType;
import net.claustra01.clayium.registry.InterfaceRegistration;

public final class InterfaceLanguageEntries {
    public static void add(String locale, BiConsumer<String, String> add) {
        boolean japanese = locale.equals("ja_jp");
        InterfaceRegistration.INTERFACES.values().forEach(definition -> add.accept(
                "block.clayium." + definition.id(),
                LogisticsLanguageEntries.tierPrefix(locale, definition.tier())
                        + name(definition.type(), japanese)));
        add.accept("item.clayium.synchronizer", japanese ? "シンクロナイザー" : "Synchronizer");
        add.accept("gui.clayium.interface.tier", japanese ? "Tier %s" : "Tier %s");
        add.accept("gui.clayium.interface.target", japanese ? "同期先" : "Synchronized target");
        add.accept("gui.clayium.interface.status.unlinked", japanese ? "状態: 未接続" : "Status: Unlinked");
        add.accept("gui.clayium.interface.status.connected", japanese ? "状態: 接続" : "Status: Connected");
        add.accept("gui.clayium.interface.status.disconnected", japanese ? "状態: 切断" : "Status: Disconnected");
        add.accept("message.clayium.synchronizer.empty", japanese ? "同期先が未登録です" : "No target recorded");
        add.accept("message.clayium.synchronizer.invalid", japanese ? "同期先へ接続できません" : "Cannot access target");
        add.accept("message.clayium.synchronizer.connected", japanese ? "同期しました" : "Interface synchronized");
        add.accept("message.clayium.synchronizer.recorded", japanese ? "同期先を登録しました" : "Target recorded");
    }

    private static String name(InterfaceType type, boolean japanese) {
        return switch (type) {
            case ITEM -> japanese ? "アイテムインターフェース" : " Item Interface";
            case REDSTONE -> japanese ? "赤石インターフェース" : " Redstone Interface";
            case LASER -> japanese ? "粘土レーザーインターフェース" : " Clay Laser Interface";
        };
    }

    private InterfaceLanguageEntries() {
    }
}
