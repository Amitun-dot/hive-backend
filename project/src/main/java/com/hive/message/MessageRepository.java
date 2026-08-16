package com.hive.message;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessageRepository extends MongoRepository<Message, String> {

    List<Message> findByConversationIdOrderByCreatedAtAsc(String conversationId);

    Optional<Message> findByIdAndConversationId(String id, String conversationId);
}
