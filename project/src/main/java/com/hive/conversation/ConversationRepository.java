package com.hive.conversation;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface ConversationRepository
        extends MongoRepository<Conversation, String> {

    /**
     * Find all conversations where the user is a participant.
     */
    List<Conversation> findByParticipantIdsContaining(String userId);

    /**
     * Find a conversation containing BOTH participant IDs.
     *
     * MongoDB $all ensures both IDs must exist in participantIds.
     *
     * Example:
     * participantIds: ["user1", "user2"]
     */
    @Query("{ 'participantIds': { $all: [?0, ?1] } }")
    Optional<Conversation> findByBothParticipantIds(
            String participantId1,
            String participantId2
    );
}