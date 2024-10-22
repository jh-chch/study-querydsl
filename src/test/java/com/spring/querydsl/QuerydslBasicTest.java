package com.spring.querydsl;

import static com.spring.querydsl.entity.QMember.member;
import static com.spring.querydsl.entity.QTeam.team;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spring.querydsl.entity.Member;
import com.spring.querydsl.entity.Team;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

	@PersistenceContext
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

	@DisplayName("JPQL로 작성")
	@Test
	void testJPQL() {
		// member1 검색
		Member findMember1 = em.createQuery("select m from Member m where m.username = :usernmae", Member.class)
				.setParameter("username", "member1")
				.getSingleResult();

		assertThat(findMember1.getUsername()).isEqualTo("member1");
	}

	@DisplayName("QUERYDSL로 작성")
	@Test
	void testQUERYDSL() {
		// - member1 검색
		// JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);
		// QMember qMember = new QMember("m"); QMember qMember = QMember.member;

		Member findMember1 = queryFactory
				.select(member)
				.from(member)
				.where(member.username.eq("member1"))
				.fetchOne();

		assertThat(findMember1.getUsername()).isEqualTo("member1");
	}

	@Test
	void search() {
		Member fetchOne = queryFactory
				.selectFrom(member)
				.where( // and 대신 ','' 로 작성가능 만약 null이 들어오면 무시해준다.
						member.username.eq("member1"),
						member.age.between(10, 30))
				// .where(member.username.eq("member1")
				// .and(member.age.between(10, 30)))
				.fetchOne();

		assertThat(fetchOne.getUsername()).isEqualTo("member1");

		member.username.ne("member1"); // !=

		member.age.goe(20); // age >= 20
		member.age.gt(20); // age > 20
		member.age.loe(20); // age <= 20
		member.age.lt(20); // age < 20

		member.username.like("member%");
		member.username.contains("member"); // like '%member%'
		member.username.startsWith("member"); // like 'member%'
	}

	/**
	 * fetch(): 리스트 조회, 데이터가 없으면 빈 리스트 반환
	 * fetchOne(): 단 건 조회, 데이터가 없으면 null 반환, 둘 이상이면 NonUniqueResultException
	 * fetchFirst(): limit(1).fetchOne()
	 * fetchResults(): 페이징 정보 포함, total count 쿼리 추가 실행 -> fetch()
	 * fetchCount(): count쿼리로 변경해서 count수 조회 -> fetch().size()
	 */
	@Test
	void fetch() {
		queryFactory
				.selectFrom(member)
				.fetchResults();

		queryFactory
				.selectFrom(member)
				.fetch().size();
	}

	@Test
	void sort() {
		queryFactory
				.selectFrom(member)
				.orderBy(member.age.desc(), member.username.asc().nullsLast())
				.fetch();
	}

	@Test
	void paging() {
		List<Member> fetch = queryFactory
				.selectFrom(member)
				.orderBy(member.username.desc())
				.offset(1)
				.limit(2)
				.fetch();

		assertThat(fetch.size()).isEqualTo(2);

		QueryResults<Member> fetchResults = queryFactory
				.selectFrom(member)
				.orderBy(member.username.desc())
				.offset(1)
				.limit(2)
				.fetchResults();

		assertThat(fetchResults.getTotal()).isEqualTo(4);
		assertThat(fetchResults.getLimit()).isEqualTo(2);
		assertThat(fetchResults.getOffset()).isEqualTo(1);
		assertThat(fetchResults.getResults().size()).isEqualTo(2);
	}

	@Test
	void aggregation() {
		List<Tuple> fetch = queryFactory
				.select(
						member.count(),
						member.age.sum(),
						member.age.avg(),
						member.age.max())
				.from(member)
				.fetch(); // fetchOne()으로 바로 Tuple을 꺼낼 수 있다.

		Tuple tuple = fetch.get(0);
		assertThat(tuple.get(member.count())).isEqualTo(4);
		assertThat(tuple.get(member.age.sum())).isEqualTo(100);
	}

	@DisplayName("팀의 이름과 팀의 평균 연령")
	@Test
	void groupBy() {
		List<Tuple> fetch = queryFactory
				.select(team.name, member.age.avg())
				.from(member)
				.join(member.team, team)
				.groupBy(team.name)
				// .having()
				.fetch();

		Tuple teamA = fetch.get(0);

		assertThat(teamA.get(team.name)).isEqualTo("teamA");
		assertThat(teamA.get(member.age.avg())).isEqualTo(15);
	}
}
