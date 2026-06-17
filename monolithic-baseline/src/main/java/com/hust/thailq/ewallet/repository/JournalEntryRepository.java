package com.hust.thailq.ewallet.repository;

import com.hust.thailq.ewallet.entity.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {
}
