package com.cheng.game.network.codec;

import com.cheng.game.common.error.ErrorCode;
import com.cheng.game.common.protocol.MsgIds;
import com.cheng.game.protocol.NotifyReason;
import com.cheng.game.protocol.SystemNotify;

public final class SystemNotifies {

    private SystemNotifies() {
    }

    public static GamePacket of(ErrorCode errorCode, NotifyReason reason) {
        SystemNotify notify = SystemNotify.newBuilder()
                .setCode(errorCode.code())
                .setMessage(errorCode.message())
                .setReason(reason)
                .build();
        return new GamePacket(MsgIds.SYSTEM_NOTIFY, notify.toByteArray());
    }
}
