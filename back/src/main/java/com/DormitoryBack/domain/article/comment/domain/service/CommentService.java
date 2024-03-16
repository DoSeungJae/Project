package com.DormitoryBack.domain.article.comment.domain.service;

import com.DormitoryBack.domain.article.comment.domain.dto.*;
import com.DormitoryBack.domain.article.comment.domain.entity.Comment;
import com.DormitoryBack.domain.article.comment.domain.repository.CommentRepository;
import com.DormitoryBack.domain.article.domain.entity.Article;
import com.DormitoryBack.domain.article.domain.repository.ArticleRepository;
import com.DormitoryBack.domain.jwt.TokenProvider;
import com.DormitoryBack.domain.member.entity.User;
import com.DormitoryBack.domain.member.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TokenProvider tokenProvider;

    public Comment getComment(Long commentId){
        Comment comment=commentRepository.findById(commentId).orElse(null);
        if(comment==null){
            throw new IllegalArgumentException("CommentNotFound");
        }
        return comment;
    }
    public List<Comment> getAllComments(){
        List<Comment> comments=commentRepository.findAll();
        if(comments.isEmpty()){
            throw new RuntimeException("NoCommentFound");
        }
        return comments;
    }
    public List<Comment> getUserComments(Long userId){
        User user=userRepository.findById(userId).orElse(null);
        if(user==null){
            throw new RuntimeException("UserNotFound");
        }
        List<Comment> comments=commentRepository.findAllByUser(user);
        if(comments.isEmpty()){
            throw new RuntimeException("NoCommentFound");
        }
        return comments;

    }
    /*
    public List<Comment> getArticleComments(Long articleId){
        Article article=articleRepository.findById(articleId).orElse(null);
        if(article==null){
            throw new RuntimeException("ArticleNotFound");
        }
        List<Comment> comments=commentRepository.findAllByArticle(article);
        if(comments.isEmpty()){
            throw new RuntimeException("NoCommentFound");
        }
        return comments;
    }

     */

    public CommentPageResponseDTO getArticleCommentsPerPage(int page, int size, Long articleId){
        Pageable pageable= PageRequest.of(page,size, Sort
                .by("createdTime")
                .ascending());
        Article article=articleRepository.findById(articleId).orElse(null);
        if(article==null){
            throw new RuntimeException("ArticleNotFound");
        }
        Page<Comment> commentPage=commentRepository.findAllByArticle(article,pageable);
        if(commentPage.isEmpty() && page==0){
            throw new RuntimeException("CommentNotFound");
        }
        if(commentPage.isEmpty()){
            throw new RuntimeException("NoMoreCommentPage");
        }
        List<Comment> rootComments=new ArrayList<>();
        List<Comment> replyComments=new ArrayList<>();
        Iterator<Comment> iterator=commentPage.iterator();
        while(iterator.hasNext()){
            Comment comment=iterator.next();
            if(comment.isRootCommentNull()){ //rootComment가 없다 => 자신이 rootComment이다. 즉, replyComment가 아님
                rootComments.add(comment);
            }
            else{
                replyComments.add(comment);
            }
        }
        log.info(listStringify(rootComments).toString());
        log.info(listStringify(replyComments).toString());

        CommentPageResponseDTO responseDTO=CommentPageResponseDTO
                .builder()
                .rootComments(listStringify(rootComments))
                .replyComments(listStringify(replyComments))
                .build();

        return responseDTO;
    }
    public List<String> listStringify(List<Comment> commentList){
        List<String> stringifiedCommentList=commentList.stream()
                .map(Comment::toJsonString)
                .collect(Collectors.toList());

        return stringifiedCommentList;
    }
    @Transactional
    public Comment newComment(CommentDTO dto,String token){
        if (!tokenProvider.validateToken(token)) {
            throw new JwtException("InvalidToken");
        }

        Long userId=tokenProvider.getUserIdFromToken(token);
        User userData=userRepository.findById(userId).orElse(null);
        Article article=articleRepository.findById(dto.getArticleId()).orElse(null);


        Comment newComment=Comment.builder()
                .article(article)
                .user(userData)
                .content(dto.getContent())
                .createdTime(LocalDateTime.now())
                .isUpdated(false)
                .build();
        Comment saved=commentRepository.save(newComment);
        return saved;

    }

    @Transactional
    public CommentReplyResponseDTO newReply(CommentReplyDTO dto, String token) {
        if(!tokenProvider.validateToken(token)){
            throw new JwtException("InvalidToken");
        }
        Long userId=tokenProvider.getUserIdFromToken(token);
        User userData=userRepository.findById(userId).orElse(null);
        Comment rootComment=commentRepository.findById(dto.getRootCommentId()).orElse(null);
        Article rootArticle=articleRepository.findById(rootComment.getArticleId()).orElse(null);
        if(rootComment==null){
            throw new RuntimeException("CommentNotFound");
        }
        if(rootComment.getRootComment()!=null){
            throw new RuntimeException("CannotReplyToReplies");
        }
        if(rootArticle==null){
            throw new RuntimeException("ArticleNotFound");
        }
        Comment newReply=Comment.builder()
                .article(rootArticle)
                .user(userData)
                .content(dto.getContent())
                .createdTime(LocalDateTime.now())
                .isUpdated(false)
                .build();

        rootComment.addReplyComment(newReply);
        Comment saved=commentRepository.save(newReply);
        CommentReplyResponseDTO commentResponseDTO= CommentReplyResponseDTO.builder()
                .content(saved.getContent())
                .time(saved.getCreatedTime())
                .rootCommentId(saved.getRootComment().getId())
                .build();

        return commentResponseDTO;
    }

    @Transactional
    public Comment updateComment(CommentUpdateDTO dto,Long commentId, String token){
        if(!tokenProvider.validateToken(token)){
            throw new JwtException("InvalidToken");
        }
        Comment comment=commentRepository.findById(commentId).orElse(null);
        if(comment==null){
            throw new IllegalArgumentException("CommentNotFound");
        }
        if(comment.getUser().getId()!=tokenProvider.getUserIdFromToken(token)){
            throw new RuntimeException("NoPermission");
        }
        comment.update(dto);
        Comment saved=commentRepository.save(comment);

        return saved;
    }

    @Transactional
    public void deleteComment(Long commentId, String token) {
        if(!tokenProvider.validateToken(token)){
            throw new JwtException("InvalidToken");
        }
        Comment target=commentRepository.findById(commentId).orElse(null);
        if(target==null){
            throw new IllegalArgumentException("CommentNotFound");
        }
        Comment comment=commentRepository.findById(commentId).orElse(null);
        if(comment.getUser().getId()!=tokenProvider.getUserIdFromToken(token)){
            throw new RuntimeException("NoPermission");
        }
        commentRepository.delete(target);
    }

    public List<String> pageStringify(Page<Comment> commentPage){
        List<String> stringifiedCommentList=commentPage.getContent()
                .stream()
                .map(Comment::toJsonString)
                .collect(Collectors.toList());

        return stringifiedCommentList;
    }


}
