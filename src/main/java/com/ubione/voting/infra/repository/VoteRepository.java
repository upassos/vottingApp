package com.ubione.voting.infra.repository;

import com.ubione.voting.domain.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    @Query("select v.choice as choice, count(v) as total from Vote v where v.agenda.id = ?1 group by v.choice")
    List<Object[]> countByChoice(Long agendaId);
}
