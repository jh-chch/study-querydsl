package com.spring.querydsl.repository;

import java.util.List;

import com.spring.querydsl.dto.MemberSearchCondition;
import com.spring.querydsl.dto.MemberTeamDto;

public interface MemberRepositoryCustom {
    List<MemberTeamDto> search(MemberSearchCondition condition);
}
