package com.sharkdom.model.organizatiocollaboration;

import com.sharkdom.constants.Flag;
import com.sharkdom.constants.LinkerType;

public record SendMessageRequest(Long chatRoomId, String query, Long linkerId, LinkerType linkerType, Flag flag,
                                 Long senderId, Long receiverId) {
}
