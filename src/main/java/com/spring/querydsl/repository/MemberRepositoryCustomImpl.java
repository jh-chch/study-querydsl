package com.spring.querydsl.repository;

import static com.spring.querydsl.entity.QMember.member;
import static com.spring.querydsl.entity.QTeam.team;
import static org.springframework.util.StringUtils.hasText;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spring.querydsl.dto.MemberSearchCondition;
import com.spring.querydsl.dto.MemberTeamDto;
import com.spring.querydsl.dto.QMemberTeamDto;

import jakarta.persistence.EntityManager;

public class MemberRepositoryCustomImpl implements MemberRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	public MemberRepositoryCustomImpl(EntityManager em) {
		queryFactory = new JPAQueryFactory(em);
	}

	@Override
	public List<MemberTeamDto> search(MemberSearchCondition condition) {
		return queryFactory
				.select(new QMemberTeamDto(
						member.id,
						member.username,
						member.age,
						team.id,
						team.name))
				.from(member)
				.leftJoin(member.team, team)
				.where(usernameEq(condition.getUsername()),
						teamNameEq(condition.getTeamName()),
						ageGoe(condition.getAgeGoe()),
						ageLoe(condition.getAgeLoe()))
				.fetch();
	}

	private BooleanExpression usernameEq(String username) {
		return hasText(username) ? member.username.eq(username) : null;
	}

	private BooleanExpression teamNameEq(String teamName) {
		return hasText(teamName) ? team.name.eq(teamName) : null;
	}

	private BooleanExpression ageGoe(Integer ageGoe) {
		return ageGoe == null ? null : member.age.goe(ageGoe);
	}

	private BooleanExpression ageLoe(Integer ageLoe) {
		return ageLoe == null ? null : member.age.loe(ageLoe);
	}

	@Override
	public Page<MemberTeamDto> searchPage(MemberSearchCondition condition, Pageable pageable) {
		List<MemberTeamDto> content = queryFactory
				.select(new QMemberTeamDto(
						member.id,
						member.username,
						member.age,
						team.id,
						team.name))
				.from(member)
				.leftJoin(member.team, team)
				.where(usernameEq(condition.getUsername()),
						teamNameEq(condition.getTeamName()),
						ageGoe(condition.getAgeGoe()),
						ageLoe(condition.getAgeLoe()))
				.offset(pageable.getOffset())
				.limit(pageable.getPageSize())
				.fetch();

		long total = queryFactory
				.select(new QMemberTeamDto(
						member.id,
						member.username,
						member.age,
						team.id,
						team.name))
				.from(member)
				.leftJoin(member.team, team)
				.where(usernameEq(condition.getUsername()),
						teamNameEq(condition.getTeamName()),
						ageGoe(condition.getAgeGoe()),
						ageLoe(condition.getAgeLoe()))
				.offset(pageable.getOffset())
				.limit(pageable.getPageSize())
				.fetch().size();

		// count 쿼리 최적화
		// 1. 페이지의 시작이고, 컨텐츠 사이즈가 페이지 보다 작을 때
		// 2. 마지막 페이지 일 때
		JPAQuery<MemberTeamDto> result2 = queryFactory
				.select(new QMemberTeamDto(
						member.id,
						member.username,
						member.age,
						team.id,
						team.name))
				.from(member)
				.leftJoin(member.team, team)
				.where(usernameEq(condition.getUsername()),
						teamNameEq(condition.getTeamName()),
						ageGoe(condition.getAgeGoe()),
						ageLoe(condition.getAgeLoe()))
				.offset(pageable.getOffset())
				.limit(pageable.getPageSize());
		// return
		PageableExecutionUtils.getPage(content, pageable, () -> result2.fetch().size());

		return new PageImpl<>(content, pageable, total);
	}

}
