package org.buratishkin.familyhub.family.poll;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PollAnswerRepository extends JpaRepository<PollAnswerEntity, Long> {
}
