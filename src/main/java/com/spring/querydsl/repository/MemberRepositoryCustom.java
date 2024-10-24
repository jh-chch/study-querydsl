package com.spring.querydsl.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.spring.querydsl.dto.MemberSearchCondition;
import com.spring.querydsl.dto.MemberTeamDto;

public interface MemberRepositoryCustom {
    List<MemberTeamDto> search(MemberSearchCondition condition);

    Page<MemberTeamDto> searchPage(MemberSearchCondition condition, Pageable pageable);
}
