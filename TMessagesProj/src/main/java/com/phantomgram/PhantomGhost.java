package com.phantomgram;

import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;

/**
 * PhantomGhost - Ghost Mode Network Packet Interceptor
 * Intercepts MTProto RPC calls before they leave the device.
 */
public class PhantomGhost {

    private static boolean allowOneReadPacket = false;

    public static void allowNextRead() {
        allowOneReadPacket = true;
    }

    /**
     * Inspects outgoing MTProto request in ConnectionsManager.sendRequest.
     * @return true if the packet should be CANCELLED (not sent to Telegram server), false if allowed.
     */
    public static boolean filterOutgoingRequest(TLObject object, RequestDelegate onComplete) {
        if (!PhantomConfig.ghostModeEnabled) {
            return false;
        }

        // 1. Intercept Typing / Upload status
        if (!PhantomConfig.sendTypingPackets && (
                object instanceof TLRPC.TL_messages_setTyping ||
                object instanceof TLRPC.TL_messages_setEncryptedTyping
        )) {
            // Drop typing status silently
            return true;
        }

        // 2. Intercept Online Status
        if (!PhantomConfig.sendOnlinePackets && object instanceof TLRPC.TL_account_updateStatus) {
            TLRPC.TL_account_updateStatus statusReq = (TLRPC.TL_account_updateStatus) object;
            // Force status to offline so server never registers user online
            statusReq.offline = true;
            return false; // let modified offline packet pass
        }

        // 3. Intercept Read Receipts (Messages & Channels)
        if (!PhantomConfig.sendReadPackets && (
                object instanceof TLRPC.TL_messages_readHistory ||
                object instanceof TLRPC.TL_messages_readEncryptedHistory ||
                object instanceof TLRPC.TL_messages_readDiscussion ||
                object instanceof TLRPC.TL_messages_readMessageContents ||
                object instanceof TLRPC.TL_channels_readHistory ||
                object instanceof TLRPC.TL_channels_readMessageContents
        )) {
            if (allowOneReadPacket) {
                allowOneReadPacket = false;
                return false; // allow this single manual read request
            }

            // Return fake affectedMessages to prevent UI locks
            if (onComplete != null) {
                TLRPC.TL_messages_affectedMessages fakeRes = new TLRPC.TL_messages_affectedMessages();
                fakeRes.pts = -1;
                fakeRes.pts_count = 0;
                onComplete.run(fakeRes, null);
            }
            return true; // Cancel sending to Telegram servers
        }

        // 4. Intercept Stories stealth view
        if (PhantomConfig.stealthStories && (
                object instanceof org.telegram.tgnet.tl.TL_stories.TL_stories_readStories
        )) {
            if (onComplete != null) {
                TLRPC.Vector fakeVector = new TLRPC.Vector();
                onComplete.run(fakeVector, null);
            }
            return true; // Dropped: story viewed silently without appearing in viewer list
        }

        return false;
    }
}
