package com.train.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.PostConstruct;
import org.springframework.data.domain.Sort;

@Configuration
public class MongoIndexConfig {

    @Autowired
    private MongoTemplate mongoTemplate;

    @PostConstruct
    public void createIndexes() {
        // Index for UserProgress collection
        mongoTemplate.indexOps("user_progress")
            .ensureIndex(new Index("user_id", Sort.Direction.ASC));
        
        mongoTemplate.indexOps("user_progress")
            .ensureIndex(new Index("user_id", Sort.Direction.ASC)
                .on("date", Sort.Direction.DESC));
        
        mongoTemplate.indexOps("user_progress")
            .ensureIndex(new Index("date", Sort.Direction.DESC));
        
        // Compound index for user_id and workout_type
        mongoTemplate.indexOps("user_progress")
            .ensureIndex(new Index("user_id", Sort.Direction.ASC)
                .on("workout_type", Sort.Direction.ASC));
        
        // TTL index for automatic cleanup (optional, 1 year)
        mongoTemplate.indexOps("user_progress")
            .ensureIndex(new Index("date", Sort.Direction.ASC)
                .expire(31536000));
        
        // Index for ExerciseRecord collection
        mongoTemplate.indexOps("exercise_record")
            .ensureIndex(new Index("user_id", Sort.Direction.ASC)
                .on("exercise_date", Sort.Direction.DESC));
        
        mongoTemplate.indexOps("exercise_record")
            .ensureIndex(new Index("user_id", Sort.Direction.ASC)
                .on("exercise_type", Sort.Direction.ASC));
    }
}
