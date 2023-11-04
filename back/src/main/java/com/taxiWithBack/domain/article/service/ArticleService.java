package com.taxiWithBack.domain.article.service;

import com.taxiWithBack.domain.article.dto.ArticleDTO;
import com.taxiWithBack.domain.article.entity.Article;
import com.taxiWithBack.domain.article.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    //Token provider가 필요한가?
    public Article newArticle(ArticleDTO dto) {
        Article newOne = Article.builder()
                .dorId(dto.getDorId())
                .title(dto.getTitle())
                .content(dto.getContent())
                .category(dto.getCategory())
                .createTime(dto.getCreateTime())
                .appointedTime(null)
                .build();

        Article saved = articleRepository.save(newOne);
        return saved;

    }

    public Article getArticle(Long articleId){
        Article article=articleRepository.findByArticleId(articleId);
        return article;

    }
    public List<Article> getAllArticles(){
        List<Article> articles=articleRepository.findAll();
        return articles;

    }

    public List<Article> getDorArticles(Long dorId){
        List<Article> dorArticles=articleRepository.findAllByDorId(dorId);
        return dorArticles;
    }




}
