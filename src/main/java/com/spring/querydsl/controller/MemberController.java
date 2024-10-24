package com.spring.querydsl.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spring.querydsl.dto.MemberSearchCondition;
import com.spring.querydsl.dto.MemberTeamDto;
import com.spring.querydsl.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;

    @GetMapping("/members")
    public List<MemberTeamDto> searchMember(MemberSearchCondition condition, Pageable pageable) {
        return memberRepository.searchPage(condition, pageable)
                .getContent();
    }

}
