package com.spring.querydsl;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spring.querydsl.entity.Hello;
import com.spring.querydsl.entity.QHello;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
class QuerydslApplicationTests {

	@Autowired
	EntityManager em;

	@Test
	void contextLoads() {
		Hello hello = new Hello();
		em.persist(hello);

		JPAQueryFactory queryFactory = new JPAQueryFactory(em);
		QHello qHello = new QHello("h");

		Hello fetchOne = queryFactory
				.selectFrom(qHello)
				.fetchOne();

		Assertions.assertThat(fetchOne).isEqualTo(hello);
		Assertions.assertThat(fetchOne.getId()).isEqualTo(hello.getId());
	}

}
