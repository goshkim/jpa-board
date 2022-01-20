package jpa.board.repository;

import jpa.board.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    List<Member> findMembersById(Long id);
    List<Member> findMembersByLoginId(String loginId);
}