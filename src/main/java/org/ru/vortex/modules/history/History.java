package org.ru.vortex.modules.history;

import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.serialization.Base64Coder;
import java.util.*;
import java.util.stream.IntStream;
import mindustry.Vars;
import mindustry.ctype.ContentType;
import mindustry.gen.Player;
import mindustry.net.Administration;
import mindustry.world.Block;
import org.ru.vortex.modules.history.components.*;
import org.ru.vortex.utils.Pipe;

public class History {

    public static final byte shortenedUUIDBites = 8;
    public static final double shortenedUUIDMaxValue = Math.pow(2, shortenedUUIDBites) - 1;
    public static final CirculatingArray<byte[]> shortenedUUIDs = new CirculatingArray<>(new byte[(int) shortenedUUIDMaxValue][]);
    public static final Set<Player> enabledHistory = new HashSet<>();
    private static final byte changeTypeBites = Pipe
        .apply(BlockChangeType.values())
        .pipe(Seq::new)
        .pipe(seq -> seq.max(changeType -> changeType.id))
        .result()
        .id;
    public static byte blocksIDBites;
    public static byte xBites;
    public static byte yBites;
    public static LongCirculatingArray history;

    public static void init() {
        shortenedUUIDs.onRemove(index -> {
            int nativeIndex = index;
            int shift = additionalDataSize() + yBites + xBites + blocksIDBites;
            long biteValueMask = ((long) Math.pow(2, shortenedUUIDBites) - 1) << shift;

            IntStream
                .range(0, history.size())
                .asLongStream()
                .parallel()
                .forEach(historyIndex -> {
                    if ((history.get((int) historyIndex) & biteValueMask) >> shift == nativeIndex) history.remove((int) historyIndex);
                });
        });
    }

    public static void initHistory() {
        blocksIDBites =
            Pipe
                .apply(ContentType.block.ordinal())
                .pipe(ordinalType -> Vars.content.getContentMap()[ordinalType])
                .pipe(blockContent -> blockContent.max(content -> content.id).id)
                .pipe(History::mathBitesSize)
                .result();
        xBites = Pipe.apply(Vars.world.width()).pipe(History::mathBitesSize).result();
        yBites = Pipe.apply(Vars.world.height()).pipe(History::mathBitesSize).result();
        history = new LongCirculatingArray(new long[500 * 500 * 4]);
    }

    public static Long compressHistory(FormattedEntry entry) {
        LongPackager packager = new LongPackager(0);
        byte changeTypeID = entry.changeType().id;
        String UUID = entry.participantInfo().id;
        byte[] decodedUUID = Pipe.apply(UUID).pipe(Base64Coder::decode).result();

        if (!shortenedUUIDs.contains(element -> Arrays.equals(element, decodedUUID))) shortenedUUIDs.add(decodedUUID);

        int shortenedUUID = shortenedUUIDs.indexOf(element -> Arrays.equals(element, decodedUUID));

        packager.write(changeTypeBites, changeTypeID);
        packager.write(shortenedUUIDBites, shortenedUUID);
        packager.write(blocksIDBites, entry.block().id);
        packager.write(xBites, (short) entry.position().getX());
        packager.write(yBites, (short) entry.position().getY());
        packager.write(additionalDataSize(), entry.additionalData());

        return packager.read();
    }

    public static FormattedEntry decompressHistory(long compressedHistory) {
        LongPackager packager = new LongPackager(compressedHistory);

        long additionalData = packager.readBites(additionalDataSize());
        short y = (short) packager.readBites(yBites);
        short x = (short) packager.readBites(xBites);

        Vec2 position = new Vec2(x, y);
        Block block = Pipe.apply((int) packager.readBites(blocksIDBites)).pipe(id -> Vars.content.block(id)).result();
        Administration.PlayerInfo playerInfo = Pipe
            .apply((int) packager.readBites(shortenedUUIDBites))
            .pipe(shortenedUUIDs::get)
            .pipe(Base64Coder::encode)
            .pipe(String::new)
            .pipe(Vars.netServer.admins::findByName)
            .result()
            .first();
        BlockChangeType changeType = Pipe.apply((byte) packager.readBites(changeTypeBites)).pipe(BlockChangeType::getByID).result();
        FormattedEntry historyEntry = changeType.formatEntry(playerInfo, block, position, additionalData);

        historyEntry.applyAdditionalData();
        return historyEntry;
    }

    public static byte additionalDataSize() {
        return (byte) (Long.BYTES * 8 - (xBites + yBites + blocksIDBites + shortenedUUIDBites + changeTypeBites));
    }

    private static byte mathBitesSize(long number) {
        return Pipe.apply(number).pipe(Mathf::log2).pipe(Mathf::ceil).result().byteValue();
    }
}
