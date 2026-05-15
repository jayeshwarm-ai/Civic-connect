package com.civicconnect.identity.config;

import com.civicconnect.identity.entity.SecurityQuestion;
import com.civicconnect.identity.repository.SecurityQuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds the security_questions lookup table on first startup. Idempotent —
 * skips any question text that is already present, so it's safe to run on
 * every boot.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityQuestionSeedRunner implements CommandLineRunner {

    private static final List<String> DEFAULT_QUESTIONS = List.of(
            "In which city were you born?",
            "What was the name of your first school?",
            "What is your mother's maiden name?",
            "What was the name of your first pet?",
            "What is the name of your favorite teacher?",
            "What is your father's middle name?",
            "In which city did your parents meet?",
            "What was the model of your first car or vehicle?",
            "What is the name of the street you grew up on?",
            "What is your favorite book?"
    );

    private final SecurityQuestionRepository repository;

    @Override
    public void run(String... args) {
        int inserted = 0;
        for (String text : DEFAULT_QUESTIONS) {
            if (!repository.existsByQuestionText(text)) {
                repository.save(SecurityQuestion.builder().questionText(text).build());
                inserted++;
            }
        }
        if (inserted > 0) {
            log.info("Seeded {} security questions (total now: {})", inserted, repository.count());
        }
    }
}
