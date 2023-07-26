package com.speer.OA.security;

import org.springframework.context.annotation.Configuration;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;

@Configuration
public class RateLimitConfiguration {

	/**
	 * Here, I use the Token Bucket strategy for rate limiting, the bucket will be refill to 10 in every 1 minute
	 * 
	 * The reason why I need to set default value here is because I want to make a bucket for each logged in user.
	 * 
	 * Therefore, the createNewBucket cannot be a Bean, and I don't want to make this configuration everywhere in 
	 * the application, so let's just make them static.
	 */
    private static final int REQUESTS_PER_MINUTE = 10;
    private static final Refill refill = Refill.greedy(REQUESTS_PER_MINUTE, java.time.Duration.ofMinutes(1));
    private static final Bandwidth limit = Bandwidth.classic(REQUESTS_PER_MINUTE, refill);

    public static Bucket createNewBucket() {
        return Bucket4j.builder()
                .addLimit(limit)
                .build();
    }
}