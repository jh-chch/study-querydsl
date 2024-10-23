package com.spring.querydsl;

import static com.spring.querydsl.entity.QMember.member;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spring.querydsl.dto.MemberDto;
import com.spring.querydsl.entity.Member;
import com.spring.querydsl.entity.Team;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

@SpringBootTest
@Transactional
public class QuerydslProjectionTests {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    void testEntity() {
        queryFactory = new JPAQueryFactory(em);

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @DisplayName("프로젝션 대상 하나 조회, select 대상 지정")
    @Test
    void simpleProjection() {
        List<Member> result = queryFactory
                .select(member)
                .from(member)
                .fetch();

        List<String> result2 = queryFactory
                .select(member.username)
                .from(member)
                .fetch();
    }

    @DisplayName("프로젝션 대상이 둘 이상 조회, 튜플")
    @Test
    void tupleProjection() {
        List<Tuple> result = queryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println(tuple.get(member.username));
            System.out.println(tuple.get(member.age));
        }
    }

    @DisplayName("프로젝션 대상이 둘 이상 조회, DTO")
    @Test
    void dtoProjection() {
        // 순수 JPA에서 DTO 조회, JPQL 문법
        // 복잡하고, 생성자 방식만 지원한다.
        TypedQuery<MemberDto> query = em.createQuery(
                "select new com.spring.querydsl.dto.MemberDto(m.username, m.age)" +
                        " from Member m",
                MemberDto.class);
        List<MemberDto> resultList = query.getResultList();
        for (MemberDto dto : resultList) {
            System.out.println(dto.getUsername());
        }

        // Querydsl

        // 1. setter(기본 생성자 필요)
        queryFactory
                .select(Projections.bean(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        // 2. 필드 (getter, setter 필요없음)
        queryFactory
                .select(Projections.fields(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        // 3. 생성자
        queryFactory
                .select(Projections.constructor(MemberDto.class,
                        member.username, // .as("name") 로 alias를 줄 수 있다.
                        member.age))
                .from(member)
                .fetch();

        // 4. ExpressionUtils 사용
        // ExpressionUtils.as(JPAExpressions.select(...), null)
    }

}
